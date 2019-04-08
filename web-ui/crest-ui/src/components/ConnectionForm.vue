<template>
<form action="">
    <div class="modal-card" style="width: auto">
        <header class="modal-card-head">
            <p class="modal-card-title">Crest Server</p>
        </header>
        <section class="modal-card-body">
            <b-field label="URL">
                <b-input
                    v-model="url"
                    maxlength="50"
                    required>
                </b-input>
            </b-field>
        </section>
        <footer class="modal-card-foot">
            <button class="button" type="button" @click="close()">Close</button>
            <button class="button is-primary" type="button" @click="save()">Select</button>
        </footer>
    </div>
</form>
</template>

<script>
    export default {
      name: 'ConnectionForm',
      data: function () {
        return {
          url: 'http://crest-undertow.web.cern.ch:80/crestapi',
        };
      },
      methods: {
        save() {
          this.$toast.open({
                   message: 'Saving url '+this.url,
                   type: 'is-success'
          });
          this.$emit('select-server', { url : this.url } );
        },
        close() {
          this.$parent.close();
        }
      }
    }
</script>
