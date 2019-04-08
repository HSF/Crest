<template>
<div class="container">
<div class="notification">
    Search for Iovs. Use the tag selected on the Tags tab.
    Access api on {{hostbaseurl}}<br>
    Selected iov is : {{selectedIov}}<br>
    Selected tag is : {{selectedtag.name}}<br>
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
      <b-input v-model="selectedtag.name" placeholder="Tag selection on another tab" disabled></b-input>
  </b-field>
  <b-field>
  <p class="control">
    <button class="button is-primary" v-on:click="loadIovs()" :disabled="radioButton !== 'Search'">Search</button>
  </p>
  </b-field>
</div>
<div class="column is-four-fifths">
  <div v-if="radioButton === 'Search'">
  <b-field>
  <p class="control">
    <button class="button is-info" v-on:click="gotoPayloads()">Payload Info</button>
  </p>
  </b-field>
    <CrestIovsTable v-bind:data="rows" v-bind:selectedtag="selectedtag" v-bind:isloading="isLoading" v-on:select-row="updateHash"/>
  </div>
  <div v-else>
    <IovForm v-bind:selectedserver="selectedserver" v-bind:selectedtag="selectedtag"/>
  </div>
</div>
</div>
</div>
</template>

<script>
import CrestIovsTable from './CrestIovsTable.vue'
import IovForm from './IovForm.vue'
import axios from 'axios';

export default {
  name: 'IovsPane',
  props : {
    selectedtag : Object,
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
        selactiveTab : 2,
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
      this.selactiveTab = 2
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
//      setTimeout(() => {
//          this.isLoading = false
//      }, 10 * 1000)
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
//      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')

      const params = [
      `tagname=${this.selectedtag.name}`,
      `since=${this.since}`,
      `until=${this.until}`,
      `snapshot=${this.snapshot}`,
      ].join('&')

      axios
        .get(`${this.hostbaseurl}/iovs/selectIovs?${params}`)
        .then(response => {this.isLoading = false; (this.rows = response.data)})
        .catch(error => { console.error(error); this.isLoading = false; return Promise.reject(error); });
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
      isLoadingData () {
        if (this.rows.length <= 0 && this.isLoading) {
          return true;
        } else {
          this.isLoading = false;
          return false;
        }
      }
  },
  components: {
    CrestIovsTable,
    IovForm
  }
};
</script>
