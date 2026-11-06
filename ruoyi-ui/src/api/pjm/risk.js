import request from '@/utils/request'

// 查询风险/问题列表
export function listRisk(query) {
  return request({ url: '/pjm/risk/list', method: 'get', params: query })
}
// 查询风险/问题详细
export function getRisk(riskId) {
  return request({ url: '/pjm/risk/' + riskId, method: 'get' })
}
// 新增风险/问题
export function addRisk(data) {
  return request({ url: '/pjm/risk', method: 'post', data: data })
}
// 修改风险/问题
export function updateRisk(data) {
  return request({ url: '/pjm/risk', method: 'put', data: data })
}
// 删除风险/问题
export function delRisk(riskIds) {
  return request({ url: '/pjm/risk/' + riskIds, method: 'delete' })
}
// ★ 按工程类别从国家标准风险点库推荐风险点
export function standardMatchRisk(engineeringCategory, keyword) {
  const params = { engineeringCategory }
  if (keyword) params.keyword = keyword
  return request({ url: '/pjm/risk/standard-match', method: 'get', params })
}
