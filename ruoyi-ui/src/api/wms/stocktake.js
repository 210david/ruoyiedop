import request from '@/utils/request'

export function listStockTake(query) {
  return request({
    url: '/wms/stocktake/list',
    method: 'get',
    params: query
  })
}

export function getStockTake(takeId) {
  return request({
    url: '/wms/stocktake/' + takeId,
    method: 'get'
  })
}

export function addStockTake(data) {
  return request({
    url: '/wms/stocktake',
    method: 'post',
    data: data
  })
}

export function updateStockTake(data) {
  return request({
    url: '/wms/stocktake',
    method: 'put',
    data: data
  })
}

export function delStockTake(takeId) {
  return request({
    url: '/wms/stocktake/' + takeId,
    method: 'delete'
  })
}

export function startStockTake(takeId) {
  return request({
    url: '/wms/stocktake/start/' + takeId,
    method: 'put'
  })
}

export function submitStockTakeDetail(takeId, detailId, actualQty, diffReason) {
  return request({
    url: '/wms/stocktake/submit/' + takeId + '/' + detailId,
    method: 'put',
    params: { actualQty, diffReason }
  })
}

export function batchSubmitDetail(takeId, detailList) {
  return request({
    url: '/wms/stocktake/batch-submit/' + takeId,
    method: 'put',
    data: detailList
  })
}

export function importDetailUrl(takeId) {
  return '/wms/stocktake/import/' + takeId
}

export function exportTemplateUrl(takeId) {
  return '/wms/stocktake/export-template/' + takeId
}

export function approveStockTake(takeId, approveOpinion) {
  return request({
    url: '/wms/stocktake/approve/' + takeId,
    method: 'put',
    params: { approveOpinion }
  })
}

export function rejectStockTake(takeId, approveOpinion) {
  return request({
    url: '/wms/stocktake/reject/' + takeId,
    method: 'put',
    params: { approveOpinion }
  })
}

export function voidStockTake(takeId) {
  return request({
    url: '/wms/stocktake/void/' + takeId,
    method: 'put'
  })
}

export function submitForApproval(takeId) {
  return request({
    url: '/wms/stocktake/submit-approve/' + takeId,
    method: 'put'
  })
}
