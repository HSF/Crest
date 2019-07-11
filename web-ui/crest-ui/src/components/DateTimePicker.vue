<template>
  <section>
    <div id="app">
      <b-field>
        <div style="display:flex;">
          <b-datepicker placeholder="Select date" icon="calendar-today" v-model="date" />    
          <b-timepicker placeholder="Select time" icon="clock" v-model="heure" />
        </div>
      </b-field>
    </div>
  </section>
</template>

<script>
import { mapState } from 'vuex'
export default {
    name: 'DateTimePicker',
    props : {
    },
    data: function() {
        return {
            date: null,
            heure: null,
        }
    },
    methods: {
        date_heure() {
            var date_complete = new Date();
            var jour = (this.date.getDate() < 10) ? "0" + this.date.getDate() : this.date.getDate();
            var mois = ((this.date.getMonth() + 1) < 10) ? "0" + (this.date.getMonth() + 1) : this.date.getMonth() + 1;
            var annee = this.date.getFullYear();
            var heures = (this.heure.getHours() < 10) ? "0" + this.heure.getHours() : this.heure.getHours();
            var minutes = (this.heure.getMinutes() < 10) ? "0" + this.heure.getMinutes() : this.heure.getMinutes();
            date_complete = (annee + '-' + mois + '-' + jour + 'T' + heures + ':' + minutes + ':00.000+00:00');
            
            // convertir la date en timestamp
            Date.prototype.getUnixTime = function() { return this.getTime()/1000|0 };
            if(!Date.now) Date.now = function() { return new Date(); }
            Date.time = function() { return Date.now().getUnixTime(); }
            
            var timestamp = new Date(date_complete).getUnixTime();
            this.$emit('select-since', timestamp)
            this.$emit('select-until', timestamp)
        }
    },
    watch: {
        date: function() {
            if (this.heure != null){
                this.date_heure();
            }
        },
        heure: function() {
            if (this.date != null){
                this.date_heure();
            }
        },
        selectedSince: function() {
            if ((this.selectedSince == 0) && (this.selectedUntil == 'INF')) {                
                this.date = '';                
                this.heure = '';
            }
        }
    },
    computed: {
        ...mapState('gui/iovForm', ['selectedSince', 'selectedUntil']),
    }
}
</script>

<style>
.dropdown-menu {
    padding-bottom:5px;
}
</style>