import request from '@/utils/request'

// 安全守则库（需求一）
export function listRule(query) {
  return request({ url: '/safety/rule/list', method: 'get', params: query })
}
export function getRule(ruleId) {
  return request({ url: '/safety/rule/' + ruleId, method: 'get' })
}
export function addRule(data) {
  return request({ url: '/safety/rule', method: 'post', data: data })
}
export function updateRule(data) {
  return request({ url: '/safety/rule', method: 'put', data: data })
}
export function delRule(ruleIds) {
  return request({ url: '/safety/rule/' + ruleIds, method: 'delete' })
}
// 发布守则（草稿→生效，自动生成新版本）
export function publishRule(ruleId) {
  return request({ url: '/safety/rule/publish/' + ruleId, method: 'put' })
}
// SR-02 废止守则（生效守则不允许删除，只可废止并留痕）
export function abolishRule(ruleId) {
  return request({ url: '/safety/rule/abolish/' + ruleId, method: 'put' })
}
// 历史版本列表
export function listRuleVersions(ruleId) {
  return request({ url: '/safety/rule/' + ruleId + '/versions', method: 'get' })
}
// 覆盖率统计（应签阅/已签阅）
export function ruleLearnStats(ruleId) {
  return request({ url: '/safety/rule/' + ruleId + '/learnStats', method: 'get' })
}
