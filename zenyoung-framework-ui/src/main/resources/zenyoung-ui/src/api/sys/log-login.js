import request from "@/util/request";

//1.8.1.登录日志管理-查询
export function logLoginQuery(query) {
  return request({
    url: `/monitor/log-login/query`,
    method: 'get',
    params: query
  })
}

//1.8.2.登录日志管理-加载
export function logLoginById(id) {
  return request({
    url: `/monitor/log-login/${id}`,
    method: 'get'
  })
}

//1.8.3.登录日志管理-批量删除
export function logLoginDel(ids) {
  return request({
    url: `/monitor/log-login/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}
