import request from '@/utils/request'

export function listAqlPlan(query) {
  return request({ url: '/qms/aqlplan/list', method: 'get', params: query })
}
export function getAqlPlan(planId) {
  return request({ url: '/qms/aqlplan/' + planId, method: 'get' })
}
export function lookupAqlPlan(aqlLevel, codeLetter, inspectLevel) {
  return request({ url: '/qms/aqlplan/lookup', method: 'get', params: { aqlLevel, codeLetter, inspectLevel } })
}
export function addAqlPlan(data) {
  return request({ url: '/qms/aqlplan', method: 'post', data })
}
export function updateAqlPlan(data) {
  return request({ url: '/qms/aqlplan', method: 'put', data })
}
export function delAqlPlan(planIds) {
  return request({ url: '/qms/aqlplan/' + planIds, method: 'delete' })
}
