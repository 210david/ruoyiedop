import request from '@/utils/request'

// 安全守则执行检查（需求一，SR-04 整改闭环）
export function listRuleCheck(query) {
  return request({ url: '/safety/ruleCheck/list', method: 'get', params: query })
}
export function getRuleCheck(checkId) {
  return request({ url: '/safety/ruleCheck/' + checkId, method: 'get' })
}
export function addRuleCheck(data) {
  return request({ url: '/safety/ruleCheck', method: 'post', data: data })
}
export function updateRuleCheck(data) {
  return request({ url: '/safety/ruleCheck', method: 'put', data: data })
}
// 整改闭环复查（SR-04：不符合项必须填整改结果）
export function reviewRuleCheck(data) {
  return request({ url: '/safety/ruleCheck/review', method: 'put', data: data })
}
// 归档（SR-04：不符合项必须整改闭环后才能归档）
export function archiveRuleCheck(checkId) {
  return request({ url: '/safety/ruleCheck/archive/' + checkId, method: 'put' })
}
export function delRuleCheck(checkIds) {
  return request({ url: '/safety/ruleCheck/' + checkIds, method: 'delete' })
}
export function ruleCheckStats() {
  return request({ url: '/safety/ruleCheck/stats', method: 'get' })
}
