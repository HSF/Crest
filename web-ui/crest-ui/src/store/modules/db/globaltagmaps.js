import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		globaltagmap: {
			/*
			label: {
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
			let label = globaltagmap.label;
			if (!(label in state.globaltagmap)) {
				Vue.set(state.globaltagmap, label, globaltagmap)
			}
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
