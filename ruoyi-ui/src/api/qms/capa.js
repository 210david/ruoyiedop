import request from '@/utils/request'

export function listCapa(query) {
  return request({ url: '/qms/capa/list', method: 'get', params: query })
}
export function getCapa(capaId) {
  return request({ url: '/qms/capa/' + capaId, method: 'get' })
}
export function addCapa(data) {
  return request({ url: '/qms/capa', method: 'post', data: data })
}
export function updateCapa(data) {
  return request({ url: '/qms/capa', method: 'put', data: data })
}
export function delCapa(capaIds) {
  return request({ url: '/qms/capa/' + capaIds, method: 'delete' })
}
export function closeCapa(capaId) {
  return request({ url: '/qms/capa/close/' + capaId, method: 'put' })
}
