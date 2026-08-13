import request from '@/utils/request'

// 根因分析
export function getRootCause(capaId) {
  return request({ url: '/qms/capa/rootCause/' + capaId, method: 'get' })
}
export function saveRootCause(data) {
  return request({ url: '/qms/capa/rootCause', method: 'post', data })
}

// 有效性追踪
export function listEffectiveness(query) {
  return request({ url: '/qms/capa/effectiveness/list', method: 'get', params: query })
}
export function getEffectiveness(capaId) {
  return request({ url: '/qms/capa/effectiveness/' + capaId, method: 'get' })
}
export function addEffectiveness(data) {
  return request({ url: '/qms/capa/effectiveness', method: 'post', data })
}
export function updateEffectiveness(data) {
  return request({ url: '/qms/capa/effectiveness', method: 'put', data })
}
export function delEffectiveness(ids) {
  return request({ url: '/qms/capa/effectiveness/' + ids, method: 'delete' })
}

// 一键发起CAPA
export function createCapaFromNcr(ncrId) {
  return request({ url: '/qms/ncr/createCapa/' + ncrId, method: 'post' })
}
export function createCapaFromComplaint(complaintId) {
  return request({ url: '/qms/complaint/createCapa/' + complaintId, method: 'post' })
}
export function createCapaFromAuditItem(itemId) {
  return request({ url: '/qms/auditItem/createCapa/' + itemId, method: 'post' })
}
