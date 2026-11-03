import request from '@/utils/request'

// 提成方案与核算表（需求三）
export function listPlan(query) {
  return request({ url: '/hr/commission/plan/list', method: 'get', params: query })
}
export function getPlan(planId) {
  return request({ url: '/hr/commission/plan/' + planId, method: 'get' })
}
export function addPlan(data) {
  return request({ url: '/hr/commission/plan', method: 'post', data: data })
}
export function updatePlan(data) {
  return request({ url: '/hr/commission/plan', method: 'put', data: data })
}
export function delPlan(planIds) {
  return request({ url: '/hr/commission/plan/' + planIds, method: 'delete' })
}

export function listBill(query) {
  return request({ url: '/hr/commission/bill/list', method: 'get', params: query })
}
export function generateBills(period) {
  return request({ url: '/hr/commission/bill/generate/' + period, method: 'post' })
}
export function confirmBill(billId) {
  return request({ url: '/hr/commission/bill/confirm/' + billId, method: 'put' })
}
export function lockBills(period) {
  return request({ url: '/hr/commission/bill/lock/' + period, method: 'put' })
}
export function delBills(period) {
  return request({ url: '/hr/commission/bill/' + period, method: 'delete' })
}
