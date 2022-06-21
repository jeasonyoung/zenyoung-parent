import Vue from "vue";
import Cookies from "js-cookie";
import Element from "element-ui"
import "./assets/styles/element-variables.scss"

import "@/assets/styles/index.scss"
import "@/assets/styles/ruoyi.scss"

import App from "./App.vue";
import store from "@/store";
import router from "@/router";
import directive from "@/directive";
import plugins from "@/plugins";

import "@/assets/icons";
import "@/permission"

import {getDictAllData} from "@/api/sys/dictType"
import {getConfigByKey} from "@/api/sys/config";

import {parseTime, resetForm, addDateRange, selectDictLabel, selectDictLabels, handleTree} from "@/util/utils";
//分页组件
import Pagination from "@/components/Pagination";
//自定义表格工具组件
import RightToolbar from "@/components/RightToolbar";
//富文本组件
import Editor from "@/components/Editor";
//文件上传组件
import FileUpload from "@/components/FileUpload";
//图片上传组件
import ImageUpload from "@/components/ImageUpload";
//头部标签组件
import VueMeta from "vue-meta";
//字典数据组件
import DictData from "@/components/DictData";
import DictTag from "@/components/DictTag"

//全局方法挂载
Vue.prototype.getDicts=getDictAllData
Vue.prototype.getConfigKey=getConfigByKey
Vue.prototype.parseTime=parseTime
Vue.prototype.resetForm=resetForm
Vue.prototype.addDateRange=addDateRange
Vue.prototype.selectDictLabel=selectDictLabel
Vue.prototype.selectDictLabels=selectDictLabels
Vue.prototype.handleTree=handleTree

//全局组件挂载
Vue.component("DictTag", DictTag)
Vue.component("Pagination", Pagination)
Vue.component("RightToolbar", RightToolbar)
Vue.component("Editor", Editor)
Vue.component("FileUpload", FileUpload)
Vue.component("ImageUpload", ImageUpload)
//
Vue.use(directive)
Vue.use(plugins)
Vue.use(VueMeta)
Vue.use(Element, {
  //set element-ui default size
  size: Cookies.get("size") || "medium"
})
//
DictData.install()
//
Vue.config.productionTip = false
//
new Vue({
  el: "#app",
  router,
  store,
  render: h=>h(App)
})
