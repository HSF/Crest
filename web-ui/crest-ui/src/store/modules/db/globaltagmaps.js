import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		globaltagmap: {
			/*
			globalTagName: {
				"globalTagName": "string",
				"tagName": "string",
				"record": "string",
				"label": "string"
			}
			*/
		},
		globaltag_for_tag: {
			/*
    		tagname: [ ...globaltagmaps ]
			*/
		},
	},
	getters: {
		getGlobalTagMapForTag : (state) => (tagName) => {
            if (!tagName || !state.globaltag_for_tag.hasOwnProperty(tagName)) {
                return [];
            }
            return state.globaltag_for_tag[tagName];
        }
	},
	mutations: {
		mergeGlobalTagMapForTagName(state, {name, globaltagmap_list}) {
			if (!(name in state.globaltag_for_tag)) {
				Vue.set(state.globaltag_for_tag, name, globaltagmap_list);
			}
		},
		mergeNewGlobalTagMap(state, globaltagmap) {
			let tagName = globaltagmap.globalTagName;
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
		fetchGlobalTagsByTagName({commit}, name) {
			const config = {'X-Crest-MapMode': 'BackTrace'};
			return axios
			.get(`${Vue.prototype.apiName}/globaltagmaps/${name}`, {headers: config})
			.then(response => response.data)
			.then(globaltagmap_list => {commit('mergeGlobalTagMapForTagName', {name, globaltagmap_list})})
			.catch(error => { return Promise.reject(error) });
		}
	}
}
