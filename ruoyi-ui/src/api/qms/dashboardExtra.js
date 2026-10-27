import request from '@/utils/request'

export function clearDashboardCache() {
  return request({ url: '/qms/dashboard/cache', method: 'delete' })
}
export function getRecentCapas() {
  return request({ url: '/qms/dashboard/recentCapas', method: 'get' })
}
export function getRecentComplaints() {
  return request({ url: '/qms/dashboard/recentComplaints', method: 'get' })
}
// 供应商评价自动填充
export function autoFillSupplierEval(supplierId, evalPeriod) {
  return request({ url: '/qms/supplierEval/autoFill', method: 'get', params: { supplierId, evalPeriod } })
}
