import request from '@/utils/request'

export function listCapa(query) {
  return request({ url: '/qms/capa/list', method: 'get', params: query })
}
export function getCapa(capaId) {
  return request({ url: '/qms/capa/' + capaId, method: 'get' })
}
export function addCapa(data) {
  return request({ url: '/qms/capa', method: 'post', data: data })
}
export function updateCapa(data) {
  return request({ url: '/qms/capa', method: 'put', data: data })
}
export function delCapa(capaIds) {
  return request({ url: '/qms/capa/' + capaIds, method: 'delete' })
}
// 草稿/已驳回 → 进行中
export function submitCapa(capaId) {
  return request({ url: '/qms/capa/submit/' + capaId, method: 'put' })
}
// 进行中 → 验证中（填写D6验证结果）
export function submitVerifyCapa(data) {
  return request({ url: '/qms/capa/submitVerify', method: 'put', data: data })
}
// 验证中 → 已关闭（填写D7/D8）
export function closeCapa(data) {
  return request({ url: '/qms/capa/close', method: 'put', data: data })
}
// 验证中 → 已驳回
export function rejectCapa(capaId, rejectReason) {
  return request({ url: '/qms/capa/reject/' + capaId, method: 'put', params: { rejectReason } })
}
