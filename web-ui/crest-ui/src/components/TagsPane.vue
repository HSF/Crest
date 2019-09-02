<template>
<div class="">
    <p class="has-text-info is-size-2">Search for Tags</p>

    <div class="columns">
      <div class="column is-one-fifth">
          <b-field>
            <b-radio-button v-model="radioButton"
                native-value="Search"
                type="is-info">
                <b-icon icon="magnify"></b-icon>
                <span>Search</span>
            </b-radio-button>
            <b-radio-button v-model="radioButton"
                native-value="Create"
                type="is-success">
                <b-icon icon="lead-pencil"></b-icon>
                <span>Create</span>
            </b-radio-button>
        </b-field>
      </div>
      <div class="column is-four-fifths">
        <div v-if="radioButton === 'Search'">
          <CrestTagsTable v-on:select-tag="selectTab" />
        </div>
        <div v-else>
          <TagForm/>
        </div>
      </div>
    </div>
</div>
</template>

<script>
import { mapGetters, mapActions, mapState } from 'vuex'
import CrestTagsTable from './CrestTagsTable.vue'
import TagForm from './TagForm.vue'
import HelpInfoPane from './HelpInfoPane.vue';

export default {
  name: 'TagsPane',
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
      ...mapActions('db/tags', ['fetchTagByName', 'fetchTagByGlobalTags']),
    updateTag() {
      const tag = Object.entries(this.getTag);
      for (var i = 0; i < tag.length; i++){
          if (tag[i][0] == this.selectedTag) {
              this.selectedtag = tag[i][1];
          }
      }
    },
    selectTab(nt) {
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    }
  },
  computed: {
      ...mapState('gui/crest', ['selectedTag', 'selectedGlobalTag']),
      ...mapGetters('db/tags', ['getTag', 'getTagForGlobaltag']),
      infomsg () {
        return "Selected tag is : "+this.selectedtag.name ;
      }
  },
  watch: {
      selectedTag: function() {
          this.updateTag();
      },
      selectedGlobalTag: function() {
          var globaltag = {'name':this.selectedGlobalTag,'record':'','label':''};
          this.fetchTagByGlobalTags(globaltag);
      }
  },
  created(){
  },
  components: {
    CrestTagsTable,
    TagForm,
    HelpInfoPane
  }
};
</script>
