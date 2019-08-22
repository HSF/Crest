<template>
  <div class="container">
  <b-field label="Global Tag Name">
    <b-input v-model="savedGlobalTag.name" placeholder="set a name for the new tag"></b-input>
  </b-field>
  <b-field label="Validity">
    <b-input v-model="savedGlobalTag.validity"></b-input>
  </b-field>
  <b-field label="Global Tag description">
    <b-input v-model="savedGlobalTag.description"></b-input>
  </b-field>
  <b-field label="Release">
    <b-input v-model="savedGlobalTag.release"></b-input>
  </b-field>
  <b-field label="Scenario">
    <b-input v-model="savedGlobalTag.scenario"></b-input>
  </b-field>
  <b-field label="Workflow">
    <b-input v-model="savedGlobalTag.workflow"></b-input>
  </b-field>
  <b-field label="Type">
    <b-input v-model="savedGlobalTag.type"></b-input>
  </b-field>
  <b-field>
      <p class="control">
        <button class="button is-primary" v-on:click="save()">Save</button>
      </p>
  </b-field>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'GlobalTagForm',
  props : {
  },
  data: function () {
    return {
    };
  },
  methods: {
    ...mapActions('db/globaltags', ['createGlobalTag']),
    save() {
        this.createGlobalTag(this.savedGlobalTag).then(response => {
            this.$toast.open({
                message: 'Saved Global tag successfully!',
                type: 'is-success'
            })},
            error => {
            this.$toast.open({
                message: 'Error saving global tag!',
                type: 'is-danger'
            })
        });
    },
  },
  computed: {
      savedGlobalTag: function() {
          return { name : '',
              validity : 0,
              description : '' ,
              release : '',
              scenario : '',
              workflow: '',
              type: ''};
      }
  },
  components: {
  },
  created() {
      this.$store.commit('gui/crest/selectGlobalTag', '');
  }
};
</script>
