import request from '@/utils/request'

// 查询企业客户列表
export function listCustomer(query) {
  return request({
    url: '/mk/customer/list',
    method: 'get',
    params: query
  })
}

// 查询公海客户列表
export function listPublicPool(query) {
  return request({
    url: '/mk/customer/publicPool',
    method: 'get',
    params: query
  })
}

// 查询企业客户详细
export function getCustomer(customerId) {
  return request({
    url: '/mk/customer/' + customerId,
    method: 'get'
  })
}

// 新增企业客户
export function addCustomer(data) {
  return request({
    url: '/mk/customer',
    method: 'post',
    data: data
  })
}

// 修改企业客户
export function updateCustomer(data) {
  return request({
    url: '/mk/customer',
    method: 'put',
    data: data
  })
}

// 删除企业客户
export function delCustomer(customerId) {
  return request({
    url: '/mk/customer/' + customerId,
    method: 'delete'
  })
}

// ====== 第二阶段：客户状态生命周期管理 ======

// 变更客户状态
export function changeCustomerStatus(customerId, customerStatus) {
  return request({
    url: '/mk/customer/status/' + customerId,
    method: 'put',
    params: { customerStatus }
  })
}

// 批量变更客户状态
export function batchUpdateStatus(data) {
  return request({
    url: '/mk/customer/batchStatus',
    method: 'put',
    data: data
  })
}

// ====== 第三阶段：公海机制 + 分配/转移 ======

// 分配客户负责人
export function assignCustomer(customerId, data) {
  return request({
    url: '/mk/customer/assign/' + customerId,
    method: 'put',
    data: data
  })
}

// 释放客户到公海
export function releaseToPool(customerId) {
  return request({
    url: '/mk/customer/release/' + customerId,
    method: 'put'
  })
}

// 从公海领取客户
export function claimCustomer(customerId) {
  return request({
    url: '/mk/customer/claim/' + customerId,
    method: 'put'
  })
}

// 批量分配客户
export function batchAssignCustomer(data) {
  return request({
    url: '/mk/customer/batchAssign',
    method: 'put',
    data: data
  })
}
