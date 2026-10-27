import request from '@/utils/request'

export function listAudit(query) {
  return request({ url: '/qms/audit/list', method: 'get', params: query })
}
export function getAudit(auditPlanId) {
  return request({ url: '/qms/audit/' + auditPlanId, method: 'get' })
}
export function addAudit(data) {
  return request({ url: '/qms/audit', method: 'post', data: data })
}
export function updateAudit(data) {
  return request({ url: '/qms/audit', method: 'put', data: data })
}
export function delAudit(auditPlanIds) {
  return request({ url: '/qms/audit/' + auditPlanIds, method: 'delete' })
}
