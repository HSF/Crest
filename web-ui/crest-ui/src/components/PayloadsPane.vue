<template>
<div class="container">
  <p class="has-text-info is-size-2">Payload information</p>
  <nav class="level">
      <div class="level is-mobile">
        <div class="level-left">
          <div class="level-item">
            <HelpInfoPane v-bind:helpmessage="helpmsg"
              v-bind:infomessage="infomsg"
              v-bind:notifytext="notifytext"
              v-bind:notiftype="notiftype"
              v-bind:links="flinks"
              v-on:child-switchtab="selectTab"/>
          </div>
      </div>
    </div>
  </nav>
    <div class="columns">
    <div class="column is-one-fifth">
      <b-field grouped>
      <p class="control">
        <button class="button is-primary" v-on:click="loadMetadata()">
          <b-icon icon="eye"></b-icon>
          <span>Meta</span>
        </button>
      </p>
      <p class="control">
        <button class="button is-primary" @click="show()">Download</button>
<!--
        <a v-bind:href="downloadlink" v-show="selectDoc">
          <b-icon icon="download"></b-icon>
          <span>Download</span>
        </a>
      -->
      <!--  <button class="button is-primary" v-on:click="download()">Download</button> -->
      </p>
      </b-field>
    </div>
    <div class="column is-four-fifths">
      <b-message  type="is-info">
      <div class="content">
      <ul id="pyld-info">
        <li v-for="(val,key) in selectedPayload">
          {{ key }} : {{val}}
        </li>
      </ul>
      </div>
      </b-message>
    </div>
    </div>
</div>
</template>
<script>
import axios from 'axios';
import HelpInfoPane from './HelpInfoPane.vue';

export default {
  name: 'PayloadsPane',
  props : {
    selectedtag : Object,
    selectediov : Object,
    selectedserver : String,
  },
  data: function () {
    return {
      flinks: [
        {'btnlabel' : 'Get Iovs', 'seltab' : 1}
      ],
      helpmsg: "<p>Get payload information on the selected IOV.</p>"
        +"<p>Click on <b>Metadata</b> to get meta data informations.</p>"
        +"<p>You can use the <b>Dowload</b> link to view or download the file.</p>",
      notiftype : 'is-info',
      notifytext : 'Searching payloads....',
      selectedPayload : {},
      selactiveTab : 0,
      dowloadlink : ''
    };
  },
  computed: {
    "payloadmeta": function loadPayloadmeta() {
      if (typeof this.selectediov.payloadHash === "undefined") {
        return {};
      } else {
      axios({
        url: `${this.hostbaseurl}/payloads/`+this.selectediov.payloadHash+'/meta',
        method: 'GET',
      }).then((response) => {
        (this.selectedPayload = response.data); return response.data;
      }).catch(error => { console.error(error); return Promise.reject(error); });
    }},
    selectDoc() {
      if (this.selectediov.payloadHash !== "") {
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
        +"<br> Selected tag is : "+this.selectedtag.name
        +"<br> Selected iov is : "+this.selectediov.since+" hash=>"+this.selectediov.payloadHash;
    }
  },
  methods: {
    selectTab(nt) {
      console.log('Selecting tab '+nt)
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    },
    async download() {
      axios({
        url: `${this.hostbaseurl}/payloads/`+this.selectediov.payloadHash,
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
    },
    async loadMetadata() {
      axios({
        url: `${this.hostbaseurl}/payloads/`+this.selectediov.payloadHash+'/meta',
        method: 'GET',
      }).then((response) => {
        (this.selectedPayload = response.data)
      });
    },
    download2() {
			this.downloadlink = this.hostbaseurl + '/payloads/'+this.selectediov.payloadHash;
		},
    show() {
      if (this.selectDoc) {
        window.open(this.downloadlink, "_blank");
      }
    }
  },
  components: {
    HelpInfoPane
  }
};
</script>
