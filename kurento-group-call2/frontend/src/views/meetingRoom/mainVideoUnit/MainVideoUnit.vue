<template>
  <div>
    <p>LocalVideo</p>
    <div id="vidCon"></div>
  </div>
</template>

<script>
// import "./template.scss";

export default {
  name: 'MainVideoUnit',
  components: {
  },
  // : props
  props: {
    mainParticipant: Object
  },
  // : data
  data() {
    return {
    }
  },
  // : computed
  computed: {
    video() {
      return this.mainParticipant.getVideoElement()
    },
  },
  // : lifecycle hook
  mounted() {
    document.getElementById("vidCon").appendChild(this.video)
  },
  // : updated
  updated() {
    console.log(this.mainParticipant)
  },
  // : methods
  methods: {
  }
}
</script>