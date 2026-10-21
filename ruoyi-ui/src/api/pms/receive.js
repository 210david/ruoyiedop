import request from '@/utils/request'

export function listReceive(query) {
  return request({
    url: '/pms/receive/list',
    method: 'get',
    params: query
  })
}

export function getReceive(receiveId) {
  return request({
    url: '/pms/receive/' + receiveId,
    method: 'get'
  })
}

export function addReceive(data) {
  return request({
    url: '/pms/receive',
    method: 'post',
    data: data
  })
}

export function updateReceive(data) {
  return request({
    url: '/pms/receive',
    method: 'put',
    data: data
  })
}

export function delReceive(receiveId) {
  return request({
    url: '/pms/receive/' + receiveId,
    method: 'delete'
  })
}

// 获取已有有效收货记录的采购订单ID列表（用于过滤已收货订单）
export function getReceivedOrderIds() {
  return request({
    url: '/pms/receive/receivedOrderIds',
    method: 'get'
  })
}

// 获取存在进行中收货单的采购订单ID列表（待验收/待审核/已驳回）
export function getInProgressOrderIds() {
  return request({
    url: '/pms/receive/inProgressOrderIds',
    method: 'get'
  })
}

export function inspectReceive(data) {
  return request({
    url: '/pms/receive/inspect',
    method: 'put',
    data: data
  })
}

// 提交审核
export function submitReceive(receiveId) {
  return request({
    url: '/pms/receive/submit/' + receiveId,
    method: 'put'
  })
}

// 审核收货
export function auditReceive(receiveId, status, auditOpinion) {
  return request({
    url: '/pms/receive/audit/' + receiveId,
    method: 'put',
    params: { status, auditOpinion }
  })
}
