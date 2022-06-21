import request from "@/util/request";

//1.4.1.角色管理-查询
export function roleQuery(query) {
  return request({
    url: '/sys/role/query',
    method: 'get',
    params: query
  })
}
//1.4.2.角色管理-加载
export function roleById(id) {
  return request({
    url: `/sys/role/${id}`,
    method: 'get'
  })
}
//1.4.3.角色管理-新增
export function roleAdd(data) {
  return request({
    url: '/sys/role',
    method: 'post',
    data: data
  })
}
//1.4.4.角色管理-修改
export function roleUpdate(id, data) {
  return request({
    url: `/sys/role/${id}`,
    method: 'put',
    data: data
  })
}
//1.4.5.角色管理-删除
export function roleDel(ids) {
  return request({
    url: `/sys/role/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}
