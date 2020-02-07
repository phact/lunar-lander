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
                    :label="isExpanded(item) ? 'Close' : 'Expand'"
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

        
      <v-btn v-if="cassandraNodes.length === 0" v-on:click="connect()">Connect</v-btn>
      <v-btn v-if="cassandraNodes.length != 0" v-on:click="connect()">Reconnect</v-btn>

      <v-select
        v-if="cassandraNodes.length > 0" 
        :items="missionNames"
        label="Mission"
        v-model="missionName"
        outlined
      ></v-select>
      <v-btn v-if="cassandraNodes.length > 0 && missionName != ''" v-on:click="rollingDeployment()">Rolling Deployment</v-btn>
      <v-btn v-if="cassandraNodes.length > 0 && missionName != ''" v-on:click="streamRollingDeployment()">Stream Rolling Deployment</v-btn>
      <v-btn v-if="cassandraNodes.length > 0 && missionName != ''" v-on:click="canaryDeployment()">Canary Deployment</v-btn>
      <v-btn v-on:click="stream()">Stream</v-btn>

      <v-data-table
        v-if="sequenceResults.length >0"
        :headers="Object.keys(sequenceResults[0]).map(x => {return {'text':x, 'value':x}})"
        :items="sequenceResults"
        :item-key="''+command + '-' + host"
        :items-per-page="5"
        class="elevation-1"
      ></v-data-table>

      </v-container fluid>

      </v-card-text>
   </v-card>
  </v-layout>
</template>
<script>
import Logo from '~/components/Logo.vue'
import { mapMutations } from 'vuex'
import requestsMixin from '~/mixins/requests.js'




export default {
    mixins: [requestsMixin],
    data: () => {
      let data = {
          missionNames: ["puppies","kittens"],
          cassandraNodes: [],
          sequenceResults: [],
          contactpoints: "",
          testStream: "",
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
    async asyncData({ $axios }) {
      let data = await $axios.$get("/missionNames")
                .then(res => {
                    return res
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
    snackTime: function (snack) {
      this.setSnack(snack)
      //this.$router.push('/')
    },
    ...mapMutations({
      setSnack: 'snackbar/setSnack'
    }),
    async connect() {
      const data = await this.$axios.$post('/connect', {
        contactPoints: this.contactpoints,
        sshUser: this.sshUser,
        privateKey: this.privateKey,
      }).catch(function (error) {
        return {err: error.response.data}
      })
      if (!data.err) {
        this.$data.cassandraNodes = data;
        this.snackTime("Connection created");
      }else {
        this.snackTime(data.err);
      }
    },
    async rollingDeployment() {
      const data = await this.$axios.$get('/rollingDeployment/' + this.missionName)
      if (!data.err) {
        this.$data.sequenceResults = data;
      }
 
    },
    async canaryDeployment() {
      const data = await this.$axios.$get('/canaryDeployment/' + this.missionName)
      if (!data.err) {
        this.$data.sequenceResults = data;
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
    streamRollingDeployment (){
        this.$data.sequenceResults = [];
        this.streamingRequest({
            url: this.$axios.defaults.baseURL + 'streamRollingDeployment/' + this.missionName,
            error: function(response){
                console.log(response);
            },
            success: function(response, vueComponent, field){
                var reader = response.body.getReader();
                const STATUS_DELIMITER = "\n\n";

                let readChunk  = function(reader, i, field, vueComponent){

                    reader.read().then(function(result){
                        var decoder = new TextDecoder();
                        var chunk = decoder.decode(result.value || new Uint8Array, {stream: !result.done});
                        chunk.split("\n").forEach((chunkedLine) => {
                            if (chunkedLine.trim().length != 0){
                                var chunkObject = {
                                    "index" : i,
                                    "msg": chunkedLine,
                                }
                                i = i + 1;

                                //console.log(chunkedLine);

                                if (chunkedLine.indexOf(STATUS_DELIMITER) != -1){
                                    status = chunkedLine.substr(12);
                                    chunkObject.msg = "exited with Status code " + status;
                                    if (status == 0){
                                        console.log(command + " Succeeded", "Success")
                                    }
                                    else if (status == 1){
                                        console.log(command + " Not Found", "Error")
                                    }
                                    else {
                                        console.log(command + " Failed", "Error")
                                    }
                                }

                                vueComponent.$data[field][chunkObject.index] = JSON.parse(chunkObject.msg);

                                vueComponent.$forceUpdate();
                                console.log(chunkObject);

                            }
                        });

                        if (result.done) {
                            console.log("done")
                            return;
                        } else {
                            return readChunk(reader, i, field, vueComponent);
                        }
                    });
     
                }

                readChunk(reader,  0, field, vueComponent);

                if (response.status == 200){
                    console.log("started streaming")
                }else{
                    console.log("Launch Command Failed")
                }
            },
            method: "GET",
            vueComponent: this,
            field: "sequenceResults"
        })
    },
    stream (){
        this.$data.sequenceResults = [];
        this.streamingRequest({
            url: this.$axios.defaults.baseURL + 'stream/' + "puppies",
            error: function(response){
                console.log(response);
            },
            success: function(response, vueComponent, field){
                var reader = response.body.getReader();
                const STATUS_DELIMITER = "\n\n";

                let readChunk  = function(reader, i, field, vueComponent){

                    reader.read().then(function(result){
                        var decoder = new TextDecoder();
                        var chunk = decoder.decode(result.value || new Uint8Array, {stream: !result.done});
                        chunk.split("\n").forEach((chunkedLine) => {
                            if (chunkedLine.trim().length != 0){
                                var chunkObject = {
                                    "index" : i,
                                    "msg": chunkedLine,
                                }
                                i = i + 1;

                                //console.log(chunkedLine);

                                if (chunkedLine.indexOf(STATUS_DELIMITER) != -1){
                                    status = chunkedLine.substr(12);
                                    chunkObject.msg = "exited with Status code " + status;
                                    if (status == 0){
                                        console.log(command + " Succeeded", "Success")
                                    }
                                    else if (status == 1){
                                        console.log(command + " Not Found", "Error")
                                    }
                                    else {
                                        console.log(command + " Failed", "Error")
                                    }
                                }

                                vueComponent.$data[field][chunkObject.index] = chunkObject.msg;

                                vueComponent.$forceUpdate();
                                console.log(chunkObject);

                            }
                        });

                        if (result.done) {
                            console.log("done")
                            return;
                        } else {
                            return readChunk(reader, i, field, vueComponent);
                        }
                    });
     
                }

                readChunk(reader,  0, field, vueComponent);

                if (response.status == 200){
                    console.log("started streaming")
                }else{
                    console.log("Launch Command Failed")
                }
            },
            method: "GET",
            vueComponent: this,
            field: "sequenceResults"
        })
      }
  },
   computed: {
      numberOfPages () {
        return Math.ceil(this.cassandraNodes.length / this.itemsPerPage)
      },
    },
}
</script>
