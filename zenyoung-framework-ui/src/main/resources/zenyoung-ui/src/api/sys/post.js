import request from "@/util/request";

//1.2.1.岗位管理-查询
export function postQuery(query) {
  return request({
    url: '/sys/post/query',
    method: 'get',
    params: query
  })
}
//1.2.2.岗位管理-加载
export function postById(id) {
  return request({
    url: `/sys/post/${id}`,
    method: 'get'
  })
}
//1.2.3.岗位管理-新增
export function postAdd(data) {
  return request({
    url: '/sys/post',
    method: 'post',
    data: data
  })
}
//1.2.4.岗位管理-修改
export function postUpdate(id, data) {
  return request({
    url: `/sys/post/${id}`,
    method: 'put',
    data: data
  })
}
//1.2.5.岗位管理-删除
export function postDel(ids) {
  return request({
    url:`/sys/post/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}
