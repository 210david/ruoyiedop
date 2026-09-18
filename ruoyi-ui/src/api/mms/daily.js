import request from '@/utils/request'

export function listDaily(query) {
  return request({ url: '/mms/daily/list', method: 'get', params: query })
}
export function getDaily(statDate) {
  return request({ url: '/mms/daily/' + statDate, method: 'get' })
}
