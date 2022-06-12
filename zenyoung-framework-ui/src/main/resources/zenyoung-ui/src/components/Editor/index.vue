<template>
  <div>
    <el-upload
      v-if="this.type === 'url'"
      ref="upload"
      name="file"
      :action="uploadUrl"
      :before-upload="handlerBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :show-file-list="false"
      :headers="headers"
      style="display: none"/>
    <div class="editor" ref="editor" :style="styles"></div>
  </div>
</template>

<script>
import Quill from "quill";
import "quill/dist/quill.core.css";
import "quill/dist/quill.snow.css";
import "quill/dist/quill.bubble.css";
import { getAccessToken } from "@/util/auth";

export default {
  name: "Editor",
  props: {
    //编辑器的内容
    value: {
      type: String,
      default: ""
    },
    //高度
    height: {
      type: Number,
      default: null
    },
    //最小高度
    minHeight: {
      type: Number,
      default: null
    },
    //只读
    readOnly: {
      type: Boolean,
      default: false
    },
    //上传文件大小限制(MB)
    fileSize: {
      type: Number,
      default: 5
    },
    //类型(base64格式,URL格式)
    type: {
      type: String,
      default: "url"
    }
  },
  data() {
    return {
      //上传的图片服务器地址
      uploadUrl: process.env.VUE_APP_BASE_API + "/upload",
      headers: {
        Authorization: "Bearer " + getAccessToken()
      },
      Quill: null,
      currentValue: "",
      options: {
        theme: "snow",
        bounds: document.body,
        debug: "warn",
        modules: {
          //工具栏配置
          toolbar: [
            //加粗 斜体 下划线 删除线
            ["bold", "italic", "underline", "strike"],
            //引用  代码块
            ["blockquote","code-block"],
            //有序、无序列表
            [{list: "ordered"}, {list: "bullet"}],
            //缩进
            [{indent: "-1"},{indent: "+1"}],
            //字体大小
            [{size: ["small", false, "large", "huge"]}],
            //标题
            [{ header: [1, 2, 3, 4, 5, 6, false] }],
            //字体颜色、字体背景颜色
            [{color: []}, {background: []}],
            //对齐方式
            [{align: []}],
            //清除文本格式
            ["clean"],
            //链接、图片、视频
            ["link","image","video"]
          ]
        },
        placeholder: "请输入内容",
        readOnly: this.readOnly
      }
    }
  },
  computed: {
    styles() {
      let style = {}
      if(this.minHeight){
        style.minHeight = `${this.minHeight}px`
      }
      if(this.height){
        style.height = `${this.height}px`
      }
      return style
    }
  },
  watch: {
    value: {
      handler(val) {
        if(val !== this.currentValue) {
          this.currentValue = val === null ? "" : val
          if(this.Quill) {
            this.Quill.pasteHTML(this.currentValue)
          }
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.init()
  },
  beforeDestroy() {
    this.Quill = null
  },
  methods: {
    init() {
      const editor = this.$refs.editor
      this.Quill = new Quill(editor, this.options)
      //如果设置了上传地址则自定义图片上传事件
      if(this.type === 'url') {
        let toolbar = this.Quill.getModule("toolbar")
        toolbar.addHandler("image", (value)=>{
          this.uploadType = "image"
          if(value) {
            this.$refs.upload.$children[0].$refs.input.click()
          } else {
            this.Quill.format("image", false)
          }
        })
      }
      this.Quill.pasteHTML(this.currentValue)
      this.Quill.on("text-change",(delta, oldDelta,source)=>{
        const html = this.$refs.editor.children[0].innerHTML
        const text = this.Quill.getText()
        const quill = this.Quill
        this.currentValue = html
        this.$emit("input", html)
        this.$emit("on-change",{html, text, quill})
      })
      this.Quill.on("text-change",(delta, oldDelta, source)=>{
        this.$emit("on-text-change", delta, oldDelta, source)
      })
      this.Quill.on("selection-change", (range, oldRange, source)=>{
        this.$emit("on-selection-change", range, oldRange, source)
      })
      this.Quill.on("editor-change", (evenName,...args)=>{
        this.$emit("on-editor-change",evenName, ...args)
      })
    },
    //上传前校验格式和大小
    handlerBeforeUpload(file){
      //校检文件大小
      if(this.fileSize) {
        const isLt = (file.size/1024/1024) < this.fileSize
        if(!isLt) {
          this.$message.error(`上传文件大小不能超过 ${this.fileSize}MB!`)
          return false
        }
      }
      return true
    },
    handleUploadSuccess(res, file){
      //获取富文本组件实例
      let quill = this.Quill
      //如果上传成功
      if(res.code === 200){
        //获取光标所在位置
        let idx = quill.getSelected().index
        //插入图片  res.url为服务器返回的图片地址
        quill.insertEmbed(idx, "image", process.env.VUE_APP_BASE_API + res.fileName())
        //调整光标到最后
        quill.setSelection(idx + 1)
      } else {
        this.$message.error("图片插入失败")
      }
    },
    handleUploadError() {
      this.$message.error("图片插入失败")
    }
  }
}
</script>

<style scoped>

</style>
