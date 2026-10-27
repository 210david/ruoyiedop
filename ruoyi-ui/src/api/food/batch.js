import request from '@/utils/request'

// 查询批次台账列表
export function listBatch(query) {
  return request({ url: '/food/batch/list', method: 'get', params: query })
}

// 批次详情
export function getBatch(batchId) {
  return request({ url: '/food/batch/' + batchId, method: 'get' })
}

// 新增批次
export function addBatch(data) {
  return request({ url: '/food/batch', method: 'post', data: data })
}

// 修改批次
export function updateBatch(data) {
  return request({ url: '/food/batch', method: 'put', data: data })
}

// 删除批次
export function delBatch(batchIds) {
  return request({ url: '/food/batch/' + batchIds, method: 'delete' })
}

// 合格判定
export function qualifyBatch(batchId, remark) {
  return request({ url: '/food/batch/qualify/' + batchId, method: 'put', data: { remark: remark } })
}

// 冻结
export function freezeBatch(batchId, reason) {
  return request({ url: '/food/batch/freeze/' + batchId, method: 'put', data: { freezeReason: reason } })
}

// 解冻
export function unfreezeBatch(batchId) {
  return request({ url: '/food/batch/unfreeze/' + batchId, method: 'put' })
}

// 报废
export function scrapBatch(batchId, reason) {
  return request({ url: '/food/batch/scrap/' + batchId, method: 'put', data: { freezeReason: reason } })
}

// 谱系查询
export function genealogy(batchId, direction) {
  return request({ url: '/food/batch/genealogy/' + batchId, method: 'get', params: { direction: direction } })
}

// 批次库存
export function batchStock(batchId) {
  return request({ url: '/food/batch/stock/list', method: 'get', params: { batchId: batchId } })
}

// 按物料查批次库存行（FEFO排序，含批次状态/效期）
export function listBatchStockByMaterial(materialId) {
  return request({ url: '/food/batch/stock/listByMaterial/' + materialId, method: 'get' })
}

// 状态日志
export function statusLog(batchId) {
  return request({ url: '/food/batch/statusLog/' + batchId, method: 'get' })
}

// 预留列表
export function listReserve(query) {
  return request({ url: '/food/batch/reserve/list', method: 'get', params: query })
}

// 新增预留
export function addReserve(data) {
  return request({ url: '/food/batch/reserve', method: 'post', data: data })
}

// 释放预留
export function releaseReserve(reserveId) {
  return request({ url: '/food/batch/reserve/release/' + reserveId, method: 'put' })
}

// 删除预留
export function deleteReserve(reserveIds) {
  return request({ url: '/food/batch/reserve/' + reserveIds, method: 'delete' })
}

// AI临期批次出库建议（FEFO口径）
export function expiryAdvice(days) {
  return request({ url: '/ai/bizAlert/expiryAdvice', method: 'get', params: { days } })
}
