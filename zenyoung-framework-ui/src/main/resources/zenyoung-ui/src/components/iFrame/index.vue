<template>
  <div v-loading="loading" :style="'height:' + height">
    <iframe :src="src" frameborder="no" scrolling="auto" style="width:100%;height:100%"/>
  </div>
</template>

<script>
export default {
  name:"iFrame",
  props: {
    src: {
      type: String,
      require: true
    }
  },
  data() {
    return {
      height: (document.documentElement.clientHeight - 94.5) + "px;",
      loading: true,
      url: this.src
    }
  },
  mounted() {
    setTimeout(()=>{
      this.loading = false
    }, 300)
    const that = this
    window.onresize = function temp(){
      that.height = (document.documentElement.clientHeight - 94.5) + "px;"
    }
  }
}
</script>
