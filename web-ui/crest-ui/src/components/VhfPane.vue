<template>
<div class="container is-widescreen">
  <div class="notification">
    This container is <strong>fluid</strong> until widescreen: it will have a 32px gap on either side, on any
    viewport size.
    Search for VHF.
  </div>
  <b-field grouped>
      <b-input v-model="theframe" placeholder="Search..."></b-input>
      <p class="control">
        <button class="button is-primary" v-on:click="loadVhf()">Search</button>
      </p>
  </b-field>
  <SvomTable v-bind:data="rows" v-bind:columns="columns"/>
</div>
</template>

<script>
import SvomTable from './SvomTable.vue'
import axios from 'axios'

export default {
  name: 'VhfPane',
  props : {
  },
  data: function () {
    return {
        rows: [],
        columns : [
                {
                    field: 'frameId',
                    label: 'ID',
                    width: '40',
                    numeric: true
                },
                {
                    field: 'receptionTime',
                    label: 'Reception Time',
                },
                {
                    field: 'isFrameValid',
                    label: 'Valid',
                },
                {
                    field: 'isFrameDuplicate',
                    label: 'Duplicate',
                    centered: true
                },
                {
                    field: 'dupFrameId',
                    label: 'Dup ID',
                }
            ],
        theframe: '0'};
  },
  methods: {
    async printRows() {
      for (var i in this.rows) {
        console.log(this.rows[i]);
      }
    },
    async loadVhf() {
      axios
        .get('http://localhost:8080/api/vhf?by=id:'+this.theframe)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
    }
  },
  mounted: function() {
//      console.log('existing rows '+this.rows);
      axios
        .get('http://localhost:8080/api/vhf?size=10')
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
  },
  components: {
    SvomTable
  }
};
</script>
