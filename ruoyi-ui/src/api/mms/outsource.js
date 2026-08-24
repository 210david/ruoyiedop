import request from '@/utils/request'

export function listOutsource(query) {
  return request({ url: '/mms/outsource/list', method: 'get', params: query })
}
export function getOutsource(outsourceId) {
  return request({ url: '/mms/outsource/' + outsourceId, method: 'get' })
}
export function addOutsource(data) {
  return request({ url: '/mms/outsource', method: 'post', data })
}
export function updateOutsource(data) {
  return request({ url: '/mms/outsource', method: 'put', data })
}
export function delOutsource(outsourceId) {
  return request({ url: '/mms/outsource/' + outsourceId, method: 'delete' })
}
export function sendOutOutsource(outsourceId, data) {
  return request({ url: '/mms/outsource/sendOut/' + outsourceId, method: 'put', data })
}
export function backAcceptOutsource(outsourceId, data) {
  return request({ url: '/mms/outsource/backAccept/' + outsourceId, method: 'put', data })
}
