<template>
<div class="">
    <p class="has-text-info is-size-2">Search for Folders</p>
    <div class="columns">
      <div class="column is-one-fifth">
          <b-field>
            <b-radio-button v-model="radioButton"
                native-value="Search"
                type="is-info">
                <b-icon icon="magnify"></b-icon>
                <span>Search</span>
            </b-radio-button>
        </b-field>
      </div>
      <div class="column is-four-fifths">
        <div v-if="radioButton === 'Search'">
          <CrestFoldersTable v-on:select-tag="selectTab" />
        </div>
        <div v-else>
          <TagForm/>
        </div>
      </div>
    </div>
</div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import CrestFoldersTable from './CrestFoldersTable.vue'

export default {
  name: 'FoldersPane',
  props : {
  },
  data: function () {
    return {
        selectedtag : {},
        radioButton : 'Search',
        flinks: [
          {'btnlabel' : 'Get Iovs', 'seltab' : 1}
        ],
        helpmsg: "<p>Search for tags using filtering by tag name.</p>"
          +"<p>Once you select a tag you can browse the associated IOVs by changing to appropriate tab or clicking on the <b>Get Iovs</b> button.</p>"
          +"<p>You can use the <b>Create</b> button to create a new tag.</p>",
        notiftype : 'is-info',
        notifytext : 'Searching tags....',
        selactiveTab : 1
      };
  },
  methods: {
      ...mapActions('db/folders', ['fetchFolder']),
      updateFolder() {
        const folder = Object.entries(this.getFolder);
        for (var i = 0; i < folder.length; i++){
            this.selectedtag = folder[i][1];
        }
      },
      selectTab(nt) {
        this.selactiveTab = nt
        this.$emit('select-tab', this.selactiveTab)
      },
    },
    computed: {
        ...mapGetters('db/folders', ['getFolder']),
        infomsg () {
          return "Selected tag is : "+this.selectedtag.nodeFullpath ;
        }
    },
    watch: {
    },
    created(){
        this.fetchFolder();
    },
  components: {
    CrestFoldersTable,
  }
};
</script>
