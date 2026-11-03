import request from '@/utils/request'

// KPI考核（需求三）
export function listScheme(query) {
  return request({ url: '/hr/kpi/scheme/list', method: 'get', params: query })
}
export function getScheme(schemeId) {
  return request({ url: '/hr/kpi/scheme/' + schemeId, method: 'get' })
}
export function addScheme(data) {
  return request({ url: '/hr/kpi/scheme', method: 'post', data: data })
}
export function updateScheme(data) {
  return request({ url: '/hr/kpi/scheme', method: 'put', data: data })
}
export function delScheme(schemeIds) {
  return request({ url: '/hr/kpi/scheme/' + schemeIds, method: 'delete' })
}

export function listKpiRecord(query) {
  return request({ url: '/hr/kpi/record/list', method: 'get', params: query })
}
export function initiateRecords(data) {
  return request({ url: '/hr/kpi/record/initiate', method: 'post', data: data })
}
export function selfScore(data) {
  return request({ url: '/hr/kpi/record/self', method: 'put', data: data })
}
export function superiorScore(data) {
  return request({ url: '/hr/kpi/record/superior', method: 'put', data: data })
}
export function confirmRecord(recordId) {
  return request({ url: '/hr/kpi/record/confirm/' + recordId, method: 'put' })
}
export function appealRecord(data) {
  return request({ url: '/hr/kpi/record/appeal', method: 'put', data: data })
}
export function handleAppeal(data) {
  return request({ url: '/hr/kpi/record/appeal/handle', method: 'put', data: data })
}
