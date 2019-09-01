import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		globaltag: {
			/*
			name: {
				"name": "string",
				"validity": 0,
				"description": "string",
				"release": "string",
				"insertionTime": "2019-06-18T13:36:16.662+0000",
				"snapshotTime": "2019-06-18T13:45:51.825+0000",
				"scenario": "string",
				"workflow": "string",
				"type": "string",
				"snapshotTimeMilli": "2019-06-18T13:36:16.662+0000",
				"insertionTimeMilli": "2019-06-18T13:45:51.825+0000"
			}
			*/
		}
	},
	getters: {
		getGlobalTag: (state) => {
			return state.globaltag;
		},
		getGlobalTaglist: (state) => {
			let globaltag_list = [];
			const globaltag = Object.entries(state.globaltag);
			for (var i = 0; i < globaltag.length; i++){
				globaltag_list.push(globaltag[i][1]);
			}
			return globaltag_list;
		}
	},
	mutations: {
		mergeGlobalTags(state, globaltags_list) {
			globaltags_list.forEach(globaltag => {
				let name = globaltag.name;
				if (!(name in state.globaltag)) {
					Vue.set(state.globaltag, name, globaltag);
				}
			});
		},
		mergeNewGlobalTag(state, globaltag) {
			let name = globaltag.name;
			if (!(name in state.globaltag)) {
				Vue.set(state.globaltag, name, globaltag)
			}
		},
	},
	actions: {
		fetchGlobalTagsByName({commit}, name) {
			const params = `by=name:` + name;
			return axios
			.get(`/api/globaltags?${params}`)
			.then(response => response.data)
			.then(globaltags_list => {commit('mergeGlobalTags', globaltags_list)})
			.catch(error => { return Promise.reject(error) });
		},
		createGlobalTag({commit}, setGlobalTag) {
			const config = {'Content-Type': 'application/json'};
			const data = JSON.stringify({name: setGlobalTag.name, validity: setGlobalTag.validity,
				description: setGlobalTag.description, release: setGlobalTag.release,
				scenario: setGlobalTag.scenario, workflow: setGlobalTag.workflow,
				type: setGlobalTag.type});
			return axios
			.post(`/api/globaltags`, data, {headers: config})
			.then(response => response.data)
			.then(globaltag => commit('mergeNewGlobalTag', globaltag))
			.catch(error => { return Promise.reject(error) });
		},
	}
}
