<template>
  <section class="hero is-info">
    <div class="hero-head">
      <header class="navbar">
    <div class="container">
    <nav class="navbar is-transparent">
      <div class="navbar-brand">
        <a class="navbar-item" href="https://atlas.web.cern.ch">
          <img src="../assets/atlas-logo-ref-cmyk-h.png" alt="ATLAS Experiment" width="140" height="120px">
        </a>
      </div>
      <div id="navbarStaticLinks" class="navbar-menu">
        <div class="navbar-start">
          <a class="navbar-item" href="/nswui/index.html">
            <span class="icon">
              <i class="fas fa-home"></i>
            </span>
            <span>Home</span>
          </a>
          <a class="navbar-item" href="https://crest-undertow.web.cern.ch/ext/web/ui/index.html">
            <span class="icon">
              <i class="fas fa-file-alt"></i>
            </span>
            <span>API</span>
          </a>
        </div>
        <div class="navbar-end">
          <div class="navbar-item">
            <span><Connection v-on:select-server="selectServer"/></span>
          </div>
          <div class="navbar-item">
            <div class="field is-grouped">
              <p class="control">
                  <a class="button is-primary" href="https://gitlab.cern.ch/formica/swagger_crestdb">
                  <span class="icon">
                    <i class="fab fa-gitlab"></i>
                  </span>
                  <span>Download</span>
                  </a>
              </p>
            </div>
          </div>
        </div>
      </div>
    </nav>
  </div>
  </header>
</div>
    <div class="hero-body">
      <h1 class="title">
        CREST MANAGER UI
      </h1>
      <h2 class="subtitle">
        Atlas experiment
      </h2>
    </div>
  </section>
</template>
<script>
import Connection from './Connection.vue'
export default {
  name: 'Title',
  props : {
  },
  data: function () {
    return {
      selectedserver: { host: this.apiHost, port: this.apiPort, protocol: this.apiProtocol, url: '' },
    };
  },
  methods: {
    selectServer(serverurl) {
      console.log('Change in title server url '+serverurl);
      this.selectedserver.url = serverurl.url;
      this.$emit('select-server', serverurl )
    },
  },
  components: {
  Connection
  }
};
</script>
