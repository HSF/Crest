import Vue from 'vue'

export default {
    namespaced: true,
    state: {
        selectedGlobalTag: "",
        selectedTag: "",
        selectedIov: "",
        selectedPayloadLink: "",
    },
    mutations: {
        selectGlobalTag(state, name) {
            state.selectedGlobalTag = name;
        },
        selectTag(state, name) {
            state.selectedTag = name;
        },
        selectIov(state, payloadHash) {
          state.selectedIov = payloadHash;
          state.selectedPayloadLink = Vue.prototype.apiName+'/payloads/'+payloadHash;
        },
    },
    actions: {
        selectGlobalTag({commit}, name) {
            commit('selectGlobalTag', name);
        },
        selectTag({commit}, name) {
            commit('selectTag', name);
        },
        selectIov({commit}, payloadHash) {
            commit('selectIov', payloadHash);
        },
    }
}
