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
        <b-collapse class="card" :open="isOpen" style="margin-top:20px;">
          <div slot="trigger" slot-scope="props" class="card-header" role="button">
            <p class="card-header-title">
              Create global tag map
            </p>
            <a class="card-header-icon">
              <b-icon :icon="props.open ? 'menu-down' : 'menu-up'">
              </b-icon>
            </a>
          </div>
          <div class="card-content">
            <div class="content">
              <b-collapse>
                <div class="container">
                  <b-field label="Global Tag name">
                    <b-dropdown v-model="globalTagName" aria-role="menu">
                      <a class="navbar-item" slot="trigger" role="button">
                        <span>{{ global_tagname_list() }}</span>
                        <b-icon icon="menu-down"></b-icon>
                      </a>
                      <b-dropdown-item aria-role="menuitem" v-for="globalTagname in liste_global_tagname"
                        :value="globalTagname"
                        :key="globalTagname">
                          {{ globalTagname }}
                      </b-dropdown-item>
                    </b-dropdown>
                  </b-field>
                  <b-field label="Record">
                    <b-input v-model="savedGlobalTagMap.record"></b-input>
                  </b-field>
                  <b-field label="Label">
                    <b-input v-model="savedGlobalTagMap.label"></b-input>
                  </b-field>
                  <b-field>
                    <p class="control">
                      <button class="button is-primary" v-on:click="save()">Save</button>
                    </p>
                  </b-field>
                </div>
              </b-collapse>
            </div>
          </div>
        </b-collapse>
      </div>
      <div class="column is-four-fifths">
        <div v-if="radioButton === 'Search'">
          <CrestTagsTable v-on:select-tag="selectTab" :globalTag="globalTagName" v-on:select-row="selectRow"/>
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
        selactiveTab : 1,
        isOpen: false,
        globalTagName: null,
        tagsSelected: []
      };
  },
  methods: {
      ...mapActions('db/tags', ['fetchTagByName', 'fetchTagByGlobalTags']),
      ...mapActions('db/globaltagmaps', ['createGlobalTagMap']),
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
    },
    save() {
        for (var i = 0; i < this.tags.length; i++){
            this.savedGlobalTagMap['tagName'] = this.tags[i];
            this.createGlobalTagMap(this.savedGlobalTagMap).then(response => {
                this.$toast.open({
                    message: 'Saved Global tag map successfully!',
                    type: 'is-success'
                })},
                error => {
                this.$toast.open({
                    message: 'Error saving global tag map!',
                    type: 'is-danger'
                })
            });
        }
    },
    global_tagname_list(){
        if (this.globalTagName != null){
            return this.globalTagName;
        } else {
            return "Select global tag name";
        }
    },
    selectRow(rows){
        this.tagsSelected = rows;
    }
  },
  computed: {
      ...mapState('gui/crest', ['selectedTag', 'selectedGlobalTag']),
      ...mapGetters('db/tags', ['getTag', 'getTagForGlobaltag']),
      ...mapGetters('db/globaltags', ['getGlobalTag']),
      infomsg () {
        return "Selected tag is : "+this.selectedtag.name ;
      },
      savedGlobalTagMap: function() {
          let record = '';
          if (this.tagsSelected.length != 0) {
              let tag = this.tagsSelected[this.tagsSelected.length - 1];
              record = tag.name.split('_')[0];
          }
          return { globalTagName : this.globalTagName,
              tagName : '',
              record : record,
              label : 'none'};
      },
      tags: function() {
          let liste_map = [];
          for (var i = 0; i < this.tagsSelected.length; i++){
              liste_map.push(this.tagsSelected[i].name);
          }
          return liste_map;  
      },
      liste_global_tagname: function() {
          let liste_globaltags = [];
          const globaltag = Object.entries(this.getGlobalTag);
          for (var i = 0; i < globaltag.length; i++){
              liste_globaltags.push(globaltag[i][0]);
          }
          return liste_globaltags;
      }
  },
  watch: {
      selectedTag: function() {
          this.updateTag();
      },
      selectedGlobalTag: function() {
          var globaltag = {'name':this.selectedGlobalTag,'record':'','label':'none'};
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
