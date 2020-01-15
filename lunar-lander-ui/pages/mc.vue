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


       <v-container fluid>

       <v-data-iterator
          v-if="cassandraNodes.length > 0"
          :items="cassandraNodes"
          item-key="broadcastAddress"
          :items-per-page="itemsPerPage"
          :page="page"
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



          <template v-slot:footer>
            <v-row class="mt-2" align="center" justify="center">
              <span class="grey--text">Items per page</span>
              <v-menu offset-y>
                <template v-slot:activator="{ on }">
                  <v-btn
                    dark
                    text
                    color="primary"
                    class="ml-2"
                    v-on="on"
                  >
                    {{ itemsPerPage }}
                    <v-icon>mdi-chevron-down</v-icon>
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item
                    v-for="(number, index) in itemsPerPageArray"
                    :key="index"
                    @click="updateItemsPerPage(number)"
                  >
                    <v-list-item-title>{{ number }}</v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>

              <v-spacer></v-spacer>

              <span
                class="mr-4
                grey--text"
              >
                Page {{ page }} of {{ numberOfPages }}
              </span>
              <v-btn
                fab
                dark
                color="blue darken-3"
                class="mr-1"
                @click="formerPage"
              >
                <v-icon>mdi-chevron-left</v-icon>
              </v-btn>
              <v-btn
                fab
                dark
                color="blue darken-3"
                class="ml-1"
                @click="nextPage"
              >
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </v-row>
          </template>

      </v-data-iterator>

      </v-container fluid>
        
      <v-btn v-if="cassandraNodes.length === 0" v-on:click="connect()">Connect</v-btn>
      <v-select
        v-if="cassandraNodes.length > 0" 
        :items="missionNames"
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
          missionNames: ["puppies","kittens"],
          cassandraNodes: [],
          contactpoints: "",
          sshUser: "",
          privateKey: "",
          missionName: "",
          expand: false,
          page: 1,
          itemsPerPage: 10,
          itemsPerPageArray: [10, 20, 50]
      };
      return data;
    },
    components: {
      Logo,
    },
    async asyncData() {
      let data = await fetch("/missionNames")
                .then(res => {
                    return res.json()
                })

      //this.$data.missionNames = data;
      return { 
          missionNames: data,
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
 
    },
    nextPage () {
      if (this.page + 1 <= this.numberOfPages) this.page += 1
    },
    formerPage () {
      if (this.page - 1 >= 1) this.page -= 1
    },
    updateItemsPerPage (number) {
      this.itemsPerPage = number
    },
  },
   computed: {
      numberOfPages () {
        return Math.ceil(this.cassandraNodes.length / this.itemsPerPage)
      },
    },
}
</script>
