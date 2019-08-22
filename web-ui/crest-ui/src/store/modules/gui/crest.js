export default {
    namespaced: true,
    state: {
        selectedGlobalTag: "",
        selectedTag: "",
        selectedIov: "",
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