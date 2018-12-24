<template>
<div class="container is-fluid">
    <div class="notification">
        Search for Tags.
        Access api on {{apiHost}}:{{apiPort}}<br>
        Selected tag is : {{selectedTag}}<br>
        <p class="content">
            <b>Selection mode:</b>
            {{ radioButton }}
        </p>
    </div>
    <div class="columns is-mobile">
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
      </div>
      <div class="column" v-if="radioButton === 'Search'">
        <b-field label="Search Tag">
            <b-autocomplete
                rounded
                v-model="thetag"
                :data="filteredDataArray"
                placeholder="e.g. TEST"
                icon="magnify"
                @select="option => selected = option">
                <template slot="empty">No results found</template>
            </b-autocomplete>
        </b-field>
        <b-field>
        <p class="control">
          <button class="button is-primary" v-on:click="loadTags()">Search</button>
        </p>
        </b-field>
      </div>
      <div class="column" v-else>
        <b-field label="Tag Name">
          <b-input v-model="savedTag.name"></b-input>
        </b-field>
        <b-field label="Tag Description">
          <b-input v-model="savedTag.description"></b-input>
        </b-field>
        <b-field label="Object Type">
          <b-input v-model="savedTag.objectType"></b-input>
        </b-field>
        <b-field label="End Of Validity">
          <b-input v-model="savedTag.endOfValidity"></b-input>
        </b-field>
        <b-field label="Last Validation Time">
          <b-input v-model="savedTag.lastValidatedTime"></b-input>
        </b-field>
        <b-field label="Since Type">
            <b-select v-model="savedTag.timeType" placeholder="Select a time type">
                <optgroup label="time in milliseconds">
                    <option value="time">time</option>
                </optgroup>

                <optgroup label="run or lumi">
                    <option value="run-lumi">run-lumi</option>
                    <option value="run">run</option>
                    <option value="run-event">run-event</option>
                </optgroup>
            </b-select>
        </b-field>
        <b-field label="Synchro Type">
            <b-select v-model="savedTag.synchronization" placeholder="Select a synchronization type">
                <optgroup label="general">
                    <option value="all">all</option>
                </optgroup>

                <optgroup label="online">
                    <option value="upd1">upd1</option>
                    <option value="es">es</option>
                    <option value="hlt">hlt</option>
                </optgroup>
                <optgroup label="offline">
                    <option value="upd4">upd4</option>
                    <option value="cool">cool</option>
                </optgroup>
            </b-select>
        </b-field>
        <b-field>
            <p class="control">
              <button class="button is-primary" v-on:click="save()">Save</button>
            </p>
        </b-field>
      </div>
  </div>
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
        savedTag : { name : '',
                     timeType : '',
                     description : '' ,
                     objectType : '',
                     synchronization : 'all',
                     lastValidatedTime: 0,
                     endOfValidity: 0},
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
  save() {
    console.log('saving a tag '+this.savedTag.name+' '+this.savedTag.timeType+' '+this.savedTag.description)
    const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      axios({
        url: `http://${hostname}/crestapi/tags`,
        method: 'post',
        data: this.savedTag
      })
      .then(function (response) {
          // your action after success
          console.log(response);
      })
      .catch(function (error) {
         // your action on error success
          console.log(error);
      });
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
    GenericTable
  }
};
</script>
