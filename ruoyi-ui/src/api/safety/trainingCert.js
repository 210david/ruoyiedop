import request from '@/utils/request'

export function listTrainingCert(query) {
  return request({ url: '/safety/training/cert/list', method: 'get', params: query })
}
export function getTrainingCert(certId) {
  return request({ url: '/safety/training/cert/' + certId, method: 'get' })
}
export function addTrainingCert(data) {
  return request({ url: '/safety/training/cert', method: 'post', data: data })
}
export function updateTrainingCert(data) {
  return request({ url: '/safety/training/cert', method: 'put', data: data })
}
export function delTrainingCert(certId) {
  return request({ url: '/safety/training/cert/' + certId, method: 'delete' })
}
