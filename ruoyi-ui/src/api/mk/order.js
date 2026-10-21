import request from '@/utils/request'

// 查询订单列表
export function listOrder(query, config = {}) {
return request({ url: '/mk/order/list', method: 'get', params: query, ...config })
}

// 查询订单详细
export function getOrder(orderId) {
  return request({ url: '/mk/order/' + orderId, method: 'get' })
}

// 新增订单
export function addOrder(data) {
  return request({ url: '/mk/order', method: 'post', data: data })
}

// 修改订单
export function updateOrder(data) {
  return request({ url: '/mk/order', method: 'put', data: data })
}

// 删除订单
export function delOrder(orderId) {
  return request({ url: '/mk/order/' + orderId, method: 'delete' })
}

// 订单发货（支持多次发货）
export function deliverOrder(data) {
  return request({ url: '/mk/order/deliver', method: 'put', data: data })
}

// 订单提交（草稿/已驳回 → 待审核）
export function submitOrder(orderId) {
  return request({ url: '/mk/order/submit/' + orderId, method: 'put' })
}

// 订单作废（草稿/待审核/已审核/部分发货 → 已作废）
export function voidOrder(orderId, voidReason) {
  return request({ url: '/mk/order/void/' + orderId, method: 'put', params: { voidReason } })
}

// 订单审核（待审核 → 已审核/已驳回）
export function auditOrder(orderId, status, auditOpinion) {
  return request({ url: '/mk/order/audit/' + orderId, method: 'put', params: { status, auditOpinion } })
}
