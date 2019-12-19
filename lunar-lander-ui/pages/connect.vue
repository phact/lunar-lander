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
      <v-btn v-on:click="connect()">Connect</v-btn>
    </v-flex>
  </v-layout>
</template>
<script>
import axios from 'axios'
export default {
  methods: {
    async connect() {
      const data = await axios.post('/hello', {
        test: this.contactpoints,
      })
      if (!data.err) {
        console.log(data)
      }
    }
  }
}
</script>
