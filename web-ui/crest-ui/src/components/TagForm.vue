<template>
  <div class="container">
  <b-field label="Tag Name">
    <b-input v-model="savedTag.name"></b-input>
  </b-field>
  <b-field label="Tag Description">
    <b-input v-model="savedTag.description"></b-input>
  </b-field>
  <b-field label="Object Type">
    <b-input v-model="savedTag.objectType"></b-input>
  </b-field>
  <b-field label="End Of Validity">
    <b-input v-model="savedTag.endOfValidity"></b-input>
  </b-field>
  <b-field label="Last Validation Time">
    <b-input v-model="savedTag.lastValidatedTime"></b-input>
  </b-field>
  <b-field label="Since Type">
      <b-select v-model="savedTag.timeType" placeholder="Select a time type">
          <optgroup label="time in milliseconds">
              <option value="time">time</option>
          </optgroup>

          <optgroup label="run or lumi">
              <option value="run-lumi">run-lumi</option>
              <option value="run">run</option>
              <option value="run-event">run-event</option>
          </optgroup>
      </b-select>
  </b-field>
  <b-field label="Synchro Type">
      <b-select v-model="savedTag.synchronization" placeholder="Select a synchronization type">
          <optgroup label="general">
              <option value="all">all</option>
          </optgroup>

          <optgroup label="online">
              <option value="upd1">upd1</option>
              <option value="es">es</option>
              <option value="hlt">hlt</option>
          </optgroup>
          <optgroup label="offline">
              <option value="upd4">upd4</option>
              <option value="cool">cool</option>
          </optgroup>
      </b-select>
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
  name: 'ApidForm',
  props : {
  selectedserver : Object,
  },
  data: function () {
    return {
    savedTag : { name : '',
                 timeType : '',
                 description : '' ,
                 objectType : '',
                 synchronization : 'all',
                 lastValidatedTime: 0,
                 endOfValidity: 0},
    };
  },
  methods: {
    save() {
//      const hostname=[`${this.apiHost}`,`${this.apiPort}`].join(':')
      const hostname=[`${this.selectedserver.host}`,`${this.selectedserver.port}`].join(':')
        axios({
          url: `http://${hostname}/${this.apiName}/tags`,
          method: 'post',
          data: this.savedTag
        })
        .then(function (response) {
            // your action after success
            console.log(response);
        })
        .catch(function (error) {
           // your action on error success
            console.log(error);
        });
    },
  },
  components: {

  }
};
</script>
