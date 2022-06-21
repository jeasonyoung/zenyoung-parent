import request from "@/util/request";
//1.10.1.用户在线管理-查询
export function onlineQuery(query) {
  return request({
    url: '/monitor/online/query',
    method: 'get',
    params: query
  })
}
//1.10.2.用户在线管理-加载"
export function onlineByKey(key) {
  return request({
    url: `/monitor/online/${key}`,
    method: 'get'
  })
}
//1.10.3.用户在线管理-单条强退
export function onlineDelByKey(key) {
  return request({
    url: `/monitor/online/force/${key}`,
    method: 'delete'
  })
}
//1.10.4.用户在线管理-批量强退
export function onlineDelKeys(keys) {
  return request({
    url: `/monitor/online/batch/${(keys||[]).join(",")}`,
    method: 'delete'
  })
}
