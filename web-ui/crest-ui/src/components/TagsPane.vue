<template>
<div class="container">
    <div class="notification">
        Search for Tags.
        Access api on {{apiHost}}:{{apiPort}}<br>
        Selected tag is : {{selectedTag}}<br>
        <p class="content">
            <b>Selection mode:</b>
            {{ radioButton }}
        </p>
    </div>
    <div class="columns">
      <div class="column is-one-fifth">
          <b-field>
            <b-radio-button v-model="radioButton"
                native-value="Search"
                type="is-info">
                <b-icon icon="magnify"></b-icon>
                <span>Search</span>
            </b-radio-button>
            </b-field>

            <b-field>
            <b-radio-button v-model="radioButton"
                native-value="Create"
                type="is-success">
                <b-icon icon="check"></b-icon>
                <span>Create</span>
            </b-radio-button>
        </b-field>
        <b-field label="Search Tag by name">
            <b-autocomplete
                rounded
                v-model="thetag"
                :data="filteredDataArray"
                placeholder="e.g. MuonAlign"
                icon="magnify"
                @select="option => selected = option">
                <template slot="empty">No results found</template>
            </b-autocomplete>
        </b-field>
        <b-field>
        <p class="control">
          <button class="button is-primary" v-on:click="loadTags()" :disabled="radioButton !== 'Search'">Search</button>
        </p>
        </b-field>
      </div>
      <div class="column is-four-fifths">
        <div v-if="radioButton === 'Search'">
          <GenericTable v-bind:data="rows" v-bind:columns="columns" v-on:select-row="updateTag"/>
        </div>
        <div v-else>
          <TagForm />
        </div>
      </div>
    </div>
</div>
</template>

<script>
import GenericTable from './GenericTable.vue'
import TagForm from './TagForm.vue'

import axios from 'axios';

export default {
  name: 'TagsPane',
  props : {
  },
  data: function () {
    return {
        selectedTag : {},
        radioButton : 'Search',
        rows: [],
        columns : [
                {
                    field: 'name',
                    label: 'TAG NAME',
                    width: '40',
                    sortable: true
                },
                {
                    field: 'timeType',
                    label: 'Type',
                    width: '15',
                    sortable: true
                },
                {
                    field: 'description',
                    label: 'Description',
                },
                {
                    field: 'objectType',
                    label: 'Object Type',
                },
                {
                    field: 'insertionTime',
                    label: 'Insert Time',
                    sortable: true
                },
            ],
        selected: null,
        thetag: ''};
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
  computed: {
      tagnames() {
        let result = this.rows.map(a => a.name);
        return result
      },
      filteredDataArray() {
          return this.tagnames.filter((option) => {
              console.log('filtering '+option)
              return option
                  .toString()
                  .toLowerCase()
                  .indexOf(this.thetag.toLowerCase()) >= 0
          })
      }
  },
  components: {
    GenericTable,
    TagForm
  }
};
</script>
