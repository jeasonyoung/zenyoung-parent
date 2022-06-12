import axios from "axios";
import {Notification,MessageBox,Message,Loading} from "element-ui";
import store from "@/store";
import {getAccessToken} from "@/util/auth";
import errorCode from "@/util/errorCode";
import {tansParams,blobValidate} from "@/util/utils";
import {saveAs} from "file-saver";

let downloadLoadingInstance

axios.defaults.headers["Content-Type"]="application/json;charset=utf-8"
//创建axios实例
const service = axios.create({
  //axios中请求配置有baseURL选项,表示请求URL公共部分
  baseURL: process.env.VUE_APP_BASE_API,
  //超时
  timeout: 10000
})
//request拦截器
service.interceptors.request.use(config=>{
  //是否需要设置token
  const isToken = (config.headers || {}).isToken === false
  if(getAccessToken() && !isToken) {
    //让每个请求携带自定义token 请根据实际情况自行修改
    config.headers["Authorization"] = 'Bearer ' + getAccessToken()
  }
  //get请求映射params参数
  if(config.method === "get" && config.params){
    let url = config.url + "?" + tansParams(config.params)
    url = url.slice(0, -1)
    config.params={}
    config.url=url
  }
  return config
}, error => {
  console.log(error)
  return Promise.reject(error)
})
//响应拦截器
service.interceptors.response.use(res=>{
  //未设置状态码则默认成功状态
  const code=res.data.code || 0
  //获取错误信息
  const msg = errorCode[code] || res.data.msg || errorCode["default"]
  //二进制数据则直接返回
  const respType = res.request.responseType
  if(respType === "blob" || respType === "arraybuffer"){
    return res.data
  }
  if(code === 401){
    MessageBox.confirm("登录状态已过期,您可以继续留在该页面或者重新登录","系统提示", {
      confirmButtonText: "重新登录",
      cancelButtonText: "取消",
      type:"warning"
    }).then(()=>{
      store.dispatch("Logout").then(()=>{location.href="/index";})
    }).catch(()=>{})
    return Promise.reject("无效的会话或会话已过期,请重新登录")
  }
  if(code !== 500){
    Message({message: msg, type: "error"})
    return Promise.reject(new Error(msg))
  }
  return res.data["data"]
}, error => {
  console.log(`error: ${error}`)
  let { message } = error;
  if (message === "Network Error") {
    message = "后端接口连接异常";
  } else if (message.includes("timeout")) {
    message = "系统接口请求超时";
  } else if (message.includes("Request failed with status code")) {
    message = "系统接口" + message.substr(message.length - 3) + "异常";
  }
  Message({message: message, type: 'error', duration: 5 * 1000})
  return Promise.reject(error)
})

// 通用下载方法
export function download(url, params, filename) {
  downloadLoadingInstance = Loading.service({ text: "正在下载数据，请稍候", spinner: "el-icon-loading", background: "rgba(0, 0, 0, 0.7)", })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob'
  }).then(async (data) => {
    const isLogin = await blobValidate(data);
    if (isLogin) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text();
      const rspObj = JSON.parse(resText);
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      Message.error(errMsg);
    }
    downloadLoadingInstance.close();
  }).catch((r) => {
    console.error(r)
    Message.error('下载文件出现错误，请联系管理员！')
    downloadLoadingInstance.close();
  })
}

export default service
