import request from '@/utils/request'

export function listDoc(query) {
  return request({ url: '/qms/doc/list', method: 'get', params: query })
}
export function getDoc(docId) {
  return request({ url: '/qms/doc/' + docId, method: 'get' })
}
export function addDoc(data) {
  return request({ url: '/qms/doc', method: 'post', data: data })
}
export function updateDoc(data) {
  return request({ url: '/qms/doc', method: 'put', data: data })
}
export function delDoc(docIds) {
  return request({ url: '/qms/doc/' + docIds, method: 'delete' })
}
