import Vue from 'vue'
import Buefy from 'buefy'
import 'buefy/dist/buefy.css'

import App from './App.vue'

// Constants

Vue.config.productionTip = false
//Vue.prototype.apiHost = "localhost"
//Vue.prototype.apiPort = "8080"
Vue.prototype.apiHost = process.env.VUE_APP_REST_API
Vue.prototype.apiPort = process.env.VUE_APP_REST_PORT
Vue.prototype.apiName = process.env.VUE_APP_API_NAME
Vue.prototype.apiProtocol = process.env.VUE_APP_API_PROTOCOL
Vue.prototype.apiTitle = process.env.VUE_APP_TITLE

Vue.use(Buefy)

new Vue({
  render: h => h(App),
}).$mount('#app')
