<template>
    <section>
        <b-table
            :data="data"
            :loading="loading"

            paginated
            backend-pagination
            :total="total"
            :per-page="perPage"
            @page-change="onPageChange"

            backend-sorting
            :default-sort-direction="defaultSortOrder"
            :default-sort="[sortField, sortOrder]"
            @sort="onSort"
            :opened-detailed="defaultOpenedDetails"
            detailed
            detail-key="frameId"
            @details-open="(row, index) => $toast.open(`Expanded ${row.frameId}`)"
            >

            <template slot-scope="props">
            <b-table-column field="frameId" label="ID" width="10" sortable>
                {{ props.row.frameId }}
            </b-table-column>
            <b-table-column field="apid.packetName" label="APID" width="10" sortable>
                {{ props.row.apid.packetName }}
            </b-table-column>
            <b-table-column field="station.stationId" label="Station" width="10" sortable>
                {{ props.row.station.stationId }}
            </b-table-column>

                <b-table-column field="receptionTime" label="Reception Time" sortable centered>
                      {{ props.row.receptionTime ? new Date(props.row.receptionTime).toLocaleString() : '' }}
                </b-table-column>

                <b-table-column field="isFrameValid" label="Valid" sortable>
                     {{ props.row.isFrameValid }}
                </b-table-column>

                <b-table-column field="isFrameDuplicate" label="Duplicate" width="10">
                <span class="tag" :class="dup(props.row.isFrameDuplicate)">
                    {{ props.row.isFrameDuplicate }}
                    </span>
                </b-table-column>

                <b-table-column field="dupFrameId" label="Dup ID" width="10">
                    {{ props.row.dupFrameId }}
                </b-table-column>
            </template>
            <template slot="detail" slot-scope="props">
                       <article class="media">
                               <div class="content">
                                   <p>
                                   <strong>ObsID: {{ props.row.header.packetObsid }}</strong>
                                   <br>
                                   <small>Station Time: {{ props.row.stationStatus.stationTime  ? new Date(props.row.stationStatus.stationTime).toLocaleString() : ''}}</small>
                                   <br>
                                   <small>Pkt Time: @{{ props.row.header.packetTime }}</small>
                                   <br>
                                   <small>Ref HASH: {{ props.row.binaryHash }}</small>
                                   </p>
                               </div>
                       </article>
                   </template>
        </b-table>
    </section>
</template>

<script>
    import axios from 'axios';

    export default {
        data() {
            return {
                data: [],
                total: 0,
                loading: false,
                sortField: 'frameId',
                sortOrder: 'DESC',
                defaultSortOrder: 'DESC',
                page: 0,
                perPage: 20,
                size: 60,
                defaultOpenedDetails: [1]
            }
        },
        methods: {
            /*
             * Load async data
             */
            loadAsyncData() {
                const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
                const params = [
                    `sort=${this.sortField}:${this.sortOrder}`,
                    `page=${this.page}`,
                    `size=${this.size}`
                ].join('&')

                this.loading = true
                axios
                  .get(`http://${hostname}/api/vhf?${params}`)
                    .then((response) => {
                        // api.themoviedb.org manage max 100 pages
                        let localdata = response.data
                        this.data = []
                        let currentTotal = localdata.length
                        if (localdata.length / this.perPage > this.size) {
                            currentTotal = this.perPage * this.size
                        }
                        this.total = currentTotal
                        localdata.forEach((item) => {
                            //item.release_date = item.release_date.replace(/-/g, '/')
                            this.data.push(item)
                        })
                        this.loading = false
                    })
                    .catch((error) => {
                        this.data = []
                        this.total = 0
                        this.loading = false
                        throw error
                    })
            },
            /*
             * Handle page-change event
             */
            onPageChange(page) {
                this.page = page
                this.loadAsyncData()
            },
            /*
             * Handle sort event
             */
            onSort(field, order) {
                this.sortField = field
                this.sortOrder = order
                this.loadAsyncData()
            },
            /*
             * Type style in relation to the value
             */
            type(value) {
                const number = parseFloat(value)
                if (number < 6) {
                    return 'is-danger'
                } else if (number >= 6 && number < 8) {
                    return 'is-warning'
                } else if (number >= 8) {
                    return 'is-success'
                }
            },
            dup(value) {
                if (value) {
                    return 'is-warning'
                } else {
                    return 'is-success'
                }
            }
        },
        filters: {
            /**
             * Filter to truncate string, accepts a length parameter
             */
            truncate(value, length) {
                return value.length > length
                    ? value.substr(0, length) + '...'
                    : value
            }
        },
        mounted() {
            this.loadAsyncData()
        }
    }
</script>
