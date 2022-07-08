import axios from "axios"
import {Notification} from "element-ui"

axios.defaults.headers["Content-Type"]="application/json;charset=utf-8"
//
const service = axios.create({

    //axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: process.env.VUE_APP_BASE_API,
    //超时
    timeout: 10000
})
//request拦截器
service.interceptors.request.use(config=>{
    return config
})
//响应拦截器
service.interceptors.response.use(res=>{
    // 二进制数据则直接返回
    if(res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer'){
        return res.data
    }
    // 未设置状态码则默认成功状态
    const code = res.data["code"] || 0;
    // 获取错误信息
    const msg = res.data["error"] || res.data["message"] || ""
    if(code !== 0){
        Notification.error(msg)
        return Promise.reject(msg)
    }
    return res.data["data"]
})
//
export default service
