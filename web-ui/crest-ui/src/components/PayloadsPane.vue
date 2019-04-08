<template>
<section>
<div class="container is-widescreen">
  <div class="notification">
    Download or show information on Payload.
    Access api on {{hostbaseurl}}<br>
    Selected iov is : {{selectediov}}<br>
    Selected tag is : {{selectedtag.name}}<br>
    Selected payload is : {{selectedPayload}}<br>

    <b-message  type="is-info">
    <div class="content">
    <ul id="pyld-info">
      <li v-for="(val,key) in selectedPayload">
        {{ key }} : {{val}}
      </li>
    </ul>
    </div>
    </b-message>

  <b-field grouped>
  <p class="control">
    <button class="button is-primary" v-on:click="loadMetadata()">
      <b-icon icon="eye"></b-icon>
      <span>Info</span>
    </button>
  </p>
  <p class="control">
    <a v-bind:href="downloadlink" v-show="selectDoc">
      <b-icon icon="download"></b-icon>
      <span>Download</span>
    </a>
  <!--  <button class="button is-primary" v-on:click="download()">Download</button> -->
  </p>
  </b-field>
  </div>
</div>
</section>
</template>

<script>
import axios from 'axios';

export default {
  name: 'PayloadsPane',
  props : {
  selectedtag : Object,
  selectediov : Object,
  selectedserver : Object,
  },
  data: function () {
    return {
        selectedPayload : {},
        dowloadlink : ''
    };
  },
  computed: {
    "payloadmeta": function loadPayloadmeta() {
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
//      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
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
      if (this.selectedserver.url !== "") {
        return this.selectedserver.url;
      }
      const selprotocol = this.selectedserver.protocol.toLowerCase();
      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':');
      var burl = `${selprotocol}://${hostname}/crestapi`;
      return burl;
    },
  },
  methods: {
    async download() {
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
//      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
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
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
//      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
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
  },
  components: {
  }
};
</script>
