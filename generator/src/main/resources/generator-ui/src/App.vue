<template>
  <div id="app">
    <CodeGenerator/>
  </div>
</template>

<script>
import CodeGenerator from "@/components/CodeGenerator";

export default {
  name: 'App',
  components: {
    CodeGenerator
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  #margin-top: 60px;
}
</style>
