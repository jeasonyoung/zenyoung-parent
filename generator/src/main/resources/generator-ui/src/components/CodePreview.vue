<template>
  <el-container>
    <el-aside>
      <!-- 文件树 -->
      <el-tree :data="fileTree" :props="fileTreeProps" @node-click="handleFileTreeNode"/>
    </el-aside>
    <el-main>
      <p>{{codeFileName}}</p>
      <pre>
        <code class="hljs" v-html="codeContent"/>
      </pre>
    </el-main>
  </el-container>
</template>

<script>
import codeHighlight from "highlight.js/lib/core";
import "highlight.js/styles/github.css"

import {getPreview} from '@/api/codegen'

//高亮代码注册
codeHighlight.registerLanguage("java", require("highlight.js/lib/languages/java"))
codeHighlight.registerLanguage("html", require("highlight.js/lib/languages/xml"))
codeHighlight.registerLanguage("xml", require("highlight.js/lib/languages/xml"))
codeHighlight.registerLanguage("sh", require("highlight.js/lib/languages/bash"))
codeHighlight.registerLanguage('vue', require('highlight.js/lib/languages/xml'));
codeHighlight.registerLanguage('javascript', require('highlight.js/lib/languages/javascript'));
codeHighlight.registerLanguage('sql', require('highlight.js/lib/languages/sql'))
codeHighlight.registerLanguage('gitignore', require('highlight.js/lib/languages/bash'))
codeHighlight.registerLanguage('properties', require('highlight.js/lib/languages/bash'))

export default {
  name: "CodePreview",
  props: {
    form: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      codeFileName: "",
      codeContent: "",
      codeFiles: [],
      fileTree: [],
      fileTreeProps: {
        label: 'label',
        children: 'children'
      }
    }
  },
  created() {
    getPreview(this.form).then(res => {
      this.codeFiles = res || []
      this.fileTree = []
      //构建文件树
      this.buildFileTree()
    })
  },
  methods: {
    //构建文件树
    buildFileTree() {
      const nodeMap = {}
      this.codeFiles.forEach(f => {
        const dir = f["dir"] || "", name = f["name"], content = f["content"]
        if (dir && name && content) {
          const wSep = "\\",lSep = "/"
          let dirs = []
          if(dir.includes(wSep)){
            dirs = dir.split(wSep)
          }else if(dir.includes(lSep)){
            let d = dir.replace("src/main/java", "src-main-java").replace("src/main/resources", "src-main-resources")
            dirs = d.split(lSep)
          }else {
            dirs = [dir]
          }
          const len = dirs.length
          for(let i = 0; i < len; i++){
            if(!dirs[i]) {
              continue
            }
            const pathKey = this.buildPathKey(dirs, i)
            let node = nodeMap[pathKey] || (void 0)
            if(!node){
              node = {
                id: pathKey,
                label: dirs[i].replace("src-main-java", "src/main/java").replace("src-main-resources", "src/main/resources"),
                content: "",
                pid: this.buildPathKey(dirs, i-1)
              }
              nodeMap[pathKey] = node
            }
          }
          const pId = this.buildPathKey(dirs, len - 1)
          if(pId){
            const leafKey = `k-${dirs[len-1]}-${name}`
            nodeMap[leafKey] = {
              id: leafKey,
              label: name,
              content: content,
              pid: pId
            }
          }
        }else if(name && content) {
          const leafKey = `k-${name}`
          nodeMap[leafKey] = {
            id: leafKey,
            label: name,
            content: content,
            pid: ""
          }
        }
      })
      //构建树
      if(nodeMap){
        //构建树
        const treeNodes = []
        Object.keys(nodeMap).map(key=>{
          const node = nodeMap[key]
          if(node && (node.pid === "" || !nodeMap[node.pid])) {
            node["children"] = []
            this.buildTreeChild(node, nodeMap)
            treeNodes.push(node)
          }
        })
        this.fileTree = treeNodes || []
      }
    },
    buildTreeChild(parent, nodeMap){
      if(parent && nodeMap){
        Object.keys(nodeMap).map(key=>{
          const node = nodeMap[key]
          if(node && node.pid && node.pid === parent.id){
            node["children"] = []
            this.buildTreeChild(node, nodeMap)
            parent.children.push(node)
          }
        })
      }
    },
    buildPathKey(dirs, idx) {
      if(dirs && idx >= 0) {
        const sep = "_", len = dirs.length
        if(len > idx) {
          const paths = []
          for (let i = 0; i <= idx; i++) {
            let dir = dirs[i]
            if (dir) {
              paths.push(dir)
            }
          }
          return `k${sep}${(paths.join(sep) || "").trim()}`
        }
      }
      return ""
    },
    //
    handleFileTreeNode(node) {
      let content
      if((content = node["content"])){
        this.codeFileName = node["label"]
        this.codeContent = this.highlightedCode(this.codeFileName, content)
      }
    },
    //代码高亮
    highlightedCode(fileName, content) {
      if (fileName && content) {
        const index = fileName.lastIndexOf(".")
        const lang = index > 0 ? fileName.substring(index + 1) : "sh"
        const ret = codeHighlight.highlight(lang, content)
        //console.info(ret)
        return ret.value || content
      }
      return content
    }
  }
}
</script>

<style lang="scss" scoped>
.el-container {
  border: 1px solid #eee;
}
.el-aside {
  min-width: 200px;
  width: auto;
  background-color: #eee;

  .el-tree {
    max-height: 600px;
  }
}
.el-main {
  width: 600px;
  height: 600px;
}
</style>
