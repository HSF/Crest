<template>
<section>
<b-tabs v-model="activeTab">
<b-tab-item label="Tags">
    <TagsPane v-on:select-tag="updateTag"/>
</b-tab-item>
<b-tab-item label="Iovs">
    <IovsPane v-bind:tagname="selectedtag"/>
</b-tab-item>
</b-tabs>
</section>
</template>
<script>
import TagsPane from './TagsPane.vue'
import IovsPane from './IovsPane.vue'

  export default {
      data : function() {
        return {
          selected: {},
          selectedtag : ''
        }
      },
      methods : {
        updateTag(tag) {
          console.log('Change tag selection '+tag)
          this.selectedtag = tag
        }
      },
      components: {
        TagsPane,
        IovsPane
      }
  }
</script>
