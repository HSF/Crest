<template>
<section>
<b-tabs v-model="activeTab">
<!--
<b-tab-item label="Server">
  <ServerForm v-on:select-server="updateServer"/>
</b-tab-item>
-->
<b-tab-item label="Tags">
    <TagsPane v-bind:selectedserver="selectedserver" v-on:select-tag="updateTag"  v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Iovs">
    <IovsPane v-bind:tagname="selectedtag" v-bind:selectedserver="selectedserver" v-on:select-iov="updateIov"  v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Payloads">
    <PayloadsPane v-bind:tagname="selectedtag" v-bind:selectedIov="selectediov" v-bind:selectedserver="selectedserver"/>
</b-tab-item>
</b-tabs>
</section>
</template>
<script>

import TagsPane from './TagsPane.vue'
import IovsPane from './IovsPane.vue'
import PayloadsPane from './PayloadsPane.vue'

  export default {
      props : {
        selectedserver : Object
      },
      data : function() {
        return {
        selected: {},
        activeTab: 0,
        selectediov: {},
        selectedtag : ''
        }
      },
      methods : {
        selActive(activetab) {
          console.log('Change active tab '+activetab)
          this.activeTab = activetab
        },
        updateTag(tag) {
          console.log('Change tag selection '+tag)
          this.selectedtag = tag
        },
        updateIov(iov) {
          console.log('Change iov selection '+iov)
          this.selectediov = iov
        },
      },
      components: {
        TagsPane,
        IovsPane,
        PayloadsPane,
      }
  }
</script>
