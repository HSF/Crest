<template>
  <section>
    <p>Number of rows: {{ numrows }}
    </p>
    {{ loadTags }}
    <b-tabs>
      <b-tab-item label="Table">
        <b-field grouped group-multiline>
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
         <!-- <b-field label="Search Tag by name">
            <b-input v-model="thetag" placeholder="e.g. MuonAlign" ></b-input>
          </b-field>-->
          <b-field label="Selection">
            <button class="button field is-danger" @click="selected = {}" :disabled="!selected">
              <b-icon icon="close"></b-icon>
              <span>Clear</span>
            </button>
          </b-field>
        </b-field>
        <b-field grouped group-multiline>
            <div v-for="(column, index) in columns"
                :key="index"
                class="control">
                <b-checkbox v-model="column.visible">
                    {{ column.label }}
                </b-checkbox>
            </div>
        </b-field>

        <b-table
            :data="tagfiltereddata"
            :paginated="isPaginated"
            :per-page="perPage"
            :current-page.sync="currentPage"
            :pagination-simple="isPaginationSimple"
            :default-sort-direction="defaultSortDirection"
            :selected.sync="selected"
            default-sort="name"
            @click="onClick"
            :loading="isloading">
            <template slot-scope="props">
              <b-table-column v-for="(column, index) in columns"
                  :key="index"
                  :label="column.label"
                  :visible="column.visible"
                  :field="column.field"
                  sortable>
                  {{ props.row[column.field] }}
              </b-table-column>
              <b-table-column field="timeType" label="Time Type" centered>
                  <span class="tag is-primary">
                      {{ props.row.timeType }}
                  </span>
              </b-table-column>
              <b-table-column field="insertionTime" label="Insert Time" centered>
                  <span class="tag is-success">
                      {{ (props.row.insertionTime) }}
                  </span>
              </b-table-column>
            </template>
            <template slot="empty">
                <section class="section">
                    <div class="content has-text-grey has-text-centered">
                        <p>
                            <b-icon
                                icon="emoticon-sad"
                                size="is-large">
                            </b-icon>
                        </p>
                        <p>Nothing here.</p>
                    </div>
                </section>
            </template>
        </b-table>
        </b-tab-item>
        <b-tab-item label="Selected">
            <pre>{{ selected }}</pre>
        </b-tab-item>
    </b-tabs>
  </section>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex'
  import Long from 'long';

  export default {
    name: 'CrestTagsTable',
    props : {
     // data : Array,
      isloading : Boolean
    },
    data: function() {
      return {
          isPaginated: true,
          isPaginationSimple: true,
          defaultSortDirection: 'asc',
          currentPage: 1,
          perPage: 10,
          selected: {},
          columns : [
                  {
                      field: 'name',
                      label: 'TAG NAME',
                      width: '40',
                      visible: true,
                      sortable: true
                  },
                  {
                      field: 'description',
                      label: 'Description',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'payloadSpec',
                      label: 'Object Type',
                      visible: true,
                      sortable: true
                  },
              ],
          thetag: '',
          data: ''
      }
    },
    methods: {
        ...mapActions('db/tags', ['fetchTagByName']),
      timestr (atime) {
        if (!atime) {
          return 'none'
        }
        if (atime === '0') {
          return '0'
        }
        if (this.selectedtag.timeType === 'time')
          return new Date(atime/1000000).toUTCString()
        if (this.selectedtag.timeType.startsWith('run')) {
          var coolt = Long.fromString(atime.toString(),10);
          var run = coolt.getHighBitsUnsigned()
          var lumi = coolt.getLowBits();
          return run.toString()+'-'+lumi.toString();
        }
      },
      onClick(row) {
        this.$store.commit('gui/crest/selectTag', row.name);
      },
      loadAllTags() {
            let liste_tags = [];
            const tag = Object.entries(this.getTag);
            for (var i = 0; i < tag.length; i++){
                liste_tags.push(tag[i][1]);
            }
            this.data = liste_tags;      
        }
    },
    computed: {
        ...mapState('gui/crest', ['selectedTag']),
        ...mapGetters('db/tags', ['getTag']),
      numrows () {
        return (!this.data ? -1 : this.data.length)
      },
      tagnames() {
          let result = this.data.map(a => a.name);
          return result
      },
      filteredDataArray() {
          return this.tagnames.filter((option) => {
              //console.log('filtering '+option)
              return option
                  .toString()
                  .toLowerCase()
                  .indexOf(this.thetag.toLowerCase()) >= 0
          })
      },
      tagfiltereddata: {
          get: function() {
            return this.data.filter(row => (row.name.includes(this.thetag) ))
          }
      },
      loadTags() {
          if (this.selectedTag == "") {
              let liste_tags = [];
              const tag = Object.entries(this.getTag);
              for (var i = 0; i < tag.length; i++){
                  liste_tags.push(tag[i][1]);
              }
              this.data = liste_tags;      
          }
      }
    },
    watch: {
        selectedTag: function() {
            this.fetchTagByName(this.selectedTag);
        }
    },
    created() {
        this.fetchTagByName('');
        this.loadAllTags();
    }
  }

</script>
