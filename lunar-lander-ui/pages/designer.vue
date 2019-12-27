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

      <v-select
        :items="missions"
        label="Mission"
        v-model="missionName"
        v-on:change="getSequences()"
        outlined
      ></v-select>
 
      <v-card-text>
       <v-text-field
         v-model="missionName"
         label="Mission Name"
         required
       ></v-text-field>
       

       <v-data-iterator
          v-if="sequences.length > 0"
          :items="sequences"
          item-key="name"
          :items-per-page="4"
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
                    <h4>{{ item.name }}</h4>
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
                          @click.stop="currentcommand=command; currenti=i; currentj=j; dialog = true"
                        >
                          mdi-code-braces-box
                        </v-icon>
                      </v-list-item-content>
                    </v-list-item>

                    <v-divider></v-divider>
                    <v-icon color="primary" dark v-on:click="pushSequence(j)">mdi-plus</v-icon>

                  </v-list>



                </v-card>
              </v-col>
            </v-row>


          </template>
        </v-data-iterator>

        <v-row justify="center">
          <v-dialog v-model="dialog" scrollable max-width="300px">
            <template v-slot:activator="{ on }">
            </template>
            <v-card>
              <v-card-title>Command Editor</v-card-title>
              <v-card-text style="height: 300px;">
                <v-textarea
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
                <v-btn color="blue darken-1" text @click="dialog = false">Close</v-btn>
                <v-btn color="blue darken-1" text @click="executeCommand({currentcommand})">Execute</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </v-row>  


        
      <v-btn v-if="sequences.length !== 0" v-on:click="saveMission()">Save</v-btn>

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
          missionName: "",
          sequences: [],
          expand: false,
          dialog: false,
          commandResponse: "",
          currenti: 0,
          currentj: 0,
          currentcommand: ""
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
    pushSequence(j){
      this.sequences[j].commands.push("")
    },
    async saveMission() {
      const data = await axios.post('/mission/', {
        missionName: this.missionName, sequences: this.sequences
      })
      if (!data.err) {
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
 
    }
  }
}
</script>
