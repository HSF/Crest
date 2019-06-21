<template>
<div class="container">
  <p class="has-text-info is-size-2">Search for Iovs</p>
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
  <b-field label="Since">
    <b-input v-model="since" maxlength="20"></b-input>
  </b-field>
  <b-field label="Until">
    <b-input v-model="until" maxlength="20"></b-input>
  </b-field>
  <b-field label="Tag">
      <b-input v-model="selectedtag.name" placeholder="Tag selection on another tab" disabled></b-input>
  </b-field>
  <b-field>
  <p class="control">
    <button class="button is-primary" v-on:click="loadIovs()" :disabled="radioButton !== 'Search'">Search</button>
  </p>
  </b-field>
</div>
<div class="column is-four-fifths">
  <div v-if="radioButton === 'Search'">
    <CrestIovsTable v-bind:data="iovs" v-bind:selectedtag="selectedtag" v-bind:isloading="isLoading" v-on:select-row="updateHash"/>
  </div>
  <div v-else>
    <IovForm v-bind:selectedserver="selectedserver" v-bind:selectedtag="selectedtag"/>
  </div>
</div>
</div>
</div>
</template>

<script>
import { mapGetters, mapActions, mapState } from 'vuex'
import CrestIovsTable from './CrestIovsTable.vue'
import IovForm from './IovForm.vue'
import HelpInfoPane from './HelpInfoPane.vue';

export default {
  name: 'IovsPane',
  props : {
    selectedtag : Object,
    selectedserver : String,
  },
  data: function () {
    return {
      flinks: [
        {'btnlabel' : 'Get Payload', 'seltab' : 2}
      ],
      helpmsg: "<p>Search for tags using filtering by tag name.</p>"
        +"<p>Once you select a tag you can browse the associated IOVs by changing to appropriate tab or clicking on the <b>Get Iovs</b> button.</p>"
        +"<p>You can use the <b>Create</b> button to create a new tag.</p>",
      notiftype : 'is-info',
      notifytext : 'Searching iovs....',
      isFullPage : false,
      isLoading : false,
      selectedIov : {},
      radioButton : 'Search',
      since : 0,
      until : 'INF',
      snapshot : '0',
      selactiveTab : 2,
      thehash: ''};
  },
  methods: {
      ...mapActions('db/iovs', ['fetchIovByTagName']),
    updateHash(row) {
      this.selectedIov = row
      this.thehash = row.payloadHash
      this.$emit('select-iov', this.selectedIov)
    },
    selectTab(nt) {
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    },
    /*async printRows() {
      for (var i in this.rows) {
        console.log(this.rows[i]);
      }
    },*/
    loadIovs() {
        this.$store.commit('gui/iovForm/selectTagname', this.selectedTag);
        this.$store.commit('gui/iovForm/selectSince', this.since);
        this.$store.commit('gui/iovForm/selectUntil', this.until);
        this.$store.commit('gui/iovForm/selectSnapshot', this.snapshot);        
        this.searchIovs;
    }
  },
  computed: {
      ...mapState('gui/crest', ['selectedTag']),
      ...mapGetters('db/iovs', ['getIovForTag']),
      hostbaseurl () {
      return this.selectedserver;
      },
      /*isLoadingData () {
        if (this.rows.length <= 0 && this.isLoading) {
          return true;
        } else {
          this.isLoading = false;
          return false;
        }
      },*/
      infomsg () {
        return "Access api  "+this.selectedserver
          +"<br> Selected tag is : "+this.selectedtag.name
          +"<br> Selected iov is : "+this.selectedIov.since;
      },
      iovs: function() {
          return this.getIovForTag(this.selectedTag);
      },
      searchIovs: function() {
          var searchIov = {'tagname':this.selectedTag,'since':this.since,'until':this.until,'snapshot':this.snapshot};
          return this.fetchIovByTagName(searchIov);
      }
  },
  watch: {
      selectedTag: function() {
          this.searchIovs;
      },
  },
  components: {
    CrestIovsTable,
    IovForm,
    HelpInfoPane
  }
};
</script>
