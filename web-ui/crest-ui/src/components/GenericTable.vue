<template>
    <section>
    <b-tabs>
        <b-tab-item label="Table">

        <b-field grouped group-multiline>
            <b-select v-model="defaultSortDirection">
                <option value="asc">Default sort direction: ASC</option>
                <option value="desc">Default sort direction: DESC</option>
            </b-select>
            <b-select v-model="perPage" :disabled="!isPaginated">
                <option value="5">5 per page</option>
                <option value="10">10 per page</option>
                <option value="15">15 per page</option>
                <option value="20">20 per page</option>
            </b-select>
            <div class="control is-flex">
                <b-switch v-model="isPaginated">Paginated</b-switch>
            </div>
            <button class="button field is-danger" @click="selected = null"
                :disabled="!selected">
                <b-icon icon="close"></b-icon>
                <span>Clear selected</span>
            </button>
        </b-field>

        <b-table
            :data="data"
            :columns="columns"
            :paginated="isPaginated"
            :per-page="perPage"
            :selected.sync="selected"
            @click="onClick"
            :current-page.sync="currentPage"
            :pagination-simple="isPaginationSimple"
            :default-sort-direction="defaultSortDirection"
            default-sort="name"
            style="overflow: scroll">

        </b-table>
        </b-tab-item>

        <b-tab-item label="Selected">
            <pre>{{ selected }}</pre>
        </b-tab-item>
    </b-tabs>

    </section>
</template>

<script>
  export default {
    name: 'GenericTable',
    props : {
      data : Array,
      columns : Array,
    },
    data: function() {
            return {
                isPaginated: true,
                isPaginationSimple: false,
                defaultSortDirection: 'asc',
                currentPage: 1,
                perPage: 5,
                selected: {}
            }
    },
    methods: {
      onClick(row) {
        //console.log('Clicked row : '+row)
        this.$emit('select-row', row)
      }
    }
  }

</script>
