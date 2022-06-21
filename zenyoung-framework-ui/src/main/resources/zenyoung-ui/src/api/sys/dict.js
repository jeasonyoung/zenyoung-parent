import request from '@/util/request'

//1.6.1.字典类型管理-查询
export function dictTypeQuery(query) {
  return request({
    url: '/sys/dict/type/query',
    method: 'get',
    params: query
  })
}

//1.6.2.字典类型管理-加载
export function dictTypeById(typeId) {
  return request({
    url: `/sys/dict/type/${typeId}`,
    method: 'get'
  })
}

//1.6.3.字典类型管理-新增
export function dictTypeAdd(data) {
  return request({
    url: `/sys/dict/type`,
    method: 'post',
    data: data
  })
}

//1.6.4.字典类型管理-修改
export function dictTypeUpdate(typeId, data) {
  return request({
    url: `/sys/dict/type/${typeId}`,
    method: 'put',
    data: data
  })
}

//1.6.5.字典类型管理-删除
export function dictTypeDel(typeIds) {
  return request({
    url: `/sys/dict/type/${(typeIds || []).join(",")}`,
    method: 'delete'
  })
}

//1.6.6.字典数据管理-加载数据
export function dictByType(dictType) {
  return request({
    url: `/sys/dict/${dictType}`,
    method: 'get'
  })
}

//1.6.7.字典数据管理-新增数据
export function dictDataByTypeAdd(typeId) {
  return request({
    url: `/sys/dict/type/${typeId}/data`,
    method: 'post'
  })
}

//1.6.8.字典数据管理-修改数据
export function dictDataUpdate(dataId, data) {
  return request({
    url: `/sys/dict/data/${dataId}`,
    method: 'put',
    data: data
  })
}

//1.6.9.字典数据管理-删除数据
export function dictDataDel(ids) {
  return request({
    url: `/sys/dict/data/${(ids || []).join(",")}`,
    method: 'delete'
  })
}
