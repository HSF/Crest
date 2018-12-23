<template>
<div class="container is-widescreen">
  <div class="notification">
    Search for Tags.
    Access api on {{apiHost}}:{{apiPort}}<br>
    Selected tag is : {{selectedTag}}<br>
  <b-field grouped>
      <b-input v-model="thetag" placeholder="Search..."></b-input>
      <p class="control">
        <button class="button is-primary" v-on:click="loadTags()">Search</button>
      </p>
  </b-field>
  </div>
  <GenericTabData v-bind:data="rows" v-bind:columns="columns" v-on:select-row="updateTag"/>
</div>
</template>

<script>
import GenericTabData from './GenericTabData.vue'
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
    GenericTabData
  }
};
</script>
