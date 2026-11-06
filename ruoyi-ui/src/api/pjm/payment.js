import request from '@/utils/request'

// 查询收款记录列表
export function listPayment(query) {
  return request({ url: '/pjm/payment/list', method: 'get', params: query })
}

// 按项目汇总回款进度
export function summaryPayment(query) {
  return request({ url: '/pjm/payment/summary', method: 'get', params: query })
}

// 查询收款记录详情
export function getPayment(paymentId) {
  return request({ url: '/pjm/payment/' + paymentId, method: 'get' })
}

// 新增收款记录
export function addPayment(data) {
  return request({ url: '/pjm/payment', method: 'post', data: data })
}

// 修改收款记录
export function updatePayment(data) {
  return request({ url: '/pjm/payment', method: 'put', data: data })
}

// 删除收款记录
export function delPayment(paymentIds) {
  return request({ url: '/pjm/payment/' + paymentIds, method: 'delete' })
}
