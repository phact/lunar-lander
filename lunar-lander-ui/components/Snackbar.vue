<template>
  <v-snackbar v-model="show">
    {{message}}
    <v-btn text color="blue darken-1" @click.native="show = false">Close</v-btn>
  </v-snackbar>
</template>

<script>
export default {
  data () {
    return {
      show: false,
      message: ''
    }
  },
  created: function () {
    this.$store.watch(state => state.snackbar.snack, () => {
      const msg = this.$store.state.snackbar.snack
      if (msg !== '') {
        this.show = true
        this.message = this.$store.state.snackbar.snack
        this.$store.commit('snackbar/setSnack', '')
      }
    })
  }
}
</script>
