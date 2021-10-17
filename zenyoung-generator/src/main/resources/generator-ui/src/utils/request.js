import axios from "axios";
import {Message} from "element-ui";
import {getToken} from './auth'

axios.defaults.headers.post['Content-Type'] = 'application/json;charset=utf-8';
//创建axios实例
const service = axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: 'http://localhost:8000',
    // 超时
    timeout: 10000
})

//请求拦截器
service.interceptors.request.use(config => {
    //令牌
    config.headers['token'] = getToken()
    return config
}, error => {
    console.error(error)
    return Promise.reject(error)
})

//响应拦截器
service.interceptors.response.use(res => {
    //未设置状态码则默认成功状态
    const code = res.data.code || 0;
    const msg = res.data.msg || '';
    if (code !== 0){
        Message({
            message: msg,
            type: 'error'
        })
        return Promise.reject(new Error(msg))
    }else{
        return res.data['data']
    }
}, error => {
    console.log('err=>' + error)
    let {message} = error;
    Message({
        message: message,
        type: 'error',
        duration: 5 * 1000,
    })
    return Promise.reject(error);
})

export default service
