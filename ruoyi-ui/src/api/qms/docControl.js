import request from '@/utils/request'

// 版本管理
export function listDocVersion(query) {
  return request({ url: '/qms/doc/version/list', method: 'get', params: query })
}
export function getDocVersion(docId) {
  return request({ url: '/qms/doc/version/' + docId, method: 'get' })
}
export function publishVersion(data) {
  return request({ url: '/qms/doc/version/publish', method: 'post', data })
}
export function obsoleteVersion(versionId) {
  return request({ url: '/qms/doc/version/obsolete/' + versionId, method: 'put' })
}
export function delDocVersion(versionIds) {
  return request({ url: '/qms/doc/version/' + versionIds, method: 'delete' })
}

// 分发/借阅
export function listDistribute(query) {
  return request({ url: '/qms/doc/distribute/list', method: 'get', params: query })
}
export function getDistribute(docId) {
  return request({ url: '/qms/doc/distribute/' + docId, method: 'get' })
}
export function distributeDoc(data) {
  return request({ url: '/qms/doc/distribute', method: 'post', data })
}
export function borrowDoc(data) {
  return request({ url: '/qms/doc/borrow', method: 'post', data })
}
export function returnDoc(distributeId) {
  return request({ url: '/qms/doc/return/' + distributeId, method: 'put' })
}
export function receiveDoc(distributeId) {
  return request({ url: '/qms/doc/receive/' + distributeId, method: 'put' })
}
export function delDistribute(distributeIds) {
  return request({ url: '/qms/doc/distribute/' + distributeIds, method: 'delete' })
}

// 作废/引用检查
export function obsoleteDoc(docId) {
  return request({ url: '/qms/doc/obsolete/' + docId, method: 'put' })
}
export function referenceCheck(docId) {
  return request({ url: '/qms/doc/reference/' + docId, method: 'get' })
}
