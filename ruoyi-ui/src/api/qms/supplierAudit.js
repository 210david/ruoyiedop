import request from '@/utils/request'

export function listSupplierAudit(query) {
  return request({ url: '/qms/supplierAudit/list', method: 'get', params: query })
}
export function getSupplierAudit(auditId) {
  return request({ url: '/qms/supplierAudit/' + auditId, method: 'get' })
}
export function addSupplierAudit(data) {
  return request({ url: '/qms/supplierAudit', method: 'post', data: data })
}
export function updateSupplierAudit(data) {
  return request({ url: '/qms/supplierAudit', method: 'put', data: data })
}
export function delSupplierAudit(auditIds) {
  return request({ url: '/qms/supplierAudit/' + auditIds, method: 'delete' })
}
