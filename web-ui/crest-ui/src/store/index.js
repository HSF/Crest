import Vue from 'vue'
import Vuex from 'vuex'
import db from './modules/db'
import gui from './modules/gui'

Vue.use(Vuex)

export default new Vuex.Store({
  strict: process.env.NODE_ENV !== 'production',
  modules: {
    db,
    gui
  },
})

