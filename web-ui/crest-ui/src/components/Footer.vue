<template>
  <footer class="footer">
    <div class="level">
    <div class="level-left">
      <div class="level-item">
      <div class="content has-text-left">
        <p>
          <strong>CREST</strong> by <a href="https://atlas.web.cern.ch">Atlas</a>.
        </p>
      </div>
    </div>
    <div class="level-item">
      <div class="content has-text-centered">
        <b-notification auto-close :duration=3000 :type="notificationType" :active.sync="isActive" :closable="false">
          {{notifytext}}
        </b-notification>
      </div>
    </div>
  </div>

  <div class="level-left">
    <div class="level-item">
    <div class="content has-text-left">
      <p>
        <b>Server: </b>{{ selectedserver }}
      </p>
    </div>
  </div>
</div>
<div class="level-right">
  <div class="level-item">
      <p><img style='height: 60px; width: 60px; object-fit: contain' src="../assets/irfu_logo.png" /></p>
  </div>
</div>
</div>
  </footer>
</template>

<script>

export default {
  name: 'Footer',
  props : {
    selectedserver : String,
    notiftype : String,
    notifytext : String,
  },
  data: function () {
    return {
      isActive : false,
    };
  },
  methods: {
    alertCustom() {
        this.$dialog.alert({
            title: this.notiftype,
            message: this.notifytext,
            confirmText: 'OK'
        })
    },
    success() {
        this.$toast.open({
            message: this.notifytext,
            type: 'is-success'
        })
    },
  },
  watch: {
    notifytext : function(val) {
//      this.isActive=true;
//      this.alertCustom();
      this.success()
    }
  },
  computed: {
    notificationType() {
      return this.notiftype
    }
  },
  components: {
  }
};
</script>
