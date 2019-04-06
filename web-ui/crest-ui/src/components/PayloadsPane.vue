<template>
<section>
<div class="container is-widescreen"">
  <div class="notification">
    Download or show information on Payload.
    Access api on {{hostbaseurl}}<br>
    Selected iov is : {{selectedIov}}<br>
    Selected tag is : {{tagname}}<br>
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
    <button class="button is-primary" v-on:click="loadMetadata()">Info</button>
  </p>
  <p class="control">
    <button class="button is-primary" v-on:click="download()">Download</button>
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
  tagname : '',
  selectedIov : Object,
  selectedserver : Object,
  },
  data: function () {
    return {
        thehash: '',
        selectedPayload : {}
    };
  },
  computed: {
    "payloadmeta": function loadPayloadmeta() {
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
//      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
      if (typeof this.selectedIov.payloadHash === "undefined") {
        return {};
      } else {
      axios({
        url: `${this.hostbaseurl}/payloads/`+this.selectedIov.payloadHash+'/meta',
        method: 'GET',
      }).then((response) => {
        (this.selectedPayload = response.data); return response.data;
      }).catch(error => { console.error(error); return Promise.reject(error); });
    }}
  },
  methods: {
    async download() {
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
//      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
      axios({
        url: `${this.hostbaseurl}/payloads/`+this.selectedIov.payloadHash,
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
        url: `${this.hostbaseurl}/payloads/`+this.selectedIov.payloadHash+'/meta',
        method: 'GET',
      }).then((response) => {
        (this.selectedPayload = response.data)
      });
    }
  },
  computed: {
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
  components: {
  }
};
</script>
