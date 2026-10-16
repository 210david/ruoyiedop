import request from '@/utils/request'

// 查询价格政策列表
export function listPolicy(query) {
  return request({ url: '/mk/pricePolicy/list', method: 'get', params: query })
}

// 查询价格政策详细
export function getPolicy(policyId) {
  return request({ url: '/mk/pricePolicy/' + policyId, method: 'get' })
}

// 新增价格政策（政策编号由后端自动生成）
export function addPolicy(data) {
  return request({ url: '/mk/pricePolicy', method: 'post', data: data })
}

// 修改价格政策
export function updatePolicy(data) {
  return request({ url: '/mk/pricePolicy', method: 'put', data: data })
}

// 删除价格政策
export function delPolicy(policyIds) {
  return request({ url: '/mk/pricePolicy/' + policyIds, method: 'delete' })
}

// 政策取价（客户专价→等级价），返回 { unitPrice, source, policyNo }
export function applyPrice(query) {
  return request({ url: '/mk/pricePolicy/apply', method: 'get', params: query })
}
