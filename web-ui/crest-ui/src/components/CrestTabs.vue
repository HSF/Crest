<template>
<section>
<b-tabs v-model="activeTab">
<!--
<b-tab-item label="Server">
  <ServerForm v-on:select-server="updateServer"/>
</b-tab-item>
-->
<b-tab-item label="Tags">
    <TagsPane v-bind:selectedserver="hostbaseurl" v-on:select-tag="updateTag"  v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Iovs">
    <IovsPane v-bind:selectedtag="selectedtag" v-bind:selectedserver="hostbaseurl" v-on:select-iov="updateIov"  v-on:select-tab="selActive"/>
</b-tab-item>
<b-tab-item label="Payloads">
    <PayloadsPane v-bind:selectedtag="selectedtag" v-bind:selectediov="selectediov" v-bind:selectedserver="hostbaseurl"/>
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
        selectedtag : {}
        }
      },
      methods : {
        selActive(activetab) {
          console.log('Change active tab '+activetab)
          this.activeTab = activetab
        },
        updateTag(tag) {
          console.log('Change tag selection '+tag.name)
          this.selectedtag = tag
        },
        updateIov(iov) {
          console.log('Change iov selection '+iov)
          this.selectediov = iov
        },
      },
      computed: {
        hostbaseurl () {
          if (this.selectedserver.url !== "") {
            return this.selectedserver.url;
          }
          const selprotocol = this.selectedserver.protocol.toLowerCase();
          const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':');
          var burl = `${selprotocol}://${hostname}/${this.selectedserver.api}`;
          return burl;
      },
      components: {
        TagsPane,
        IovsPane,
        PayloadsPane,
      }
  }
</script>
