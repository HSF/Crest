<template>
  <section>
    <p>Number of rows: {{ numrows }}
    </p>
    <b-tabs>
      <b-tab-item label="Table">
        <b-field label="Search Tag by name">
          <b-autocomplete
            rounded
            v-model="thetag"
            :data="filteredDataArray"
            placeholder="e.g. MuonAlign"
            icon="magnify">
            <template slot="empty">No results found</template>
          </b-autocomplete>
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
            :data="globaltagfiltereddata"
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
              <b-table-column field="insertionTime" label="Insert Time" centered>
                  <span class="tag is-success">
                      {{ (props.row.insertionTime) }}
                  </span>
              </b-table-column>
              <b-table-column label="Get tags" centered>
                 <a class="tag is-info" style="text-decoration:none;" @click="goTags(props.row.name)">Get tags</a>
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
    name: 'CrestGlobalTagsTable',
    props : {
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
                      label: 'Global tag name',
                      width: '40',
                      visible: true,
                      sortable: true
                  },
                  {
                      field: 'validity',
                      label: 'Validity',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'description',
                      label: 'Description',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'release',
                      label: 'Release',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'scenario',
                      label: 'Scenario',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'workflow',
                      label: 'Workflow',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'type',
                      label: 'Type',
                      visible: true,
                      sortable: false,
                  },
              ],
          thetag: '',
      }
    },
    methods: {
        ...mapActions('db/globaltags', ['fetchGlobalTagsByName']),
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
          this.$store.commit('gui/crest/selectGlobalTag', row.name);
      },
      goTags(globaltagname) {
          this.$emit('select-tag', 1);
          this.$store.commit('gui/crest/selectGlobalTag', globaltagname);
      }
    },
    computed: {
        ...mapState('gui/crest', ['selectedGlobalTag']),
        ...mapGetters('db/globaltags', ['getGlobalTaglist']),
      numrows () {
        return (!this.globaltaglist ? -1 : this.globaltaglist.length)
      },
      globaltaglist: function() {
        return this.getGlobalTaglist;
      },
      tagnames() {
          let result = this.globaltaglist.map(a => a.name);
          return result
      },
      filteredDataArray() {
          return this.tagnames.filter((option) => {
              return option
                  .toString()
                  .toLowerCase()
                  .indexOf(this.thetag.toLowerCase()) >= 0
          })
      },
      globaltagfiltereddata: {
          get: function() {
            return this.globaltaglist.filter(row => (row.name.includes(this.thetag) ))
          }
      }
    },
    watch: {
        selectedGlobalTag: function() {
            this.fetchGlobalTagsByName(this.selectedGlobalTag);
        }
    },
    created() {
        this.fetchGlobalTagsByName('');
    }
  }

</script>
