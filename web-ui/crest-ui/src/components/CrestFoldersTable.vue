<template>
  <section>
    <p>Number of rows: {{ numrows }}
    </p>
    <b-tabs>
      <b-tab-item label="Table">
        <b-field label="Search Folder by node full path">
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
            :data="folderfiltereddata"
            :paginated="isPaginated"
            :per-page="perPage"
            :current-page.sync="currentPage"
            :pagination-simple="isPaginationSimple"
            :default-sort-direction="defaultSortDirection"
            :selected.sync="selected"
            default-sort="nodeFullpath"
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
            </template>
            <template slot="detail" slot-scope="props">
              <span>{{ count(props.row.name) }} iovs</span>
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
    name: 'CrestFoldersTable',
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
                      field: 'nodeFullpath',
                      label: 'Full path',
                      width: '40',
                      visible: true,
                      sortable: true
                  },
                  {
                      field: 'schemaName',
                      label: 'Schema name',
                      visible: true,
                      sortable: false,
                  },
                  {
                      field: 'nodeName',
                      label: 'Name',
                      visible: true,
                      sortable: true
                  },
                  {
                      field: 'nodeDescription',
                      label: 'Description',
                      visible: true,
                      sortable: true
                  },
                  {
                      field: 'tagPattern',
                      label: 'Tag pattern',
                      visible: true,
                      sortable: true
                  },
                  {
                      field: 'groupRole',
                      label: 'Group role',
                      visible: true,
                      sortable: true
                  },
              ],
          thetag: '',
      }
    },
    methods: {
        ...mapActions('db/folders', ['fetchFolder']),
    },
    computed: {
        ...mapGetters('db/folders', ['getFolderlist']),
      numrows () {
        return (!this.folderlist ? -1 : this.folderlist.length)
      },
      folderlist: function() {
          return this.getFolderlist;
      },
      foldernames() {
          let result = this.folderlist.map(a => a.nodeFullpath);
          return result
      },
      filteredDataArray() {
          return this.foldernames.filter((option) => {
              return option
                  .toString()
                  .toLowerCase()
                  .indexOf(this.thetag.toLowerCase()) >= 0
          })
      },
      folderfiltereddata: {
          get: function() {
            return this.folderlist.filter(row => (row.nodeFullpath.includes(this.thetag) ))
          }
      }
    },
    watch: {
    },
    created() {
        this.fetchFolder();
    }
  }

</script>
