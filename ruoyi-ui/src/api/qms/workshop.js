import request from '@/utils/request'

// 列表查询（平表）
export function listWorkshop(query) {
  return request({
    url: '/qms/workshop/list',
    method: 'get',
    params: query
  })
}

// 树形结构查询
export function workshopTree(query) {
  return request({
    url: '/qms/workshop/tree',
    method: 'get',
    params: query
  })
}

// 详情
export function getWorkshop(workshopId) {
  return request({
    url: '/qms/workshop/' + workshopId,
    method: 'get'
  })
}

// 新增
export function addWorkshop(data) {
  return request({
    url: '/qms/workshop',
    method: 'post',
    data: data
  })
}

// 修改
export function updateWorkshop(data) {
  return request({
    url: '/qms/workshop',
    method: 'put',
    data: data
  })
}

// 删除
export function delWorkshop(workshopIds) {
  return request({
    url: '/qms/workshop/' + workshopIds,
    method: 'delete'
  })
}
