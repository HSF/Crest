<template>
  <div class="container">
  <b-field label="Tag Name">
    <b-input v-model="savedIov.tagname" disabled></b-input>
  </b-field>
  <b-field label="Since">
    <b-input v-model="savedIov.since"></b-input>
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
import axios from 'axios'

export default {
  name: 'IovForm',
  props : {
    selectedserver : String,
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
      savedresponse : {},
      savederror : {},
    };
  },
  methods: {
  save() {
    let that=this;

    console.log('saving a iov '+this.savedIov.tagname+' '+this.savedIov.since+" with format "+this.format)
    const sdata = new FormData();
    sdata.append("file", this.savedIov.file);
    sdata.append("tag", this.savedIov.tagname);
    sdata.append("since", this.savedIov.since);
    var postobj = {
      url: `${this.hostbaseurl}/payloads/store`,
      method: 'post',
      headers: { 'X-Crest-PayloadFormat' : `${this.format}` },
      data: sdata
    };
    console.log('use header '+postobj.headers['X-Crest-PayloadFormat']);
    axios(postobj)
      .then(function (response) {
          // your action after success
          that.savedresponse = response;
      })
      .catch(function (error) {
         // your action on error success
          console.log(error);
          that.savederror = error;
      });
    },
  },
  computed: {
      hostbaseurl () {
        return this.selectedserver;
      },
  },
  watch: {
    savedresponse : function() {
      this.$toast.open({
               message: 'Saved Iov successfully!',
               type: 'is-success'
      })
    },
    savederror : function() {
      this.$toast.open({
               message: 'Error saving iov!',
               type: 'is-danger'
      })
    }
  },
  components: {

  }
};
</script>
