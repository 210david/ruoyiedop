import request from '@/utils/request'

// 查询回款计划列表
export function listPayment(query) {
  return request({ url: '/mk/payment/list', method: 'get', params: query })
}

// 查询回款计划详细
export function getPayment(planId) {
  return request({ url: '/mk/payment/detail/' + planId, method: 'get' })
}

// 新增回款计划
export function addPayment(data) {
  return request({ url: '/mk/payment', method: 'post', data: data })
}

// 修改回款计划
export function updatePayment(data) {
  return request({ url: '/mk/payment', method: 'put', data: data })
}

// 删除回款计划
export function delPayment(planId) {
  return request({ url: '/mk/payment/' + planId, method: 'delete' })
}

// 回款统计
export function paymentStatistics() {
  return request({ url: '/mk/payment/statistics', method: 'get' })
}

// 查询回款记录列表
export function listPaymentRecord(query) {
  return request({ url: '/mk/payment/record/list', method: 'get', params: query })
}

// 查询回款记录详细
export function getPaymentRecord(recordId) {
  return request({ url: '/mk/payment/record/' + recordId, method: 'get' })
}

// 新增回款记录（多次回款登记）
export function addPaymentRecord(data) {
  return request({ url: '/mk/payment/record', method: 'post', data: data })
}

// 修改回款记录
export function updatePaymentRecord(data) {
  return request({ url: '/mk/payment/record', method: 'put', data: data })
}

// 删除回款记录
export function delPaymentRecord(recordId) {
  return request({ url: '/mk/payment/record/' + recordId, method: 'delete' })
}

// 确认回款记录
export function confirmPaymentRecord(recordId, confirmStatus, remark) {
  return request({ url: '/mk/payment/record/confirm/' + recordId + '/' + confirmStatus, method: 'put', params: { remark } })
}
