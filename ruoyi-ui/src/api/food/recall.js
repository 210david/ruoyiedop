import request from '@/utils/request'

// 查询召回单列表
export function listRecall(query) {
  return request({ url: '/food/recall/list', method: 'get', params: query })
}

// 召回单详情（含圈批明细）
export function getRecall(recallId) {
  return request({ url: '/food/recall/' + recallId, method: 'get' })
}

// 圈批预览：按谱系正/反向遍历关联批次（不落库不冻结）
export function circlesBatch(triggerBatchId, scope) {
  return request({ url: '/food/recall/circles', method: 'get', params: { triggerBatchId: triggerBatchId, scope: scope } })
}

// 召回单圈批明细
export function recallBatchList(recallId) {
  return request({ url: '/food/recall/batchList/' + recallId, method: 'get' })
}

// 新增召回单
export function addRecall(data) {
  return request({ url: '/food/recall', method: 'post', data: data })
}

// 执行召回：圈批冻结全部关联批次
export function executeRecall(recallId) {
  return request({ url: '/food/recall/execute/' + recallId, method: 'post' })
}

// 取消召回（仅待执行）
export function cancelRecall(recallId) {
  return request({ url: '/food/recall/cancel/' + recallId, method: 'post' })
}

// 删除召回单
export function delRecall(recallIds) {
  return request({ url: '/food/recall/' + recallIds, method: 'delete' })
}
