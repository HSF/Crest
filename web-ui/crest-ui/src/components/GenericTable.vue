<template>
    <section>
        <button class="button field is-danger" @click="selected = null"
            :disabled="!selected">
            <b-icon icon="close"></b-icon>
            <span>Clear selected</span>
        </button>

        <b-tabs>
            <b-tab-item label="Table">
                <b-table
                    :data="data"
                    :columns="columns"
                    :selected.sync="selected"
                    @click="onClick"
                    focusable>
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
          columns : Array
        },
        data : function() {
          return {
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
