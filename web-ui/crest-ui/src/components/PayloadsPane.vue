<template>
  <div class="content">
    <div class="columns">
      <div class="column is-four-fifths">
        <ul id="pyld-info">
          <li v-for="(val,key) in selectedPayload">
            {{ key }} : {{val}}
          </li>
        </ul>
      </div>
      <div class="column is-one-fifth">
        <b-field grouped>
        <p class="control">
          <button class="button is-primary" @click="show()">Download</button>
        </p>
        </b-field>
      </div>
    </div>
  </div>
</template>
<script>
import { mapState, mapActions, mapGetters } from 'vuex'

export default {
  name: 'PayloadsPane',
  props : {
    selectediov : Object,
    selectedserver : String,
  },
  data: function () {
    return {
      selectedPayload : {},
      selactiveTab : 0,
      dowloadlink : ''
    };
  },
  computed: {
      ...mapState('gui/crest', ['selectedTag', 'selectedIov']),
      ...mapGetters('db/payloads', ['getPayload']),
    "payloadmeta": function loadPayloadmeta() {
      if (typeof this.selectedIov === "undefined") {
        return {};
      } else {
          this.fetchPayloadMeta(this.selectedIov).then(() => {
              (this.selectedPayload = this.getPayload[this.selectedIov]);
              return this.getPayload[this.selectedIov];
          }).catch(error => {
              console.error(error);
              return Promise.reject(error);
          });
    }},
    selectDoc() {
      if (this.selectedIov !== "") {
        this.download2();
        return true;
      }
      return false;
    },
    hostbaseurl () {
    return this.selectedserver;
    },
    infomsg () {
      return "Access api  "+this.selectedserver
        +"<br> Selected tag is : "+this.selectedTag
        +"<br> Selected iov is : "+this.selectediov.since+" hash=>"+this.selectedIov;
    }
  },
  methods: {
      ...mapActions('db/payloads', ['fetchPayloadMeta']),
    selectTab(nt) {
      console.log('Selecting tab '+nt)
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    },
  /*  async download() {
      axios({
        url: `${this.hostbaseurl}/payloads/`+this.selectedIov,
        method: 'GET',
        responseType: 'blob', // important
      }).then((response) => {
         const url = window.URL.createObjectURL(new Blob([response.data]));
         const link = document.createElement('a');
         link.href = url;
         link.setAttribute('download', 'payload.blob'); //or any other extension
         document.body.appendChild(link);
         link.click();
      });
    },*/
    loadMetadata() {
        this.fetchPayloadMeta(this.selectedIov).then(() => {
            (this.selectedPayload = this.getPayload[this.selectedIov])
        });
    },
    download2() {
			this.downloadlink = '/crestapi/payloads/'+this.selectedIov;
		},
    show() {
      if (this.selectDoc) {
        window.open(this.downloadlink, "_blank");
      }
    }
  },
  components: {
  },
  created() {
      this.$store.commit('gui/crest/selectIov', this.selectediov.payloadHash);
      this.loadMetadata();
  }
};
</script>
