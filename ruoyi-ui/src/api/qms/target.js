import request from '@/utils/request'

export function listTarget(query) {
  return request({ url: '/qms/target/list', method: 'get', params: query })
}
export function getTarget(targetId) {
  return request({ url: '/qms/target/' + targetId, method: 'get' })
}
export function addTarget(data) {
  return request({ url: '/qms/target', method: 'post', data: data })
}
export function updateTarget(data) {
  return request({ url: '/qms/target', method: 'put', data: data })
}
export function delTarget(targetIds) {
  return request({ url: '/qms/target/' + targetIds, method: 'delete' })
}
