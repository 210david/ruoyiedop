import request from '@/utils/request'

export function listMr(query) {
  return request({ url: '/qms/mr/list', method: 'get', params: query })
}
export function getMr(mrId) {
  return request({ url: '/qms/mr/' + mrId, method: 'get' })
}
export function addMr(data) {
  return request({ url: '/qms/mr', method: 'post', data: data })
}
export function updateMr(data) {
  return request({ url: '/qms/mr', method: 'put', data: data })
}
export function delMr(mrIds) {
  return request({ url: '/qms/mr/' + mrIds, method: 'delete' })
}
