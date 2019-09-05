import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		globaltagmap: {
			/*
			tagName: {
				"globalTagName": "string",
				"tagName": "string",
				"record": "string",
				"label": "string"
			}
			*/
		}
	},
	getters: {
	},
	mutations: {
		mergeNewGlobalTagMap(state, globaltagmap) {
			let tagName = globaltagmap.tagName;
			Vue.set(state.globaltagmap, tagName, globaltagmap)
		}
	},
	actions: {
		createGlobalTagMap({commit}, setGlobalTagMap) {
			const config = {'Content-Type': 'application/json'};
			const data = JSON.stringify({globalTagName: setGlobalTagMap.globalTagName, tagName: setGlobalTagMap.tagName,
				record: setGlobalTagMap.record, label: setGlobalTagMap.label});
			return axios
			.post(`${Vue.prototype.apiName}/globaltagmaps`, data, {headers: config})
			.then(response => response.data)
			.then(globaltagmap => commit('mergeNewGlobalTagMap', globaltagmap))
			.catch(error => { return Promise.reject(error) });
		},
	}
}
