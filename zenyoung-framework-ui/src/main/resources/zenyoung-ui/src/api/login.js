import request from "@/util/request";
import {encrypt} from "@/util/jsencrypt";

// 用户注册
export function register(data) {
  return request({
    url: "/auth/register",
    headers: {
      isToken: false
    },
    method: "post",
    data: data
  })
}

// 用户登录
export function login(account,passwd,code,key) {
  const data = {
    account: account,
    password: (!passwd || passwd === "") ? "": encrypt(passwd),
    verifyCode: code,
    verifyKey: key
  }
  return request({
    url: "/auth/login",
    headers: {
      isToken: false
    },
    method: "post",
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: "/auth/getInfo",
    method: "get"
  })
}

// 退出方法
export function logout(){
  return request({
    url: "/auth/logout",
    method: "get"
  })
}

//获取验证码
export function getCaptcha() {
  return request({
    url: "/auth/captcha",
    headers: {
      isToken: false
    },
    method: 'get'
  })
}
