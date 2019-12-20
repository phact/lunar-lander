<template>
  <v-layout>
    <div class="text-center">
      <logo />
    </div>
    <v-flex class="text-center">
      <v-text-field
        v-model="contactpoints"
        label="Contact points"
        required
      ></v-text-field>

    <ul>
      <li v-for="(cassandraNode, i) in cassandraNodes" :key="i">
        <span>{{ cassandraNode.broadcastAddress}}</span>
        <span>{{ cassandraNode.listenAddress}}</span>
        <span>{{ cassandraNode.broadcastRpcAddress}}</span>
      </li>
    </ul>
 
      <v-btn v-on:click="connect()">Connect</v-btn>
      <node-view :cn="cn"/>
   </v-flex>
  </v-layout>
</template>
<script>
import axios from 'axios'
import NodeView from '~/components/NodeView.vue'

export default {
    data: () => ({
     cassandraNodes:[],
    }),
    methods: {
    async connect() {
      const data = await axios.post('/connect', {
        contactPoints: this.contactpoints,
      })
      if (!data.err) {
        this.$data.cassandraNodes = data.data;
      }
    }
  }
}
</script>
