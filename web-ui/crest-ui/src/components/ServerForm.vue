<template>
  <div class="container">
    <div class="notification">
    <b-field label="Host">
      <b-input v-model="thehost"></b-input>
    </b-field>
    <b-field label="Port">
      <b-input v-model="theport"></b-input>
    </b-field>
    <b-field>
        <p class="control">
          <button class="button is-primary" v-on:click="save()">Save</button>
        </p>
    </b-field>
  </div>
  </div>
</template>

<script>

export default {
  name: 'ServerForm',
  props : {
  },
  data: function () {
    return {
      thehost: this.apiHost,
      theport: this.apiPort
    };
  },
  methods: {
    save() {
      this.apiHost = this.thehost;
      this.apiPort = this.theport;
      this.$emit('select-server', { host : this.thehost, port : this.theport } )
    },
  },
  components: {

  }
};
</script>
