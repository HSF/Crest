<template>
  <div class="content">
    <div class="columns">
      <div class="column is-four-fifths">
        <ul id="pyld-info">
          <li v-for="(val,key) in selectedPayload"  v-bind:key="key">
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
  },
  data: function () {
    return {
      selectedPayload : {},
      selactiveTab : 0,
      downloadlink : ''
    };
  },
  computed: {
      ...mapState('gui/crest', ['selectedTag', 'selectedIov','selectedPayloadLink']),
      ...mapGetters('db/payloads', ['getPayload']),
      selectDoc() {
          if (this.selectedIov !== "") {
              this.download2();
              return true;
          }
          return false;
      }

  },
  methods: {
      ...mapActions('db/payloads', ['fetchPayloadMeta','fetchPayload']),
      loadMetadata() {
          this.fetchPayloadMeta(this.selectedIov).then(() => {
              (this.selectedPayload = this.getPayload[this.selectedIov])
          });
      },
      download2() {
          this.downloadlink = this.selectedPayloadLink;
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
