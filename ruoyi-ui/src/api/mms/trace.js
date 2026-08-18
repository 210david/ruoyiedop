import request from '@/utils/request'

export function listTrace(query) {
  return request({ url: '/mms/trace/list', method: 'get', params: query })
}
export function getTrace(traceId) {
  return request({ url: '/mms/trace/' + traceId, method: 'get' })
}
