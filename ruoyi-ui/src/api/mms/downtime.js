import request from '@/utils/request'

export function listDowntime(query) {
  return request({ url: '/mms/downtime/list', method: 'get', params: query })
}
export function getDowntime(downtimeId) {
  return request({ url: '/mms/downtime/' + downtimeId, method: 'get' })
}
export function addDowntime(data) {
  return request({ url: '/mms/downtime', method: 'post', data })
}
export function updateDowntime(data) {
  return request({ url: '/mms/downtime', method: 'put', data })
}
export function delDowntime(downtimeId) {
  return request({ url: '/mms/downtime/' + downtimeId, method: 'delete' })
}
