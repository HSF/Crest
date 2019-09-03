<template>

<section>
<b-tabs v-model="activeTab">
<!--
<b-tab-item label="Server">
  <ServerForm v-on:select-server="updateServer"/>
</b-tab-item>
-->
<b-tab-item label="GlobalTags">
    <GlobalTagsPane v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Tags">
    <TagsPane v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Iovs">
    <IovsPane v-bind:selectedtag="selectedtag" v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Folders">
    <FoldersPane  v-on:select-tab="selActive"/>
</b-tab-item>
</b-tabs>
</section>
</template>
<script>

import { mapState, mapActions, mapGetters } from 'vuex'
import TagsPane from './TagsPane.vue'
import IovsPane from './IovsPane.vue'
import FoldersPane from './FoldersPane.vue'
import GlobalTagsPane from './GlobalTagsPane.vue'

  export default {
      props : {
        selectedserver : Object
      },
      data : function() {
        return {
        selected: {},
        activeTab: 0,
        selectediov: {},
        selectedtag : {}
        }
      },
      methods : {
          ...mapActions('db/tags', ['fetchTagByName']),
        selActive(activetab) {
          console.log('Change active tab '+activetab)
          this.activeTab = activetab
        },
        updateTag() {
            const tag = Object.entries(this.getTag);
            for (var i = 0; i < tag.length; i++){
                if (tag[i][0] == this.selectedTag) {
                    this.selectedtag = tag[i][1];
                    console.log('Change tag selection '+tag[i][1].name)
                }
            }
        },
        updateIov() {
            const iov = Object.entries(this.getIovForTag(this.selectedTag));
            for (var i = 0; i < iov.length; i++){
                if (iov[i][1].payloadHash == this.selectedIov) {
                    this.selectIov = iov[i][1];
                    console.log('Change iov selection '+iov[i][1])
                }
            }
        }
      },
      computed: {
          ...mapState('gui/crest', ['selectedTag', 'selectedIov']),
          ...mapGetters('db/tags', ['getTag']),
          ...mapGetters('db/iovs', ['getIovForTag']),
      },
      watch: {
          selectedTag: function() {
              this.fetchTagByName(this.selectedTag);
              this.updateTag();
          },
          selectedIov: function() {
              this.updateIov();
          }
      },
      components: {
        GlobalTagsPane,
        TagsPane,
        IovsPane,
        FoldersPane
      }
  }
</script>
