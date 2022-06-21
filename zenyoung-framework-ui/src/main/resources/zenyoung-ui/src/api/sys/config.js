import request from '@/util/request'

//1.7.1.参数管理-查询
export function configQuery(query) {
  return request({
    url: '/sys/config/query',
    method: 'get',
    params: query
  })
}

//1.7.2.参数管理-加载
export function configById(id) {
  return request({
    url: `/sys/config/${id}`,
    method: 'get'
  })
}

//1.7.3.参数管理-新增
export function configAdd(data) {
  return request({
    url: '/sys/config',
    method: 'post',
    data: data
  })
}

//1.7.4.参数管理-修改
export function configUpdate(id, data) {
  return request({
    url: `/sys/config/${id}`,
    method: 'put',
    data: data
  })
}

//1.7.5.参数管理-删除
export function configDel(ids) {
  return request({
    url: `/sys/config/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}

//1.7.6.参数管理-加载
export function configByKey(key) {
  return request({
    url: `/sys/config/key/${key}`,
    method: 'get'
  })
}
