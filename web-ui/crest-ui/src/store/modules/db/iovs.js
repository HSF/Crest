import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		iovs_for_tag: {
			/*
	        tagname: [ ...payloadHashs ]
			*/
		},
		nb_iovs_for_tag: {
			/*
	        tagname: [ ...nbIovs ]
			*/
		}
	},
	getters: {
		getIovForTag : (state) => (tagName) => {
	    if (!tagName || !state.iovs_for_tag.hasOwnProperty(tagName)) {
	      return [];
	    }
	    return state.iovs_for_tag[tagName];
    },
		getNbIovForTag : (state) => (tagName) => {
	    if (!tagName || !state.nb_iovs_for_tag.hasOwnProperty(tagName)) {
	       return [];
	    }
	    return state.nb_iovs_for_tag[tagName];
    }
	},
	mutations: {
		mergeIovsForTag(state, {tagname, iovs_list}) {
            Vue.set(state.iovs_for_tag, tagname, iovs_list.resources);
        },
        mergeNbIovs(state, {tagname, iovs_list}) {
            if (!(tagname in state.nb_iovs_for_tag)) {
                Vue.set(state.nb_iovs_for_tag, tagname, iovs_list.resources[0]);
            }
        }
	},
	actions: {
		fetchIovByTagName({commit}, getIov) {
			const tagname = getIov.tagname;
			const since = getIov.since;
			const until = getIov.until;
			const snapshot = getIov.snapshot;
			const params = `tagname=` + tagname + `&since=` + since + `&until=` + until + `&snapshot=` + snapshot;

			const config = {'Cache-Control': 'no-cache'};

			return axios
			.get(`${Vue.prototype.apiName}/iovs/selectIovs?${params}`,{headers: config})
			.then(response => response.data)
			.then(iovs_list => {commit('mergeIovsForTag', {tagname, iovs_list})})
			.catch(error => { return Promise.reject(error) });
		},
		createIov({dispatch}, res) {
			const config = {'X-Crest-PayloadFormat': res.iovForm.format};
			const data = new FormData();
			data.append("file", res.setIov.file);
			data.append("tag", res.setIov.tagname);
			data.append("since", res.setIov.since);

			var iovForm = {'tagname':res.iovForm.tagname,'since':res.iovForm.since,'until':res.iovForm.until,'snapshot':res.iovForm.snapshot};

			return axios
			.post(`${Vue.prototype.apiName}/payloads/store`, data, {headers: config})
			.then(response => response.data)
			.then(() => {return dispatch('fetchIovByTagName', iovForm);})
			.catch(error => { return Promise.reject(error) });
		},
		countIovsByTag({commit}, tagname) {
			const params = `tagname=` + tagname;
			return axios
			.get(`${Vue.prototype.apiName}/iovs/getSizeByTag?${params}`)
			.then(response => response.data)
			.then(iovs_list => {commit('mergeNbIovs', {tagname, iovs_list})})
			.catch(error => { return Promise.reject(error) });
		}
	}
}
