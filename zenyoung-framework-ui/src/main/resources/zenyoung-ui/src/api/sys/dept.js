import request from '@/util/request'

//1.1.1.部门管理-全部
export function deptQuery(query) {
  return request({
    url: '/sys/dept/all',
    method: 'get',
    params: query
  })
}

//1.1.2.部门管理-部门树
export function deptTree(pid, excludes){
  return request({
    url: `/sys/dept/tree?pid=${pid||''}&excludes=${(excludes||[]).join(",")}`,
    method: 'get'
  })
}

//1.1.3.部门管理-加载
export function deptById(id) {
  return request({
    url: `/sys/dept/${id}`,
    method: 'get'
  })
}

//1.1.4.部门管理-新增
export function deptAdd(data) {
  return request({
    url: '/sys/dept',
    method: 'post',
    data: data
  })
}

//1.1.5.部门管理-修改
export function deptUpdate(id, data) {
  return request({
    url: `/sys/dept/${id}`,
    method: 'post',
    data: data
  })
}

//1.1.6.部门管理-删除
export function deptDel(ids){
  return request({
    url: `/sys/dept/${(ids||[]).join(",")}`,
    method: 'delete'
  })
}
