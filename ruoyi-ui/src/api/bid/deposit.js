import request from '@/utils/request'

export function listDeposit(query) {
  return request({ url: '/bid/deposit/list', method: 'get', params: query })
}
export function getDeposit(depositId) {
  return request({ url: '/bid/deposit/' + depositId, method: 'get' })
}
export function addDeposit(data) {
  return request({ url: '/bid/deposit', method: 'post', data })
}
export function updateDeposit(data) {
  return request({ url: '/bid/deposit', method: 'put', data })
}
export function delDeposit(depositIds) {
  return request({ url: '/bid/deposit/' + depositIds, method: 'delete' })
}
export function refundDeposit(depositId, actualRefundTime, remark) {
  return request({ url: '/bid/deposit/refund/' + depositId, method: 'put', params: { actualRefundTime, remark } })
}
