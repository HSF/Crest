<template>
<div class="">
    <p class="has-text-info is-size-2">Search for Global Tags</p>
    <nav class="level">
        <div class="level is-mobile">
          <div class="level-left">
            <div class="level-item">
              <HelpInfoPane v-bind:helpmessage="helpmsg" v-bind:infomessage="infomsg" v-bind:notifytext="notifytext" v-bind:notiftype="notiftype" v-bind:links="flinks" v-on:child-switchtab="selectTab"/>
            </div>
        </div>
      </div>
    </nav>
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
        <b-radio-button v-model="radioButton"
            native-value="CreateGlobalTagMap"
            type="is-success">
            <b-icon icon="link-plus"></b-icon>
            <span>Create global tag map</span>
        </b-radio-button>
      </div>
      <div class="column is-four-fifths">
        <div v-if="radioButton === 'Search'">
          <CrestGlobalTagsTable v-on:select-tag="selectTab" />
        </div>
        <div v-if="radioButton === 'Create'">
          <GlobalTagForm/>
        </div>
        <div v-else>
          <GlobalTagMapForm/>
        </div>
      </div>
    </div>
</div>
</template>

<script>
import { mapGetters, mapActions, mapState } from 'vuex'
import CrestGlobalTagsTable from './CrestGlobalTagsTable.vue'
import GlobalTagForm from './GlobalTagForm.vue'
import GlobalTagMapForm from './GlobalTagMapForm.vue'
import HelpInfoPane from './HelpInfoPane.vue';

export default {
  name: 'GlobalTagsPane',
  props : {
    selectedserver : String,
  },
  data: function () {
    return {
        selectedtag : {},
        radioButton : 'Search',
        flinks: [
          {'btnlabel' : 'Get Tags', 'seltab' : 1}
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
    ...mapActions('db/globaltags', ['fetchGlobalTagsByName']),
    updateGlobalTag() {
      const globaltag = Object.entries(this.getGlobalTag);
      for (var i = 0; i < globaltag.length; i++){
          if (globaltag[i][0] == this.selectedGlobalTag) {
              this.selectedtag = globaltag[i][1];
          }
      }
    },
    selectTab(nt) {
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    },
  },
  computed: {
      ...mapState('gui/crest', ['selectedGlobalTag']),
      ...mapGetters('db/globaltags', ['getGlobalTag']),
      infomsg () {
        return "Access api  "+this.selectedserver
          +"<br> Selected tag is : "+this.selectedtag.name ;
      }
  },
  watch: {
      selectedGlobalTag: function() {
          this.fetchGlobalTagsByName('');
          this.updateGlobalTag();
      }
  },
  created(){
      this.fetchGlobalTagsByName('');
  },
  components: {
    CrestGlobalTagsTable,
    GlobalTagForm,
    GlobalTagMapForm,
    HelpInfoPane
  }
};
</script>
