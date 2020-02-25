export const state = () => ({
  cassandraNodes: []
})

export const mutations = {
  updateCassandraNodes (state, cassandraNodes) {
    state.cassandraNodes = cassandraNodes;
  },
}

export const getters = () => ({
  getCassandraNodes (state) {
    return state.cassandraNodes
  }
})
