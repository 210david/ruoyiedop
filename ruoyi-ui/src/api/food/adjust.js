import request from '@/utils/request'

// 查询调整单列表
export function listAdjust(query) {
  return request({ url: '/food/adjust/list', method: 'get', params: query })
}

// 调整单详情（含明细）
export function getAdjust(adjustId) {
  return request({ url: '/food/adjust/' + adjustId, method: 'get' })
}

// 新增调整单
export function addAdjust(data) {
  return request({ url: '/food/adjust', method: 'post', data: data })
}

// 执行调整单
export function executeAdjust(adjustId) {
  return request({ url: '/food/adjust/execute/' + adjustId, method: 'put' })
}

// 取消调整单
export function cancelAdjust(adjustId) {
  return request({ url: '/food/adjust/cancel/' + adjustId, method: 'put' })
}

// 删除调整单
export function delAdjust(adjustIds) {
  return request({ url: '/food/adjust/' + adjustIds, method: 'delete' })
}
