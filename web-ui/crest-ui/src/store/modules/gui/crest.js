export default {
    namespaced: true,
    state: {
        selectedTag: "",
        selectedIov: "",
    },
    mutations: {
        selectTag(state, name) {
            state.selectedTag = name;
        },
        selectIov(state, payloadHash) {
            state.selectedIov = payloadHash;
        },
    },
    actions: {
        selectTag({commit}, name) {
            commit('selectTag', name);
        },
        selectIov({commit}, payloadHash) {
            commit('selectIov', payloadHash);
        },
    }
}