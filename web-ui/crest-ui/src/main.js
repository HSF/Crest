import Vue from 'vue'
import Buefy from 'buefy'
import 'buefy/dist/buefy.css'

import App from './App.vue'

// Constants

Vue.config.productionTip = false
Vue.prototype.apiHost = "localhost"
Vue.prototype.apiPort = "8080"
Vue.use(Buefy)

new Vue({
  render: h => h(App),
}).$mount('#app')