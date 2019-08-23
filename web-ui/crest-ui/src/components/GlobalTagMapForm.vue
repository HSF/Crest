<template>
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
  <b-field label="Tag name">
    <b-dropdown v-model="tagName" aria-role="menu">
      <a class="navbar-item" slot="trigger" role="button">
        <span>{{ tagname_list() }}</span>
        <b-icon icon="menu-down"></b-icon>
      </a>
      <b-dropdown-item aria-role="menuitem" v-for="tagName in liste_tagname"
        :value="tagName"
        :key="tagName">
          {{ tagName }}
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
</template>

<script>
import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'GlobalTagMapForm',
  props : {
  },
  data: function () {
    return {
        globalTagName: null,
        tagName: null
    };
  },
  methods: {
    ...mapActions('db/globaltagmaps', ['createGlobalTagMap']),
    save() {
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
    },
    global_tagname_list(){
        if (this.globalTagName != null){
            return this.globalTagName;
        } else {
            return "Select global tag name";
        }
    },
    tagname_list(){
        if (this.tagName != null){
            return this.tagName;
        } else {
            return "Select tag name";
        }
    }
  },
  computed: {
      ...mapGetters('db/globaltags', ['getGlobalTag']),
      ...mapGetters('db/tags', ['getTag']),
      savedGlobalTagMap: function() {
          return { globalTagName : this.globalTagName,
              tagName : this.tagName,
              record : '' ,
              label : ''};
      },
      liste_global_tagname: function() {
          let liste_globaltags = [];
          const globaltag = Object.entries(this.getGlobalTag);
          for (var i = 0; i < globaltag.length; i++){
              liste_globaltags.push(globaltag[i][0]);
          }
          return liste_globaltags;
      },
      liste_tagname: function() {
          let liste_tags = [];
          const tag = Object.entries(this.getTag);
          for (var i = 0; i < tag.length; i++){
              liste_tags.push(tag[i][0]);
          }
          return liste_tags;
      }
  },
  components: {
  },
  created() {
      this.$store.commit('gui/crest/selectGlobalTag', '');
  }
};
</script>
