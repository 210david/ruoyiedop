import request from '@/utils/request'

export function listQualification(query) {
  return request({ url: '/bid/qualification/list', method: 'get', params: query })
}
export function getQualification(qualificationId) {
  return request({ url: '/bid/qualification/' + qualificationId, method: 'get' })
}
export function addQualification(data) {
  return request({ url: '/bid/qualification', method: 'post', data })
}
export function updateQualification(data) {
  return request({ url: '/bid/qualification', method: 'put', data })
}
export function delQualification(qualificationIds) {
  return request({ url: '/bid/qualification/' + qualificationIds, method: 'delete' })
}
