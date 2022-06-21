import request from "@/util/request";
//1.5.1.用户管理-查询
export function userQuery(query) {
  return request({
    url: '/sys/user/query',
    method: 'get',
    params: query
  })
}
//1.5.2.用户管理-加载
export function userById(id){
  return request({
    url: '/sys/user/${id}',
    method: 'get'
  })
}
//1.5.3.用户管理-新增
export function userAdd(data) {
  return request({
    url: '/sys/user',
    method: 'post',
    data: data
  })
}
//1.5.4.用户管理-修改
export function userUpdate(id,data) {
  return request({
    url: `/sys/user/${id}`,
    method: 'put',
    data: data
  })
}
//1.5.5.用户管理-删除
export function userDel(ids) {
  return request({
    url: `/sys/user/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}
//1.5.6.用户管理-重置密码
export function userRestPwd(id, data) {
  return request({
    url: `/sys/user/${id}/rest`,
    method: 'put',
    data: data
  })
}
