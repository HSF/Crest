<template>
<div class="container">
<div class="notification">
    Search for Iovs. Use the tag selected on the Tags tab.
    Access api on {{selectedserver.host}}:{{selectedserver.port}}<br>
    Selected iov is : {{selectedIov}}<br>
    Selected tag is : {{tagname}}<br>
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
  <b-field label="Since">
    <b-input v-model="since" maxlength="20"></b-input>
  </b-field>
  <b-field label="Until">
    <b-input v-model="until" maxlength="20"></b-input>
  </b-field>
  <b-field label="Tag">
      <b-input v-model="tagname" placeholder="Tag selection on another tab" disabled></b-input>
  </b-field>
  <b-field>
  <p class="control">
    <button class="button is-primary" v-on:click="loadIovs()" :disabled="radioButton !== 'Search'">Search</button>
  </p>
  </b-field>
  <b-field>
  <p class="control">
    <button class="button is-info" v-on:click="gotoPayloads()">Payload Info</button>
  </p>
  </b-field>
</div>
<div class="column is-four-fifths">
  <div v-if="radioButton === 'Search'">
    <GenericTable v-bind:data="rows" v-bind:columns="columns" v-bind:sortcolumn="since" v-on:select-row="updateHash"/>
  </div>
  <div v-else>
    <IovForm v-bind:selectedserver="selectedserver"/>
  </div>
</div>
</div>
</div>
</template>

<script>
import GenericTable from './GenericTable.vue'
import IovForm from './IovForm.vue'
import axios from 'axios';

export default {
  name: 'IovsPane',
  props : {
    tagname : '',
    selectedserver : Object,
  },
  data: function () {
    return {
    isFullPage : false,
    isLoading : false,
    selectedIov : {},
        radioButton : 'Search',
        since : 0,
        until : 'INF',
        rows: [],
        snapshot : '0',
        selactiveTab : 3,
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
                    sortable: true
                },
                {
                    field: 'insertionTime',
                    label: 'Insert Time',
                    sortable: true
                },
                {
                    field: 'payloadHash',
                    label: 'HASH',
                    width: '60'
                },
            ],
        thehash: ''};
  },
  methods: {
    gotoPayloads() {
      this.selactiveTab = 3
      this.$emit('select-tab', this.selactiveTab)
    },
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
      this.isLoading = true
      setTimeout(() => {
          this.isLoading = false
      }, 10 * 1000)
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')

      const params = [
      `tagname=${this.tagname}`,
      `since=${this.since}`,
      `until=${this.until}`,
      `snapshot=${this.snapshot}`,
      ].join('&')

      axios
        .get(`http://${hostname}/${this.apiName}/iovs/selectIovs?${params}`)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
    }
  },
  mounted: function() {
//      console.log('existing rows '+this.rows);
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
      const params = [
      `page=0`,
      `size=100`,
      `sort=id.since:DESC`,
      ].join('&')
      axios
        .get(`http://${hostname}/${this.apiName}/iovs?${params}`)
        .then(response => (this.rows = response.data))
        .catch(error => { console.error(error); return Promise.reject(error); });
  },
  components: {
    GenericTable,
    IovForm
  }
};
</script>
