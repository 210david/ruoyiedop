import request from '@/utils/request'

// 查询效期策略列表
export function listPolicy(query) {
  return request({ url: '/food/policy/list', method: 'get', params: query })
}

// 策略详情
export function getPolicy(policyId) {
  return request({ url: '/food/policy/' + policyId, method: 'get' })
}

// 新增策略
export function addPolicy(data) {
  return request({ url: '/food/policy', method: 'post', data: data })
}

// 修改策略
export function updatePolicy(data) {
  return request({ url: '/food/policy', method: 'put', data: data })
}

// 删除策略
export function delPolicy(policyIds) {
  return request({ url: '/food/policy/' + policyIds, method: 'delete' })
}

// 三级匹配测试
export function matchPolicy(query) {
  return request({ url: '/food/policy/match', method: 'get', params: query })
}

// 批量刷新效期状态
export function refreshExpire() {
  return request({ url: '/food/policy/refresh', method: 'put' })
}
