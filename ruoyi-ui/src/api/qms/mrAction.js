import request from '@/utils/request'

export function listMrAction(query) {
  return request({ url: '/qms/mr/action/list', method: 'get', params: query })
}
export function getMrAction(mrId) {
  return request({ url: '/qms/mr/action/' + mrId, method: 'get' })
}
export function addMrAction(data) {
  return request({ url: '/qms/mr/action', method: 'post', data })
}
export function updateMrAction(data) {
  return request({ url: '/qms/mr/action', method: 'put', data })
}
export function closeMrAction(actionId, actionResult) {
  return request({ url: '/qms/mr/action/close/' + actionId, method: 'put', params: { actionResult } })
}
export function delMrAction(ids) {
  return request({ url: '/qms/mr/action/' + ids, method: 'delete' })
}
export function checkAllClosed(mrId) {
  return request({ url: '/qms/mr/action/checkAllClosed/' + mrId, method: 'get' })
}
export function getInputSummary(mrId) {
  return request({ url: '/qms/mr/inputSummary/' + mrId, method: 'get' })
}
