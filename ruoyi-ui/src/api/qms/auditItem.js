import request from '@/utils/request'

export function listAuditItem(query) {
  return request({ url: '/qms/auditItem/list', method: 'get', params: query })
}
export function getAuditItem(itemId) {
  return request({ url: '/qms/auditItem/' + itemId, method: 'get' })
}
export function listByPlanId(auditPlanId) {
  return request({ url: '/qms/auditItem/byPlan/' + auditPlanId, method: 'get' })
}
export function addAuditItem(data) {
  return request({ url: '/qms/auditItem', method: 'post', data })
}
export function updateAuditItem(data) {
  return request({ url: '/qms/auditItem', method: 'put', data })
}
export function delAuditItem(itemIds) {
  return request({ url: '/qms/auditItem/' + itemIds, method: 'delete' })
}
export function createCapaFromItem(itemId) {
  return request({ url: '/qms/auditItem/createCapa/' + itemId, method: 'post' })
}
