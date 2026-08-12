import request from '@/utils/request'

export function listCert(query) {
  return request({ url: '/safety/cert/list', method: 'get', params: query })
}
export function getCert(certId) {
  return request({ url: '/safety/cert/' + certId, method: 'get' })
}
export function addCert(data) {
  return request({ url: '/safety/cert', method: 'post', data: data })
}
export function updateCert(data) {
  return request({ url: '/safety/cert', method: 'put', data: data })
}
export function delCert(certId) {
  return request({ url: '/safety/cert/' + certId, method: 'delete' })
}