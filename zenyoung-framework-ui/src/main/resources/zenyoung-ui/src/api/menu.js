//
import request from "@/util/request";
//获取路由
export function getRouters() {
  return request({
    url: "/get-routers",
    method: "get"
  })
}
