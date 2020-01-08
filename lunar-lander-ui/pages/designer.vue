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
     <v-container fluid>

      <v-select
        v-if="missionName == null || missionName == ''"
        :items="missions"
        label="Mission"
        v-model="missionName"
        value="missionName"
        v-on:change="newType = 'Clone';getSequences();"
        outlined
      ></v-select>
 
       <v-container fluid>
       <h4 v-if="missionName != null && missionName != ''">
         {{missionName}}
       </h4>
       <v-data-iterator
          v-if="sequences.length > 0"
          :items="sequences"
          item-key="name"
          :items-per-page="itemsPerPage"
          :page="page"
          :single-expand="expand"
          hide-default-footer
        >
          <template v-slot:default="{ items, isExpanded, expand }">
            <v-row>
              <v-col
                v-for="(item, j) in items"
                :key="item.command"
                cols="12"
                sm="6"
                md="4"
                lg="3"
              >
                <v-card>
                  <v-card-title>
                    <v-text-field
                      v-model="item.name"
                      label="Sequence Name"
                    >{{ item.name }}</v-text-field>

                    <v-icon color="primary" dark v-on:click="spliceSequence(j)">mdi-delete</v-icon>

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
                      <v-list-item-content>Name:</v-list-item-content>
                      <v-list-item-content class="align-end">{{ item.name }}</v-list-item-content>
                    </v-list-item>
                    <v-list-item>
                      <v-list-item-content>Sequence type:</v-list-item-content>
                      <v-list-item-content class="align-end">{{ item.sequenceType }}</v-list-item-content>
                    </v-list-item>
                    <v-list-item>
                      <v-list-item-content>Expected response:</v-list-item-content>
                      <v-list-item-content class="align-end">{{ item.expectedResponse }}</v-list-item-content>
                    </v-list-item>
 
                    <v-list-item 
                      v-for="(command, i) in item.commands"
                      v-bind:key="command"
                    >
                      <v-list-item-content>Command {{i}}: </v-list-item-content>
                      <v-list-item-content class="align-end">{{ command }}</v-list-item-content>

                      <v-list-item-content>
                        <v-icon 
                          color="primary" 
                          dark 
                          @click.stop="currentcommand=command; currenti=i; currentj=j; commandDialog = true"
                        >
                          mdi-code-braces-box
                        </v-icon>
                        <v-icon color="primary" dark v-on:click="spliceCommand(j,i)">mdi-delete</v-icon>
                      </v-list-item-content>
                    </v-list-item>

                    <v-divider></v-divider>
                    <v-icon color="primary" dark v-on:click="pushCommand(j)">mdi-plus</v-icon>

                  </v-list>



                </v-card>
              </v-col>

              <v-col>
                    <v-icon color="primary" dark v-on:click="pushSequence(i)">mdi-plus</v-icon>
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

        <v-row justify="center">
          <v-dialog v-model="commandDialog" scrollable max-width="300px">
            <template v-slot:activator="{ on }">
            </template>
            <v-card>
              <v-card-title>Command Editor</v-card-title>
              <v-card-text style="height: 300px;">
                <v-spacer></v-spacer>
                <v-textarea style="margin-top:10px"
                  v-model="currentcommand"
                  outlined
                  name="input-7-4"
                  :label="`Command ${currenti}`"
                  :value="currentcommand"
                  @change="changeCommand({currenti}, {currentj}, {currentcommand})" 
                ></v-textarea>
                <span>{{commandResponse}}</span>
              </v-card-text>
              <v-divider></v-divider>
              <v-card-actions>
                <v-btn color="blue darken-1" text @click="commandDialog = false">Close</v-btn>
                <v-btn color="blue darken-1" text @click="executeCommand({currentcommand})">Execute</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>

          <v-dialog v-model="missionDialog" scrollable max-width="300px">
            <template v-slot:activator="{ on }">
            </template>
            <v-card>
              <v-card-title>New Mission</v-card-title>
              <v-card-text style="height: 300px;">
                <v-spacer></v-spacer>
                <v-textarea style="margin-top:10px"
                  v-model="missionName"
                  outlined
                  name="input-7-4"
                  :label="`Mission Name`"
                ></v-textarea>
              </v-card-text>
              <v-divider></v-divider>
              <v-card-actions>
                <v-btn 
                  v-if="newType=='New'"
                  color="blue darken-1" 
                  text 
                  @click="missionDialog = false ; newMission();"
                >
                  New
                </v-btn>
                <v-btn 
                  v-if="newType=='Clone'"
                  color="blue darken-1" 
                  text 
                  @click="missionDialog = false; cloneMission();"
                >
                  Clone
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
 
        </v-row>  


        
      <v-btn v-if="sequences.length !== 0" v-on:click="saveMission()">Save Mission</v-btn>
      <v-btn v-if="sequences.length !== 0" v-on:click="deleteMission()">Delete Mission</v-btn>
      <v-btn v-on:click="missionDialog = true">{{newType}} Mission</v-btn>
      <v-btn v-if="sequences.length !== 0" v-on:click="clearSelectedMission()">Back</v-btn>

      </v-card-text>
      <v-container fluid>
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
          missionName: "",
          sequences: [],
          expand: false,
          commandDialog: false,
          missionDialog: false,
          commandResponse: "",
          currenti: 0,
          currentj: 0,
          currentcommand: "",
          page: 1,
          itemsPerPage: 10,
          itemsPerPageArray: [10, 20, 50],
          newType: "New"
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
    newMission(){
      this.missions.push(this.missionName)
      this.sequences=[{"commands":[]}];
    },
    cloneMission(){
      this.missions.push(this.missionName)
    },
    pushCommand(j){
      this.sequences[j].commands.push("");
    },
    spliceCommand(j,i){
      this.sequences[j].commands.splice(i,1);
    },
    pushSequence(){
      this.sequences.push({ "commands": []});
    },
    spliceSequence(j){
      this.sequences.splice(j,1);
    },
    async saveMission() {
      const data = await axios.post('/mission/', {
        missionName: this.missionName, sequences: this.sequences
      })
      if (!data.err) {
        //this.$data.cassandraNodes = data.data;
      }
    },
    async deleteMission() {
      const data = await axios.post('/deleteMission/', {
        missionName: this.missionName
      })
      if (!data.err) {
        this.$data.missions.splice((this.$data.missions.indexOf(this.missionName)),1)
        this.$data.missionName="";
        this.$data.sequences = [];
        this.$data.newType = "New";
        //this.$data.cassandraNodes = data.data;
      }
    },
   async getSequences() {
      const data = await axios.get('/getSequences/' + this.missionName)
      if (!data.err) {
        this.$data.sequences = data.data;
      }
 
    },
    async changeCommand(currenti,currentj, currentcommand) {
      this.$set(this.$data.sequences[currentj.currentj].commands, currenti.currenti, currentcommand.currentcommand)
    },
    async executeCommand(currentcommand) {
      const data = await axios.get('/executeCommand/' + currentcommand.currentcommand)
      if (!data.err) {
        this.$data.commandResponse = data.data;
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
    clearSelectedMission() {
      this.missionName="";
      this.sequences = [];
      this.newType = "New";
    },
 
  },
  computed: {
    numberOfPages () {
      return Math.ceil(this.sequences.length / this.itemsPerPage)
    },
  },

}
</script>
