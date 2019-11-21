import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		folder: {
			/*
			nodeFullpath: {
				"nodeFullpath": "string",
				"schemaName": "string",
				"nodeName": "string",
				"nodeDescription": "string",
				"tagPattern": "string",
				"groupRole": "string"
			}
			*/
		}
	},
	getters: {
		getFolder: (state) => {
			return state.folder;
		},
		getFolderlist: (state) => {
			let folder_list = [];
			const folder = Object.entries(state.folder);
			for (var i = 0; i < folder.length; i++){
				folder_list.push(folder[i][1]);
			}
			return folder_list;
		}
	},
	mutations: {
		mergeFolders(state, folders_list) {
			folders_list.forEach(folder => {
				let nodeFullpath = folder.nodeFullpath;
				if (!(nodeFullpath in state.folder)) {
					Vue.set(state.folder, nodeFullpath, folder);
				}
			});
		}
	},
	actions: {
		fetchFolder({commit}) {
			return axios
			.get(`${Vue.prototype.apiName}/folders`)
			.then(response => response.data.resources)
			.then(folders_list => {commit('mergeFolders', folders_list)})
			.catch(error => { return Promise.reject(error) });
		}
	}
}
