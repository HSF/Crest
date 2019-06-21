export default {
    namespaced: true,
    state: {
        selectedTagname: "",
        selectedSince: 0,
        selectedUntil: "INF",
        selectedSnapshot: '0',
    },
    mutations: {
        selectTagname(state, tagname) {
            state.selectedTagname = tagname;
        },
        selectSince(state, since) {
            state.selectedSince = since;
        },
        selectUntil(state, until) {
            state.selectedUntil = until;
        },
        selectSnapshot(state, snapshot) {
            state.selectedSnapshot = snapshot;
        },
    },
    actions: {
        selectTagname({commit}, tagname) {
            commit('selectTagname', tagname);
        },
        selectSince({commit}, since) {
            commit('selectSince', since);
        },
        selectUntil({commit}, until) {
            commit('selectUntil', until);
        },
        selectSnapshot({commit}, snapshot) {
            commit('selectSnapshot', snapshot);
        },
    }
}