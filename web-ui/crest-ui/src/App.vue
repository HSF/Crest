<template>
<div id="app" class="container">
  <Title v-on:select-server="selectServer"/>
  <CrestTabs v-bind:selectedserver="selectedserver"/>
  <p>{{msg}}: default api on {{apiHost}} and port {{apiPort}} using protocol {{apiProtocol}}</p>
  <p>Selected server is {{ selectedserver.url }}</p>
</div>
</template>
<script>
import Title from './components/Title.vue'
import CrestTabs from './components/CrestTabs.vue'

export default {
  name: 'app',
  data () {
    return {
      selectedserver: { host: this.apiHost, port: this.apiPort, protocol: this.apiProtocol, url: ''},
      msg: 'Welcome to Crest Browser'
    }
  },
  methods: {
    selectServer(serverurl) {
      console.log('App is changing active server url '+serverurl.url);
      this.selectedserver.url = serverurl.url;
      console.log('The server is now set to '+this.selectedserver.url);
    },
  },
  components: {
    CrestTabs,
    Title
  }
}
</script>
