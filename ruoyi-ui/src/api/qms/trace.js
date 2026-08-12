import request from '@/utils/request'

// ==================== 谱系管理 ====================
export function listGenealogy(query) {
  return request({ url: '/qms/trace/genealogy/list', method: 'get', params: query })
}
export function getGenealogy(id) {
  return request({ url: '/qms/trace/genealogy/' + id, method: 'get' })
}
export function addGenealogy(data) {
  return request({ url: '/qms/trace/genealogy', method: 'post', data: data })
}
export function updateGenealogy(data) {
  return request({ url: '/qms/trace/genealogy', method: 'put', data: data })
}
export function delGenealogy(ids) {
  return request({ url: '/qms/trace/genealogy/' + ids, method: 'delete' })
}
export function batchAddGenealogy(data) {
  return request({ url: '/qms/trace/genealogy/batch', method: 'post', data: data })
}

// ==================== 追溯引擎 ====================
export function forwardTrace(batchNo) {
  return request({ url: '/qms/trace/forward/' + batchNo, method: 'get' })
}
export function backwardTrace(batchNo) {
  return request({ url: '/qms/trace/backward/' + batchNo, method: 'get' })
}

// ==================== 完整度看板 ====================
export function traceCompleteness() {
  return request({ url: '/qms/trace/completeness', method: 'get' })
}
export function traceBreakList() {
  return request({ url: '/qms/trace/breakList', method: 'get' })
}
