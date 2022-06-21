import request from "@/util/request";
//1.3.1.菜单管理-查询
export function menuQuery(query) {
  return request({
    url: '/sys/menu/query',
    method: 'get',
    params: query
  })
}
//1.3.2.岗位管理-加载
export function menuById(id) {
  return request({
    url: `/sys/menu/${id}`,
    method: 'get'
  })
}
//1.3.3.岗位管理-菜单树
export function menuTreeByPid(pid) {
  return request({
    url: `/sys/menu/tree?parentId=${pid||""}`,
    method: 'get'
  })
}
//1.3.4.菜单管理-新增
export function menuAdd(data) {
  return request({
    url: `/sys/menu`,
    method: 'post',
    data: data
  })
}
//1.3.5.菜单管理-修改
export function menuUpdate(id, data) {
  return request({
    url: `/sys/menu/${id}`,
    method: 'post',
    data: data
  })
}
//1.3.6.菜单管理-删除
export function menuDel(ids) {
  return request({
    url: `/sys/menu/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}

//获取路由
export function getRouters() {
  return request({
    url: "/get-routers",
    method: "get"
  })
}
