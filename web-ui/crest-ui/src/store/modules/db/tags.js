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
		},
		tag_for_globaltag: {
			/*
	        globaltagname: [ ...tags ]
			*/
		}
	},
	getters: {
		getTag: (state) => {
			return state.tag;
		},
		getTagForGlobaltag : (state) => (globalTagName) => {
            if (!globalTagName || !state.tag_for_globaltag.hasOwnProperty(globalTagName)) {
                return [];
            }
            return state.tag_for_globaltag[globalTagName];
        },
		getTaglist: (state) => {
			let tag_list = [];
			const tag = Object.entries(state.tag);
			for (var i = 0; i < tag.length; i++){
					tag_list.push(tag[i][1]);
			}
			return tag_list;
		}
	},
	mutations: {
		mergeTags(state, tags_list) {
			tags_list.forEach(tag => {
				let name = tag.name;
				if (!(name in state.tag)) {
					Vue.set(state.tag, name, tag);
				}
			});
		},
		mergeTagsForGlobaltag(state, {gtname, tags_list}) {
            Vue.set(state.tag_for_globaltag, gtname, tags_list);
        },
		mergeNewTag(state, tag) {
			let name = tag.name;
			if (!(name in state.tag)) {
				Vue.set(state.tag, name, tag)
			}
		},
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
		fetchTagByGlobalTags({commit}, getGlobalTag) {
			const gtname = getGlobalTag.name;
			const record = getGlobalTag.record;
			const label = getGlobalTag.label;
			const params = `record=` + record + `&label=` + label;
			return axios
			.get(`/crestapi/globaltags/${gtname}/tags?${params}`)
			.then(response => response.data)
			.then(tags_list => {commit('mergeTagsForGlobaltag', {gtname, tags_list})})
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
