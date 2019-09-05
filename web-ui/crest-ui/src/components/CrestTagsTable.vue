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
            :checkable="globalTagMap"
            :checked-rows.sync="checkedRows"
            :is-row-checkable="record"
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
              <div class="content">
                <span>{{ count(props.row.name) }} iovs</span>
                <b-tabs v-model="activeTab">
                  <b-tab-item label="Meta" v-on:select-tab="selActive">
                    <ul id="tagMeta">
                      <li v-for="(val,key) in detailsTag(props.row.name)" v-bind:key="key">
                        {{ key }} : {{ val }}
                      </li>
                    </ul>
                  </b-tab-item>
                  <b-tab-item label="Global tags" v-on:select-tab="selActive">
                  <b-table
                  :data="globaltags(props.row.name)"
                  :paginated="isPaginated"
                  :per-page="perPage"
                  :current-page.sync="currentPage"
                  :pagination-simple="isPaginationSimple"
                  :default-sort-direction="defaultSortDirection"
                  :selected.sync="selected"
                  default-sort="name"
                  :loading="isloading">
                  <template slot-scope="props">
                    <b-table-column v-for="(column, index) in columnsGlobalTag"
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
                  </template>
                  </b-table>
                  </b-tab-item>
                </b-tabs>
              </div>
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
      isloading : Boolean,
      globalTag: String,
      tagFilter: String
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
          columnsGlobalTag : [
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
          globalTagMap: false,
          checkedRows: [],
          record: (row) => this.checkRecord(row),
          selectedRow: '',
          activeTab: 0
      }
    },
    methods: {
        ...mapActions('db/tags', ['fetchTagByName', 'fetchTagMetaByTagName', 'fetchTagByGlobalTags']),
        ...mapActions('db/globaltagmaps', ['fetchGlobalTagsByTagName']),
        ...mapActions('db/globaltags', ['fetchGlobalTagsByName']),
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
          this.$emit('select-tag', 2);
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
      },
      getTagDetails(tagname) {
          this.fetchTagMetaByTagName(tagname);
      },
      detailsTag(tagname) {
          let liste = {};
          this.getTagDetails(tagname);
          const tagmeta = Object.entries(this.getTagMetaForTag(tagname));
          for (var i = 0; i < tagmeta.length; i++){
              if (tagmeta[i][1][0] == tagname) {
                  liste = tagmeta[i][1][1];
              }
          }
          return liste;
      },
      searchGlobalTagMap(globalTag) {
          let checked = [];
          const globaltag = Object.entries(this.getTagForGlobaltag(globalTag));
          for (var i = 0; i < globaltag.length; i++){
              for (var j = 0; j < this.tagfiltereddata.length; j++){
                  if (this.tagfiltereddata[j].name == globaltag[i][1].name) {
                      checked.push(this.tagfiltereddata[j]);
                  }
              }
          }
          this.checkedRows = checked;
      },
      checkRecord(row) {
          let res = row.name !== '';
          for (var i = 0; i < this.selectedRow.length; i++){
              if (row.name.split('_')[0] == this.selectedRow[i].name.split('_')[0]) {
                  res = row.name !== row.name;
              }
          }
          return (res);
      },
      selActive(activetab) {
          this.activeTab = activetab;
      },
      fetchGlobalTagMap(tagname) {
          this.fetchGlobalTagsByTagName(tagname);
      },
      fetchGlobalTag(globalTagName) {
          this.fetchGlobalTagsByName(globalTagName);
      },
      globaltags(tagname) {
          let globaltags_liste = [];
          this.fetchGlobalTagMap(tagname);
          const globaltagmap = Object.entries(this.getGlobalTagMapForTag(tagname));
          for (var i = 0; i < globaltagmap.length; i++){
              this.fetchGlobalTag(globaltagmap[i][1].globalTagName);
              const globaltag = Object.entries(this.getGlobalTaglist);
              for (var j = 0; j < globaltag.length; j++){
                  if (globaltag[j][1].name == globaltagmap[i][1].globalTagName) {
                      globaltags_liste.push(globaltag[j][1]);
                  }
              }
          }
          return globaltags_liste;
      }
    },
    computed: {
        ...mapState('gui/crest', ['selectedTag', 'selectedGlobalTag']),
        ...mapGetters('db/tags', ['getTaglist', 'getTagForGlobaltag', 'getTagMetaForTag']),
        ...mapState('db/iovs', ['nb_iovs_for_tag']),
        ...mapGetters('db/globaltags', ['getGlobalTaglist']),
        ...mapGetters('db/globaltagmaps', ['getGlobalTagMapForTag']),
      numrows () {
        return (!this.taglist ? -1 : this.taglist.length)
      },
      taglist: function() {
          if (this.selectedGlobalTag != "") {
              return this.getTagForGlobaltag(this.selectedGlobalTag);
          } else if (this.tagFilter != "") {
              let tags = [];
              const liste_tags = Object.entries(this.getTaglist);
              for (var i = 0; i < liste_tags.length; i++){
                  if (liste_tags[i][1].name.match(this.tagFilter)) {
                      tags.push(liste_tags[i][1])
                  }
              }
              return tags;
          } else {
              return this.getTaglist;
          }
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
        },
        globalTag: function() {
            this.globalTagMap = true;
            var globaltag = {'name':this.globalTag,'record':'','label':''};
            this.fetchTagByGlobalTags(globaltag).then(() => this.searchGlobalTagMap(this.globalTag));
        },
        checkedRows: function() {
            this.$emit('select-row', this.checkedRows);
            this.selectedRow = this.checkedRows;
        },
        tagFilter: function() {
            this.fetchTagByName(this.tagFilter);
        }
    },
    created() {
    }
  }

</script>
