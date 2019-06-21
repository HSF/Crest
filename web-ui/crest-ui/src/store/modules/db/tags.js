import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		tag: {
			/*
			name: {
				"name": "string",
				"timeType": "string",
				"payloadSpec": "string",
				"synchronization": "string",
				"description": "string",
				"lastValidatedTime": 0,
				"endOfValidity": 0,
				"insertionTime": "2019-06-18T13:36:16.662+0000",
				"modificationTime": "2019-06-18T13:45:51.825+0000"
			}
			*/
		}
	},
	getters: {
		getTag: (state) => {
			return state.tag;
		}
	},
	mutations: {
		mergeTags(state, tags_list) {
			state.tag = {};
			tags_list.forEach(tag => {
				let name = tag.name;
				if (!(name in state.tag)) {
					Vue.set(state.tag, name, tag);
				}
			})
		},
		mergeNewTag(state, tag) {
			let name = tag.name;
			if (!(name in state.tag)) {
				Vue.set(state.tag, name, tag)
			}
		}
	},
	actions: {
		fetchTagByName({commit}, name) {
			const params = `by=name:` + name;
			return axios
			.get(`/crestapi/tags?${params}`)
			.then(response => response.data)
			.then(tags_list => {commit('mergeTags', tags_list)})
			.catch(error => { return Promise.reject(error) });
		},
		createTag({commit}, setTag) {
			const config = {'Content-Type': 'application/json'};
			const data = JSON.stringify({name: setTag.name, description: setTag.description,
				payloadSpec: setTag.payloadSpec, endOfValidity: setTag.endOfValidity,
				lastValidatedTime: setTag.lastValidatedTime, timeType: setTag.timeType,
				synchronization: setTag.synchronization});
			return axios
			.post(`/crestapi/tags`, data, {headers: config})
			.then(response => response.data)
			.then(tag => commit('mergeNewTag', tag))
			.catch(error => { return Promise.reject(error) });
		},
	}
}