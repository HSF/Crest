<template>
<section>
<b-tabs v-model="activeTab">
<b-tab-item label="Tags">
    <TagsPane v-on:select-tag="updateTag"/>
</b-tab-item>
<b-tab-item label="Iovs">
    <IovsPane v-bind:tagname="selectedtag" v-on:select-iov="updateIov"/>
</b-tab-item>
<b-tab-item label="Payloads">
    <PayloadsPane v-bind:tagname="selectedtag" v-bind:selectedIov="selectediov"/>
</b-tab-item>
</b-tabs>
</section>
</template>
<script>
import TagsPane from './TagsPane.vue'
import IovsPane from './IovsPane.vue'
import PayloadsPane from './PayloadsPane.vue'

  export default {
      data : function() {
        return {
        selected: {},
        activeTab: 0,
        selectediov: {},
          selectedtag : ''
        }
      },
      methods : {
      updateTag(tag) {
        console.log('Change tag selection '+tag)
        this.selectedtag = tag
      },
      updateIov(iov) {
        console.log('Change iov selection '+iov)
        this.selectediov = iov
      }
      },
      components: {
        TagsPane,
        IovsPane,
        PayloadsPane
      }
  }
</script>
