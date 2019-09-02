<template>
  <section class="hero is-primary">
    <div class="hero-head">
      <nav class="navbar">
        <div class="container">
          <div class="navbar-brand">
            <a class="navbar-item" href="http://irfu.cea.fr/">
              <img src="../assets/irfu_logo.png" alt="Logo">
            </a>
            <a class="navbar-item" href="https://svom.fr">
              <img src="../assets/svom-logo.png" alt="Logo">
            </a>
            <div class="navbar-burger" @click="showNav = !showNav" :class="{ 'is-active': showNav }">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
          <div class="navbar-menu" :class="{ 'is-active': showNav }">
            <div class="navbar-end">
              <a class="navbar-item is-active" href="index.html">
                <span class="icon">
                  <i class="fas fa-home"></i>
                </span>
                <span>Home</span>
              </a>
              <a class="navbar-item" href="/ext/web/ui/index.html">
                <span class="icon">
                  <i class="fas fa-file-alt"></i>
                </span>
                <span>API</span>
              </a>
              <span class="navbar-item">
                <a class="button is-primary is-inverted" href="https://gitlab.cern.ch/formica/swagger_crestdb">
                  <span class="icon">
                    <i class="fab fa-github"></i>
                  </span>
                  <span>Download</span>
                </a>
              </span>
            </div>
          </div>
        </div>
      </nav>
    </div>
    <div class="hero-body">
      <div class="container">
        <h1 class="title">
          Crest Manager UI
        </h1>
        <h2 class="subtitle">
          Svom experiment
        </h2>
      </div>
    </div>
  </section>
</template>
<script>
export default {
  name: 'Title',
  props : {
  },
  data: function () {
    return {
      selectedserver: { host: this.apiHost, port: this.apiPort, protocol: this.apiProtocol, url: '' },
      showNav: false
    };
  },
  methods: {

  },
  components: {
  }
};
</script>
