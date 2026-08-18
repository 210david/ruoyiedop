import request from '@/utils/request'

export function listDaily(query) {
  return request({ url: '/mms/daily/list', method: 'get', params: query })
}
export function getDaily(statId) {
  return request({ url: '/mms/daily/' + statId, method: 'get' })
}
