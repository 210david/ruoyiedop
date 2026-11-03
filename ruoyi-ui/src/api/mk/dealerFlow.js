import request from '@/utils/request'

// 查询流向登记列表
export function listFlow(query, config = {}) {
  return request({ url: '/mk/dealerFlow/list', method: 'get', params: query, ...config })
}

// 查询流向详情
export function getFlow(flowId) {
  return request({ url: '/mk/dealerFlow/' + flowId, method: 'get' })
}

// 按发货单生成流向草稿（不入库）
export function flowDraft(shipmentId) {
  return request({ url: '/mk/dealerFlow/draft/' + shipmentId, method: 'get' })
}

// 手工新增流向
export function addFlow(data) {
  return request({ url: '/mk/dealerFlow', method: 'post', data: data })
}

// 批量保存草稿（防重，已登记跳过）
export function batchSaveFlow(data) {
  return request({ url: '/mk/dealerFlow/batchSave', method: 'post', data: data })
}

// 修改流向
export function updateFlow(data) {
  return request({ url: '/mk/dealerFlow', method: 'put', data: data })
}

// 删除流向
export function delFlow(flowIds) {
  return request({ url: '/mk/dealerFlow/' + flowIds, method: 'delete' })
}
