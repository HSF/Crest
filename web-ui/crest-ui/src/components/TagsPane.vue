<template>
<div class="container">
    <p class="has-text-info is-size-2">Search for Tags</p>
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
            </b-field>

            <b-field>
            <b-radio-button v-model="radioButton"
                native-value="Create"
                type="is-success">
                <b-icon icon="check"></b-icon>
                <span>Create</span>
            </b-radio-button>
        </b-field>
        <b-field label="Search Tag by name">
            <b-autocomplete
                rounded
                v-model="thetag"
                :data="filteredDataArray"
                placeholder="e.g. MuonAlign"
                icon="magnify"
                @select="option => selected = option">
                <template slot="empty">No results found</template>
            </b-autocomplete>
        </b-field>
        <b-field>
          <p class="control">
            <button class="button is-primary" v-on:click="loadTags()" :disabled="radioButton !== 'Search'">Search</button>
          </p>
        </b-field>
      </div>
      <div class="column is-four-fifths">
        <div v-if="radioButton === 'Search'">
          <CrestTagsTable v-bind:data="rows" v-on:select-row="updateTag"/>
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
import CrestTagsTable from './CrestTagsTable.vue'
import TagForm from './TagForm.vue'
import HelpInfoPane from './HelpInfoPane.vue';

export default {
  name: 'TagsPane',
  props : {
    selectedserver : String,
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
        rows: [],
        selected: null,
        selactiveTab : 1,
        thetag: ''
      };
  },
  methods: {
      ...mapActions('db/tags', ['fetchTagByName']),
    updateTag(stag) {
      this.selectedtag = stag
      this.thetag = stag.name
      this.$emit('select-tag', this.selectedtag)
    },
    gotoIovs() {
      this.selactiveTab = 1
      this.$emit('select-tab', this.selactiveTab)
    },
    selectTab(nt) {
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    },
    loadTags(){
        let liste_tags = [];
        const tag = Object.entries(this.getTag);
        for (var i = 0; i < tag.length; i++){
            liste_tags.push(tag[i][1]);
        }
        this.rows = liste_tags;
    },
  },
  computed: {
      ...mapGetters('db/tags', ['getTag']),
      tagnames() {
        let result = this.rows.map(a => a.name);
        return result
      },
      filteredDataArray() {
          return this.tagnames.filter((option) => {
              //console.log('filtering '+option)
              return option
                  .toString()
                  .toLowerCase()
                  .indexOf(this.thetag.toLowerCase()) >= 0
          })
      },
      infomsg () {
        return "Access api  "+this.selectedserver
          +"<br> Selected tag is : "+this.selectedtag.name ;
      }
  },
  watch: {
      thetag: function() {
          this.fetchTagByName(this.thetag);
      }
  },
  created(){
      this.fetchTagByName(this.thetag);  
  },
  components: {
    CrestTagsTable,
    TagForm,
    HelpInfoPane
  }
};
</script>
