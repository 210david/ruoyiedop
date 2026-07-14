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

export function approveStockTake(takeId) {
  return request({
    url: '/wms/stocktake/approve/' + takeId,
    method: 'put'
  })
}

export function voidStockTake(takeId) {
  return request({
    url: '/wms/stocktake/void/' + takeId,
    method: 'put'
  })
}
