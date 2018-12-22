<template>
<div class="container is-widescreen">
  <div class="notification">
    This container is <strong>fluid</strong> until widescreen: it will have a 32px gap on either side, on any
    viewport size.
    Search for APIDs or other.
    Access api on {{apiHost}}:{{apiPort}}<br>
    Selected iov is : {{selectedIov}}<br>
    Selected tag is : {{tagname}}
  </div>
  <b-field grouped>
      <b-input v-model="tagname" placeholder="Search..."></b-input>
      <p class="control">
        <button class="button is-primary" v-on:click="loadIovs()">Search</button>
      </p>
  </b-field>
  <GenericTable v-bind:data="rows" v-bind:columns="columns" v-on:select-row="updateHash"/>
</div>
</template>

<script>
import GenericTable from './GenericTable.vue'
import axios from 'axios';

export default {
  name: 'IovsPane',
  props : {
    tagname : '',
  },
  data: function () {
    return {
        selectedIov : {},
        rows: [],
        columns : [
                {
                    field: 'tagName',
                    label: 'TAG NAME',
                    width: '20',
                },
                {
                    field: 'since',
                    label: 'SINCE',
                    width: '15',
                },
                {
                    field: 'insertionTime',
                    label: 'Insert Time',
                },
                {
                    field: 'payloadHash',
                    label: 'HASH',
                    width: '50'
                },
            ],
        thehash: ''};
  },
  methods: {
    updateHash(row) {
      this.selectedIov = row
      this.thehash = row.payloadHash
      this.$emit('select-iov', this.selectedIov)
    },
    async printRows() {
      for (var i in this.rows) {
        console.log(this.rows[i]);
      }
    },
    async loadIovs() {
      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      const params = [
          `tagname=${this.tagname}`,
      ].join('&')

      axios
        .get(`http://${hostname}/crestapi/iovs/selectIovs?${params}`)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
    }
  },
  mounted: function() {
//      console.log('existing rows '+this.rows);
      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      const params = [
          `tagname=${this.tagname}`,
      ].join('&')
      axios
        .get(`http://${hostname}/crestapi/iovs/selectIovs?${params}`)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
  },
  components: {
    GenericTable
  }
};
</script>
