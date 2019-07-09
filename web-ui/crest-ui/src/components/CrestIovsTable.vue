<template>
  <section>
    <p>Number of rows: {{ numrows }}
    </p>
    <b-tabs>
        <b-tab-item label="Table">
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
            :data="data"
            :paginated="isPaginated"
            :per-page="perPage"
            :current-page.sync="currentPage"
            :pagination-simple="isPaginationSimple"
            :default-sort-direction="defaultSortDirection"
            :selected.sync="selected"
            default-sort="since"
            @click="onClick"
            style="overflow: scroll"
            :loading="isloading"
            striped
            bordered
            narrowed>
            <template slot-scope="props">
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
                            field: 'tagName',
                            label: 'TAG NAME',
                            visible: true,
                            width: '20',
                        },
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
                            visible: false,
                            sortable: false
                        },
                        {
                            field: 'payloadHash',
                            label: 'HASH',
                            width: '60',
                            visible: false
                        },
                    ],
            }
    },
    methods: {
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
      onClick(row) {
        this.$store.commit('gui/crest/selectIov', row.payloadHash);
      }
    },
    computed: {
      numrows () {
        return (!this.data ? -1 : this.data.length)
      },
    },

  }

</script>
