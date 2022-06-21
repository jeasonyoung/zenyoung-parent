import {Message} from "element-ui";
import NProgress from "nprogress";
import "nprogress/nprogress.css"

import router from "@/router";
import store from "@/store";
import {getAccessToken} from "@/util/auth";

NProgress.configure({showSpinner: false})

const whiteList = ["/login","/auth-redirect", "/bind", "/register"]

router.beforeEach((to,from,next)=>{
  NProgress.start()
  if(getAccessToken()) {
    to.meta.title && store.dispatch("settings/setTitle", to.meta.title)
    if(to.path === "/login"){
      next({path: "/"})
      NProgress.done()
    } else {
      if(store.getters.roles.length === 0){
        //判断当前用户是否已拉取完user_info信息
        store.dispatch("GetInfo").then(()=>{
          //路由数据
          store.dispatch("GenerateRoutes").then(accessRoutes=>{
            // 根据roles权限生成可访问的路由表,动态添加可访问路由表
            router.addRoutes(accessRoutes)
            // hack方法 确保addRoutes已完成
            next({...to, replace: true})
          })
        }).catch(err => {
          store.dispatch("LogOut").then(()=>{
            Message.error(err)
            next({path: "/"})
          })
        })
      } else {
        next()
      }
    }
  } else {
    //没有Token
    if(whiteList.indexOf(to.path) !== -1){
      //在免登录白名单，直接进入
      next()
    }else {
      //否则全部重定向到登录页
      next(`/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
  }
})
//
router.afterEach(()=>{
  NProgress.done()
})
