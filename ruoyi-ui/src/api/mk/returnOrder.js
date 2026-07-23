import request from '@/utils/request'

// 查询退货列表
export function listReturn(query) {
  return request({ url: '/mk/return/list', method: 'get', params: query })
}

// 查询退货详细
export function getReturn(returnId) {
  return request({ url: '/mk/return/' + returnId, method: 'get' })
}

// 新增退货
export function addReturn(data) {
  return request({ url: '/mk/return', method: 'post', data: data })
}

// 删除退货
export function delReturn(returnId) {
  return request({ url: '/mk/return/' + returnId, method: 'delete' })
}

// 审批退货
export function approveReturn(returnId, approved, opinion) {
  return request({ url: '/mk/return/approve/' + returnId + '/' + approved, method: 'put', params: { opinion } })
}

// 退款
export function refundReturn(returnId, refundAmount, refundDate) {
  return request({ url: '/mk/return/refund/' + returnId, method: 'put', params: { refundAmount, refundDate } })
}
