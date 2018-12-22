<template>
<div class="container is-widescreen">
  <div class="notification">
    This container is <strong>fluid</strong> until widescreen: it will have a 32px gap on either side, on any
    viewport size.
    Search for APIDs or other.
    Access api on {{apiHost}}:{{apiPort}}
    Selected tag is : {{selectedTag}}
  </div>
  <b-field grouped>
      <b-input v-model="thetag" placeholder="Search..."></b-input>
      <p class="control">
        <button class="button is-primary" v-on:click="loadTags()">Search</button>
      </p>
  </b-field>
  <GenericTable v-bind:data="rows" v-bind:columns="columns" v-on:select-row="updateTag"/>
</div>
</template>

<script>
import GenericTable from './GenericTable.vue'
import axios from 'axios';

export default {
  name: 'TagsPane',
  props : {
  },
  data: function () {
    return {
        selectedTag : {},
        rows: [],
        columns : [
                {
                    field: 'name',
                    label: 'TAG NAME',
                    width: '40',
                },
                {
                    field: 'timeType',
                    label: 'Type',
                    width: '15',
                },
                {
                    field: 'description',
                    label: 'Description',
                },
                {
                    field: 'objectType',
                    label: 'Object Type',
                },
            ],
        thetag: '%'};
  },
  methods: {
    updateTag(stag) {
      this.selectedTag = stag
      this.thetag = stag.name
      this.$emit('select-tag', this.thetag)
    },
    async printRows() {
      for (var i in this.rows) {
        console.log(this.rows[i]);
      }
    },
    async loadTags() {
      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      const params = [
          `by=name:${this.thetag}`,
      ].join('&')

      axios
        .get(`http://${hostname}/crestapi/tags?${params}`)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
    }
  },
  mounted: function() {
//      console.log('existing rows '+this.rows);
      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      axios
        .get(`http://${hostname}/crestapi/tags`)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
  },
  components: {
    GenericTable
  }
};
</script>
