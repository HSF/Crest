<template>
<div id="app">
  <Title v-on:select-server="selectServer"/>
  <CrestTabs v-bind:selectedserver="hostname" v-on:info-notification="activateInfoNotification"
    v-on:error-notification="activateErrorNotification"/>
  <p>{{msg}}: default api {{apiName}} on {{apiUrl}} </p>
  <Footer v-bind:notiftype="thetype" v-bind:selectedserver="hostname" v-bind:notifytext="thenotification"/>

</div>
</template>

<style lang="scss">
@import "bulma_custom";
</style>

<script>
import Title from './components/Title.vue'
import CrestTabs from './components/CrestTabs.vue'
import Footer from './components/Footer.vue'

export default {
  name: 'app',
  data () {
    return {
      thenotification : 'none',
      thetype : '',
      selectedserver: { host: this.apiHost, port: this.apiPort, protocol: this.apiProtocol, api: this.apiName, url: ''},
      msg: 'Welcome to Crest Browser',
      hostname: this.apiHost
    }
  },
  methods: {
    selectServer(serverurl) {
      console.log('App is changing active server url '+serverurl.url);
      this.selectedserver.url = serverurl.url;
      console.log('The server is now set to '+this.selectedserver.url);
    },
    activateInfoNotification(notif) {
      console.log("received Info notification "+notif);
      this.thetype = 'is-info';
      this.thenotification = notif;
    },
    activateErrorNotification(notif) {
      console.log("received Error notification "+notif);
      this.thetype = 'is-error';
      this.thenotification = notif;
    },
  },
  components: {
    CrestTabs,
    Footer,
    Title
  }
}
</script>
