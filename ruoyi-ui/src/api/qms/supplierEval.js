import request from '@/utils/request'

export function listSupplierEval(query) {
  return request({ url: '/qms/supplierEval/list', method: 'get', params: query })
}
export function getSupplierEval(evalId) {
  return request({ url: '/qms/supplierEval/' + evalId, method: 'get' })
}
export function addSupplierEval(data) {
  return request({ url: '/qms/supplierEval', method: 'post', data: data })
}
export function updateSupplierEval(data) {
  return request({ url: '/qms/supplierEval', method: 'put', data: data })
}
export function delSupplierEval(evalIds) {
  return request({ url: '/qms/supplierEval/' + evalIds, method: 'delete' })
}
