import Vue from 'vue'
import axios from 'axios'

export default {
	namespaced: true,
	state: {
		payload: {
		}
	},
	getters: {
		getPayload: (state) => {
			return state.payload;
		}
	},
	mutations: {
		mergePayload(state, payload) {
			let hash = payload.hash;
			if (!(hash in state.payload)) {
				Vue.set(state.payload, hash, payload);
			}
		}
	},
	actions: {
		fetchPayloadMeta({commit}, iov) {
			const params = iov;
			return axios
			.get(`/api/payloads/${params}/meta`)
			.then(response => response.data)
			.then(payload => {commit('mergePayload', payload)})
			.catch(error => { return Promise.reject(error) });
		}
	}
}
