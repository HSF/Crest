<template>
  <div>
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
          <CrestIovsTable v-bind:data="iovs" v-bind:selectedtag="selectedtag"/>
        </div>
        <div v-else>
          <IovForm v-bind:selectedtag="selectedtag"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions, mapState } from 'vuex'
import CrestIovsTable from './CrestIovsTable.vue'
import IovForm from './IovForm.vue'
import HelpInfoPane from './HelpInfoPane.vue'

export default {
  name: 'IovsPane',
  props : {
    selectedtag : Object,
  },
  data: function () {
    return {
      flinks: [
        {'btnlabel' : 'Get Tags', 'seltab' : 1}
      ],
      helpmsg: "<p>Search for tags using filtering by tag name.</p>"
        +"<p>Once you select a tag you can browse the associated IOVs by changing to appropriate tab or clicking on the <b>Get Iovs</b> button.</p>"
        +"<p>You can use the <b>Create</b> button to create a new tag.</p>",
      notiftype : 'is-info',
      notifytext : 'Searching iovs....',
      isFullPage : false,
      selectIov : {},
      radioButton : 'Search',
      since : 0,
      until : 'INF',
      snapshot : '0',
      selactiveTab : 2};
  },
  methods: {
      ...mapActions('db/iovs', ['fetchIovByTagName']),
    updateHash() {
      const iov = Object.entries(this.getIovForTag(this.selectedTag));
      for (var i = 0; i < iov.length; i++){
          if (iov[i][1].payloadHash == this.selectedIov) {
              this.selectIov = iov[i][1];
          }
      }
    },
    selectTab(nt) {
      this.selactiveTab = nt
      this.$emit('select-tab', this.selactiveTab)
    },
    selectSince(since) {
        this.since = since;
    },
    selectUntil(until) {
        this.until = until;
    },
  },
  computed: {
      ...mapState('gui/crest', ['selectedTag', 'selectedIov']),
      ...mapGetters('db/tags', ['getTag']),
      ...mapGetters('db/iovs', ['getIovForTag']),
      infomsg () {
        return "Selected tag is : "+this.selectedtag.name
          +"<br> Selected iov is : "+this.selectIov.since;
      },
      iovs: function() {
          return this.getIovForTag(this.selectedTag);
      },
      searchIovs: function() {
          var searchIov = {'tagname':this.selectedTag,'since':this.since,'until':this.until,'snapshot':this.snapshot};
          return this.fetchIovByTagName(searchIov);
      },
      tag: function() {
          return this.getTag;
      }
  },
  watch: {
      selectedTag: function() {
          this.searchIovs;
      },
      selectedIov: function() {
          this.updateHash();
      }
  },
  components: {
    CrestIovsTable,
    IovForm,
    HelpInfoPane
  }
};
</script>
