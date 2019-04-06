<template>
    <section>
        <button class="button is-primary" @click="isComponentModalActive = true">
          <b-icon icon="link-variant"></b-icon>
          <span>Connect</span>
        </button>
        <b-modal :active.sync="isComponentModalActive" has-modal-card>
            <ConnectionForm v-on:select-server="selectServer" />
        </b-modal>

    </section>
</template>

<script>
  import ConnectionForm from './ConnectionForm.vue';
  export default {
    name: 'Connection',
    props : {
    },
    data: function () {
      return {
        isComponentModalActive : false,
        selectedserver: { url: 'http://crest-undertow.web.cern.ch:80/crestapi' },
      };
    },
    methods: {
        selectServer(serverurl) {
          console.log('Change active server url '+serverurl)
          this.$emit('select-server', serverurl )
        },
        openModal() {
            this.$modal.open({
                  parent: this,
                  component: ConnectionForm,
                  hasModalCard: true
            })
        },
    },
    components: {
      ConnectionForm
    }
  }
</script>
