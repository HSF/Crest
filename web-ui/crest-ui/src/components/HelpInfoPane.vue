<template>
<div class="container">
      <div class="level is-mobile">
        <div class="level-left">
          <div class="level-item">
            <button class="button block" @click="isHelpShow = !isHelpShow">
              <b-icon icon="help-circle-outline"></b-icon>
              <span>Help</span>
            </button>
          </div>
          <div class="level-item">
              <button class="button block" @click="isInfoShow = !isInfoShow">
                <b-icon icon="eye"></b-icon>
                <span>Info</span>
              </button>
          </div>
          <div v-for="blnk in links" class="level-item">
            <button class="button is-info" v-on:click="$emit('child-switchtab',blnk.seltab)">
              <b-icon icon="arrow-right"></b-icon>
              <span>{{blnk.btnlabel}}</span>
            </button>
          </div>
        </div>
      </div>
      <b-notification :active.sync="isHelpShow" type="is-info">
        <span v-html="helpmessage"></span>
      </b-notification>
      <b-notification :active.sync="isInfoShow" type="is-warning">
        <span v-html="infomessage"></span>
      </b-notification>
      <b-notification auto-close :duration=3000 :type="notiftype" :active.sync="isActive">
        {{notifytext}}
      </b-notification>
</div>
</template>

<script>

export default {
  name: 'HelpInfoPane',
  props : {
    infomessage : String,
    helpmessage : String,
    notifytext : String,
    notiftype : String,
    links : Array,
  },
  data: function () {
    return {
        isActive: false,
        isInfoShow : false,
        isHelpShow : false,
      };
  },
};
</script>
