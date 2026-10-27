import request from '@/utils/request'

export function listReturn(query) {
  return request({
    url: '/pms/return/list',
    method: 'get',
    params: query
  })
}

export function getReturn(returnId) {
  return request({
    url: '/pms/return/info/' + returnId,
    method: 'get'
  })
}

export function addReturn(data) {
  return request({
    url: '/pms/return',
    method: 'post',
    data: data
  })
}

export function updateReturn(data) {
  return request({
    url: '/pms/return',
    method: 'put',
    data: data
  })
}

export function delReturn(returnId) {
  return request({
    url: '/pms/return/' + returnId,
    method: 'delete'
  })
}

export function submitReturn(returnId) {
  return request({
    url: '/pms/return/submit/' + returnId,
    method: 'put'
  })
}

export function auditReturn(returnId, status, auditOpinion) {
  return request({
    url: '/pms/return/audit/' + returnId,
    method: 'put',
    params: { status, auditOpinion }
  })
}

// 获取存在进行中退货单的采购订单ID列表（草稿/待审批/已审批/已驳回）
export function getInProgressReturnOrderIds() {
  return request({
    url: '/pms/return/query/inProgressOrderIds',
    method: 'get'
  })
}
