import request from '@/utils/request'

export function listKeyword(query) {
  return request({ url: '/bid/keyword/list', method: 'get', params: query })
}
export function getKeyword(keywordId) {
  return request({ url: '/bid/keyword/' + keywordId, method: 'get' })
}
export function addKeyword(data) {
  return request({ url: '/bid/keyword', method: 'post', data })
}
export function updateKeyword(data) {
  return request({ url: '/bid/keyword', method: 'put', data })
}
export function delKeyword(keywordIds) {
  return request({ url: '/bid/keyword/' + keywordIds, method: 'delete' })
}
