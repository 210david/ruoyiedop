import request from '@/utils/request'

// 查询退货列表
export function listReturn(query, config = {}) {
return request({ url: '/mk/return/list', method: 'get', params: query, ...config })
}

// 查询退货详细
export function getReturn(returnId) {
  return request({ url: '/mk/return/' + returnId, method: 'get' })
}

// 新增退货
export function addReturn(data) {
  return request({ url: '/mk/return', method: 'post', data: data })
}

// 修改退货
export function updateReturn(data) {
  return request({ url: '/mk/return', method: 'put', data: data })
}

// 删除退货
export function delReturn(returnId) {
  return request({ url: '/mk/return/' + returnId, method: 'delete' })
}

// 提交审批（草稿→待审批）
export function submitReturn(returnId) {
  return request({ url: '/mk/return/submit/' + returnId, method: 'put' })
}

// 审批退货
export function approveReturn(returnId, approved, opinion) {
  return request({ url: '/mk/return/approve/' + returnId + '/' + approved, method: 'put', params: { opinion } })
}

// 退款
export function refundReturn(returnId, refundAmount, refundDate) {
  return request({ url: '/mk/return/refund/' + returnId, method: 'put', params: { refundAmount, refundDate } })
}
