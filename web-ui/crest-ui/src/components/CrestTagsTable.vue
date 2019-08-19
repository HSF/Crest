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
            :data="tagfiltereddata"
            detailed
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
              <b-table-column field="niovs" label="Get iovs" centered>
                 <a class="tag is-info" style="text-decoration:none;" @click="goIovs(props.row.name)">Get {{ props.row.niovs }} iovs</a>
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
    name: 'CrestTagsTable',
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
      }
    },
    methods: {
        ...mapActions('db/tags', ['fetchTagByName']),
        ...mapActions('db/iovs', ['countIovsByTag']),
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
      countIovs(tagname) {
          this.countIovsByTag(tagname);
      },
      goIovs(tagname) {
          this.$emit('select-tag', 1);
          this.$store.commit('gui/crest/selectTag', tagname);
      },
      count(tagname) {
          let niovs = 0;
          this.countIovs(tagname);
          const nb_iovs = Object.entries(this.nb_iovs_for_tag);
          for (var i = 0; i < nb_iovs.length; i++){
              if (nb_iovs[i][0] == tagname) {
                  if (nb_iovs[i][1]) {
                      niovs = nb_iovs[i][1].niovs;
                  } else {
                      niovs = 0;
                  }
              }
          }
          return niovs;
      }
    },
    computed: {
        ...mapState('gui/crest', ['selectedTag']),
        ...mapGetters('db/tags', ['getTag','getTaglist']),
        ...mapState('db/iovs', ['nb_iovs_for_tag']),
      numrows () {
        return (!this.taglist ? -1 : this.taglist.length)
      },
      taglist: function() {
        return this.getTaglist;
      },
      tagnames() {
          let result = this.taglist.map(a => a.name);
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
      tagfiltereddata: {
          get: function() {
            return this.taglist.filter(row => (row.name.includes(this.thetag) ))
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
    }
  }

</script>
