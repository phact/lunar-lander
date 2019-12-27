<template>
  <v-layout
    column
    justify-center
    align-center>
    <v-flex
      xs12
      sm8
      md6
    >
    <div class="text-center">
      <logo />
    </div>
    </v-flex>
 
      <v-card
        min-width="600px"
        width="100%"
      >
      <v-card-text>
       <v-text-field
         v-model="contactpoints"
         label="Contact points"
         required
       ></v-text-field>
       <v-text-field
         v-model="sshUser"
         label="SSH User"
         required
       ></v-text-field>
       <v-textarea
         outlined
         v-model="privateKey"
         label="SSH Private Key"
       ></v-textarea>
 
       
   <!--    <ul> -->
   <!--    <li v-for="mission in missions"> -->
   <!--    <span -->
   <!--    >{{mission}}</span> -->
   <!--    </li> -->
   <!--    </ul> -->


       <v-data-iterator
          v-if="cassandraNodes.length > 0"
          :items="cassandraNodes"
          item-key="broadcastAddress"
          :items-per-page="4"
          :single-expand="expand"
          hide-default-footer
        >
          <template v-slot:default="{ items, isExpanded, expand }">
            <v-row>
              <v-col
                v-for="item in items"
                :key="item.broadcastAddress"
                cols="12"
                sm="6"
                md="4"
                lg="3"
              >
                <v-card>
                  <v-card-title>
                    <h4>{{ item.broadcastAddress }}</h4>
                  </v-card-title>
                  <v-switch
                    :input-value="isExpanded(item)"
                    :label="isExpanded(item) ? 'Expanded' : 'Closed'"
                    class="pl-4 mt-0"
                    @change="(v) => expand(item, v)"
                  ></v-switch>
                  <v-divider></v-divider>
                  <v-list v-if="isExpanded(item)" dense>
                    <v-list-item>
                      <v-list-item-content>Listen Address:</v-list-item-content>
                      <v-list-item-content class="align-end">{{ item.listenAddress }}</v-list-item-content>
                    </v-list-item>
                    <v-list-item>
                      <v-list-item-content>Broadcast RPC Address:</v-list-item-content>
                      <v-list-item-content class="align-end">{{ item.broadcastRpcAddress }}</v-list-item-content>
                    </v-list-item>
                  </v-list>
                </v-card>
              </v-col>
            </v-row>
          </template>
        </v-data-iterator>
        
      <v-btn v-if="cassandraNodes.length === 0" v-on:click="connect()">Connect</v-btn>
      <v-select
        v-if="cassandraNodes.length > 0" 
        :items="missions"
        label="Mission"
        v-model="missionName"
        outlined
      ></v-select>
      <v-btn v-if="cassandraNodes.length > 0 && missionName != ''" v-on:click="initiateSequence()">Initiate Sequence</v-btn>

      </v-card-text>
   </v-card>
  </v-layout>
</template>
<script>
import axios from 'axios'
import Logo from '~/components/Logo.vue'

const fetch = require('node-fetch');


export default {
    data: () => {
      let data = {
          missions: ["puppies","kittens"],
          cassandraNodes: [],
          contactpoints: "",
          sshUser: "",
          privateKey: "",
          missionName: "",
          expand: false
      };
      return data;
    },
    components: {
      Logo,
    },
    async asyncData() {
      let data = await fetch("/missions")
                .then(res => {
                    return res.json()
                })

      //this.$data.missions = data;
      return { 
          missions: data,
      };
    },
 
    head () {
      return {
        title: 'Lunar Lander',
        meta: [
        ]
      }
    },
   methods: {
    async connect() {
      const data = await axios.post('/connect', {
        contactPoints: this.contactpoints,
        sshUser: this.sshUser,
        privateKey: this.privateKey,
      })
      if (!data.err) {
        this.$data.cassandraNodes = data.data;
      }
    },
    async initiateSequence() {
      const data = await axios.get('/initiateSequence/' + this.missionName)
      if (!data.err) {
        this.$data.cassandraNodes = data.data;
      }
 
    }
  }
}
</script>
