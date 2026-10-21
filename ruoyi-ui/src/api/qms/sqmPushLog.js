import request from '@/utils/request'

// SQM→PMS 推送日志查询
export function listSqmPushLog(query) {
  return request({ url: '/qms/sqmPushLog/list', method: 'get', params: query })
}
export function getSqmPushLog(logId) {
  return request({ url: '/qms/sqmPushLog/' + logId, method: 'get' })
}
export function delSqmPushLog(logIds) {
  return request({ url: '/qms/sqmPushLog/' + logIds, method: 'delete' })
}
