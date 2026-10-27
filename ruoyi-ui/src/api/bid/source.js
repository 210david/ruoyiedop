import request from '@/utils/request'

export function listSource(query) {
  return request({ url: '/bid/source/list', method: 'get', params: query })
}
export function getSource(sourceId) {
  return request({ url: '/bid/source/' + sourceId, method: 'get' })
}
export function addSource(data) {
  return request({ url: '/bid/source', method: 'post', data })
}
export function updateSource(data) {
  return request({ url: '/bid/source', method: 'put', data })
}
export function delSource(sourceIds) {
  return request({ url: '/bid/source/' + sourceIds, method: 'delete' })
}
export function runSource(sourceId) {
  return request({ url: '/bid/source/run/' + sourceId, method: 'post' })
}
