import request from '@/utils/request'

// 查询批次规则列表
export function listRule(query) {
  return request({ url: '/food/rule/list', method: 'get', params: query })
}

// 规则详情
export function getRule(ruleId) {
  return request({ url: '/food/rule/' + ruleId, method: 'get' })
}

// 新增规则
export function addRule(data) {
  return request({ url: '/food/rule', method: 'post', data: data })
}

// 修改规则
export function updateRule(data) {
  return request({ url: '/food/rule', method: 'put', data: data })
}

// 删除规则
export function delRule(ruleIds) {
  return request({ url: '/food/rule/' + ruleIds, method: 'delete' })
}

// 规则试算预览
export function previewRule(data) {
  return request({ url: '/food/rule/preview', method: 'post', data: data })
}
