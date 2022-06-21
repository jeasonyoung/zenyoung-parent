import request from "@/util/request";

//1.9.1.操作日志-查询
export function logOperaQuery(query) {
  return request({
    url: '/monitor/opera/query',
    method: 'get',
    params: query
  })
}
//1.9.2.操作日志-加载
export function logOperaById(id) {
  return request({
    url: `/monitor/opera/${id}`,
    method: 'get'
  })
}
//1.9.3.操作日志-批量删除
export function logOperaDel(data) {
  return request({
    url:'/monitor/opera',
    method: 'delete',
    params: data
  })
}
