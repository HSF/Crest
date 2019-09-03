<template>
  <div class="content" style="overflow:auto; margin-right:15px; padding-left:5px;">
  <b-field label="Tag Name">
    <b-input v-model="savedIov.tagname" disabled></b-input>
  </b-field>
  <b-field label="Since">
    <b-input v-if="selectedtag.timeType != 'time'" v-model="savedIov.since" maxlength="20"></b-input>
    <DateTimePicker v-if="selectedtag.timeType == 'time'" v-on:select-since="selectSince" v-model="savedIov.since" />
  </b-field>
  <b-field label="Format">
      <b-select placeholder="Select a format" v-model="format">
          <option
              v-for="option in fmtdata"
              :value="option.format"
              :key="option.format">
              {{ option.format }}
          </option>
      </b-select>
  </b-field>
  <b-field class="file">
      <b-upload v-model="savedIov.file">
          <a class="button is-primary">
              <b-icon icon="upload"></b-icon>
              <span>Click to upload</span>
          </a>
      </b-upload>
      <span class="file-name" v-if="savedIov.file">
          {{ savedIov.file.name }}
      </span>
  </b-field>
  <b-field>
      <p class="control">
        <button class="button is-primary" v-on:click="save()">Save</button>
      </p>
  </b-field>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import DateTimePicker from './DateTimePicker.vue';

export default {
  name: 'IovForm',
  props : {
    selectedtag: Object,
  },
  data: function () {
    return {
      format : 'JSON',
      savedIov : { tagname : this.selectedtag.name, since : 0 , file : null, endtime : 0 },
      fmtdata : [
        { 'format' : 'PNG'},
        { 'format' : 'JSON'},
        { 'format' : 'YAML'},
        { 'format' : 'FITS'},
        { 'format' : 'ROOT'},
        { 'format' : 'CSV'},
        { 'format' : 'XML'},
        { 'format' : 'TXT'},
        { 'format' : 'BIN'},
        { 'format' : 'PDF'},
        { 'format' : 'empty'}
      ],
    };
  },
  methods: {
      ...mapActions('db/iovs', ['createIov']),
      selectSince(since) {
          this.savedIov['since'] = since;
      },
      selectUntil(until) {
          this.savedIov['until'] = until;
      },
      save() {
          var iovForm = {'format': this.format, 'tagname':this.selectedTag,'since':this.selectedSince,'until':this.selectedUntil,'snapshot':this.selectedSnapshot};
          var res = {'setIov':this.savedIov,'iovForm':iovForm}
          this.createIov(res).then(response => {
              this.$toast.open({
                  message: 'Saved Iov successfully!',
                  type: 'is-success'
              })},
              error => {
              this.$toast.open({
                  message: 'Error saving iov!',
                  type: 'is-danger'
              })
          });
      },
  },
  computed: {
      ...mapState('gui/iovForm', ['selectedSince', 'selectedUntil', 'selectedSnapshot']),
      ...mapState('gui/crest', ['selectedTag']),
  },
  components: {
      DateTimePicker
  }
};
</script>
