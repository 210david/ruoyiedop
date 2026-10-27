import request from '@/utils/request'

// 查询技术变更单列表
export function listChange(query) {
  return request({ url: '/pdm/change/list', method: 'get', params: query })
}

// 查询变更单详情（含涉及资料明细）
export function getChange(changeId) {
  return request({ url: '/pdm/change/' + changeId, method: 'get' })
}

// 列出产品当前生效资料清单（供变更单勾选涉及资料）
export function listEffectiveDocs(productId) {
  return request({ url: '/pdm/change/effectiveDocs/' + productId, method: 'get' })
}

// 新增变更单（草稿）
export function addChange(data) {
  return request({ url: '/pdm/change', method: 'post', data: data })
}

// 修改变更单（仅草稿/已驳回）
export function updateChange(data) {
  return request({ url: '/pdm/change', method: 'put', data: data })
}

// 删除变更单（仅草稿）
export function delChange(changeIds) {
  return request({ url: '/pdm/change/' + changeIds, method: 'delete' })
}

// 提交审批
export function submitChange(changeId) {
  return request({ url: '/pdm/change/submit/' + changeId, method: 'put' })
}

// 变更审批（通过/驳回）
export function auditChange(data) {
  return request({ url: '/pdm/change/audit', method: 'put', data: data })
}

// 执行明细：按变更单对资料升版生效
export function executeItem(data) {
  return request({ url: '/pdm/change/execute', method: 'put', data: data })
}

// 关闭变更单
export function closeChange(changeId) {
  return request({ url: '/pdm/change/close/' + changeId, method: 'put' })
}
