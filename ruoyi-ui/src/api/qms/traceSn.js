import request from '@/utils/request'

export function listSn(query) {
  return request({ url: '/qms/trace/sn/list', method: 'get', params: query })
}
export function getSn(id) {
  return request({ url: '/qms/trace/sn/' + id, method: 'get' })
}
export function getSnByCode(snCode) {
  return request({ url: '/qms/trace/sn/code/' + snCode, method: 'get' })
}
export function getSnByBatch(batchNo) {
  return request({ url: '/qms/trace/sn/batch/' + batchNo, method: 'get' })
}
export function addSn(data) {
  return request({ url: '/qms/trace/sn', method: 'post', data })
}
export function updateSn(data) {
  return request({ url: '/qms/trace/sn', method: 'put', data })
}
export function delSn(ids) {
  return request({ url: '/qms/trace/sn/' + ids, method: 'delete' })
}
export function exportTracePdf(batchNo) {
  return request({ url: '/qms/report/exportPdf/' + batchNo, method: 'post', responseType: 'blob' })
}
