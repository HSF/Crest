<template>
<div id="app" class="container">
  <Title />
  <CrestTabs />
  <p>{{msg}}: access api on {{apiHost}} and port {{apiPort}}</p>
</div>
</template>
<script>
import Title from './components/Title.vue'
import CrestTabs from './components/CrestTabs.vue'

export default {
  name: 'app',
  data () {
    return {
      msg: 'Welcome to Crest Browser'
    }
  },
  components: {
    CrestTabs,
    Title
  }
}
</script>
