<template>
  <section>
    <p>Number of rows: {{ numrows }}
    </p>
    {{ loadTags }}
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
          data: ''
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
      loadAllTags() {
          let liste_tags = [];
          const tag = Object.entries(this.getTag);
          for (var i = 0; i < tag.length; i++){
              liste_tags.push(tag[i][1]);
              this.countIovs(tag[i][1].name);
          }
          this.data = liste_tags;      
      },
      countIovs(tagname) {
          this.countIovsByTag(tagname);
      },
      goIovs(tagname) {
          this.$emit('select-tag', 1);
          this.$store.commit('gui/crest/selectTag', tagname);
      }
    },
    computed: {
        ...mapState('gui/crest', ['selectedTag']),
        ...mapGetters('db/tags', ['getTag']),
        ...mapState('db/iovs', ['nb_iovs_for_tag']),
      numrows () {
        return (!this.data ? -1 : this.data.length)
      },
      tagnames() {
          let result = this.data.map(a => a.name);
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
            return this.data.filter(row => (row.name.includes(this.thetag) ))
          }
      },
      loadTags() {
          if (this.selectedTag == "") {
              let liste_tags = [];
              const tag = Object.entries(this.getTag);
              for (var i = 0; i < tag.length; i++){
                  this.countIovs(tag[i][1].name);
                  const nb_iovs = Object.entries(this.nb_iovs_for_tag);
                  for (var j = 0; j < nb_iovs.length; j++){
                      if (nb_iovs[j][0] == tag[i][1].name) {
                          if (nb_iovs[j][1]) {
                              tag[i][1]['niovs'] = nb_iovs[j][1].niovs;
                          } else {
                              tag[i][1]['niovs'] = 0;
                          }
                      }
                  }
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
