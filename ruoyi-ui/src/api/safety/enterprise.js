import request from '@/utils/request'

// 查询企业档案列表
export function listEnterprise(query) {
  return request({
    url: '/safety/enterprise/list',
    method: 'get',
    params: query
  })
}

// 查询企业档案详情
export function getEnterprise(enterpriseId) {
  return request({
    url: '/safety/enterprise/' + enterpriseId,
    method: 'get'
  })
}

// 新增企业档案
export function addEnterprise(data) {
  return request({
    url: '/safety/enterprise',
    method: 'post',
    data: data
  })
}

// 修改企业档案
export function updateEnterprise(data) {
  return request({
    url: '/safety/enterprise',
    method: 'put',
    data: data
  })
}

// 删除企业档案
export function delEnterprise(enterpriseId) {
  return request({
    url: '/safety/enterprise/' + enterpriseId,
    method: 'delete'
  })
}

// 导出企业档案
export function exportEnterprise(query) {
  return request({
    url: '/safety/enterprise/export',
    method: 'post',
    params: query
  })
}