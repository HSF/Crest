<template>
  <div class="container">
  <b-field label="Tag Name">
    <b-input v-model="savedIov.tag"></b-input>
  </b-field>
  <b-field label="Since">
    <b-input v-model="savedIov.since"></b-input>
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
  selectedserver : Object,
  },
  data: function () {
    return {
    savedIov : { tag : '', since : 0 , file : null, endtime : 0 },

    };
  },
  methods: {
  save() {
    console.log('saving a iov '+this.savedIov.tag+' '+this.savedIov.since)
//    const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
    const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
    const sdata = new FormData();
    sdata.append("file", this.savedIov.file);
    sdata.append("tag", this.savedIov.tag);
    sdata.append("since", this.savedIov.since);

      axios({
        url: `http://${hostname}/${this.apiName}/payloads/store`,
        method: 'post',
        data: sdata
      })
      .then(function (response) {
          // your action after success
          this.$toast.open({
                   message: 'Saved Iov successfully!',
                   type: 'is-success'
          })
          //console.log(response);
      })
      .catch(function (error) {
         // your action on error success
          console.log(error);
          this.$toast.open({
                   message: 'Error in saving Iov '+error,
                   type: 'is-danger'
          })
      });
    },
  },
  components: {

  }
};
</script>
