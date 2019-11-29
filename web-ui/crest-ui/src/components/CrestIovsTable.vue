<template>
  <section>
    <p>Number of rows: {{ numrows }}
    </p>
    <b-tabs>
      <b-tab-item label="Table">
      <div class="columns">
      <div class="column is-three-quarters">
          <span v-if="selectedTag != ''">
            <b-field label="Since">
              <b-input v-if="tag[selectedTag].timeType != 'time'" v-model="since" maxlength="20"></b-input>
              <DateTimePicker v-if="tag[selectedTag].timeType == 'time'" v-on:select-since="selectSince" v-model="since" />
            </b-field>
            <b-field label="Until">
              <b-input v-if="tag[selectedTag].timeType != 'time'" v-model="until" maxlength="20"></b-input>
              <DateTimePicker v-if="tag[selectedTag].timeType == 'time'" v-on:select-until="selectUntil" v-model="until" />
            </b-field>
            </span>
            </div>
            <div class="column is-one-quarter">
            <span v-if="selectedTag != ''">
            <b-field label="Tag">
              <b-input v-model="selectedtag.name" placeholder="Tag selection on another tab" disabled></b-input>
            </b-field>
            <b-field>
              <p class="control">
                <button class="button is-primary" v-on:click="loadIovs()">Search</button>
              </p>
            </b-field>
            <b-field>
              <button class="button field is-danger" @click="clearFilters()" :disabled="!selected">
                <b-icon icon="close"></b-icon>
                <span>Clear filters</span>
              </button>
            </b-field>
          </span>
          </div>
          </div>
        <!--<b-field grouped group-multiline>
            <div v-for="(column, index) in columns"
                :key="index"
                class="control">
                <b-checkbox v-model="column.visible">
                    {{ column.label }}
                </b-checkbox>
            </div>
        </b-field>-->

        <b-table
            :data="data"
            detailed
            :paginated="isPaginated"
            :per-page="perPage"
            :current-page.sync="currentPage"
            :pagination-simple="isPaginationSimple"
            :default-sort-direction="defaultSortDirection"
            :selected.sync="selected"
            default-sort="since"
            style="overflow: scroll"
            :loading="isloading">
            <template slot-scope="props">
              <b-table-column field="selectedtag.name" label="Tag name" centered>
                  {{ selectedtag.name }}
              </b-table-column>
              <b-table-column v-for="(column, index) in columns"
                  :key="index"
                  :label="column.label"
                  :visible="column.visible"
                  :field="column.field"
                  sortable>
                  {{ props.row[column.field] }}
              </b-table-column>
              <b-table-column field="since" label="Str-Since" centered>
                  <span class="tag is-success">
                      {{ timestr(props.row.since) }}
                  </span>
              </b-table-column>
              <b-table-column field="insertionTime" label="Insert Time" centered>
                  <span class="tag is-success">
                      {{ (props.row.insertionTime) }}
                  </span>
              </b-table-column>
            </template>
            <template slot="detail" slot-scope="props">
              <PayloadsPane v-bind:selectediov="props.row"/>
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
import { mapState, mapGetters, mapActions } from 'vuex'
import DateTimePicker from './DateTimePicker.vue'
import PayloadsPane from'./PayloadsPane.vue'
import Long from 'long';

  export default {
    name: 'CrestIovsTable',
    props : {
      data : Array,
      selectedtag : Object,
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
                            field: 'since',
                            label: 'SINCE',
                            width: '15',
                            visible: true,
                            sortable: true
                        },
                        {
                            field: 'insertionTime',
                            label: 'InsertTime',
                            visible: true,
                            sortable: false
                        },
                        {
                            field: 'payloadHash',
                            label: 'HASH',
                            width: '60',
                            visible: true
                        },
                    ],
                since : 0,
                until : 'INF',
                snapshot : '0'
            }
    },
    methods: {
        ...mapActions('db/iovs', ['fetchIovByTagName']),
      timestr (atime) {
        if (!atime) {
          return 'none'
        }
        if (atime === '0') {
          return '0'
        }
        if (this.selectedtag.timeType === 'time')
          return new Date(atime*1000).toGMTString()
        if (this.selectedtag.timeType.startsWith('run')) {
          var coolt = Long.fromString(atime.toString(),10);
          var run = coolt.getHighBitsUnsigned()
          var lumi = coolt.getLowBits();
          return run.toString()+'-'+lumi.toString();
        }
      },
      loadIovs() {
          this.$store.commit('gui/iovForm/selectTagname', this.selectedTag);
          this.$store.commit('gui/iovForm/selectSince', this.since);
          this.$store.commit('gui/iovForm/selectUntil', this.until);
          this.$store.commit('gui/iovForm/selectSnapshot', this.snapshot);
          this.searchIovs;
      },
      selectSince(since) {
          this.since = since;
      },
      selectUntil(until) {
          this.until = until;
      },
      clearFilters(){
          this.since = 0;
          this.until = 'INF';
          this.$store.commit('gui/iovForm/selectSince', this.since);
          this.$store.commit('gui/iovForm/selectUntil', this.until);
          this.searchIovs;
      }
    },
    computed: {
        ...mapState('gui/crest', ['selectedTag']),
        ...mapGetters('db/tags', ['getTag']),
      numrows () {
        return (!this.data ? -1 : this.data.length)
      },
      searchIovs: function() {
          var searchIov = {'tagname':this.selectedTag,'since':this.since,'until':this.until,'snapshot':this.snapshot};
          return this.fetchIovByTagName(searchIov);
      },
      tag: function() {
          return this.getTag;
      }
    },
    components: {
        DateTimePicker,
        PayloadsPane
    }
  }

</script>
