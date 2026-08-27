import request from '@/utils/request'

// 查询组织架构列表
export function listHrOrganization(query) {
  return request({
    url: '/hr/organization/list',
    method: 'get',
    params: query
  })
}

// 查询组织架构详细
export function getHrOrganization(orgId) {
  return request({
    url: '/hr/organization/' + orgId,
    method: 'get'
  })
}

// 新增组织架构
export function addHrOrganization(data) {
  return request({
    url: '/hr/organization',
    method: 'post',
    data: data
  })
}

// 修改组织架构
export function updateHrOrganization(data) {
  return request({
    url: '/hr/organization',
    method: 'put',
    data: data
  })
}

// 删除组织架构
export function delHrOrganization(orgId) {
  return request({
    url: '/hr/organization/' + orgId,
    method: 'delete'
  })
}
