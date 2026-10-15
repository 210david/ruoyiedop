import request from '@/utils/request'

export function listOutbound(query) {
  return request({
    url: '/wms/outbound/list',
    method: 'get',
    params: query
  })
}

export function getOutbound(orderId) {
  return request({
    url: '/wms/outbound/' + orderId,
    method: 'get'
  })
}

export function addOutbound(data) {
  return request({
    url: '/wms/outbound',
    method: 'post',
    data: data
  })
}

export function updateOutbound(data) {
  return request({
    url: '/wms/outbound',
    method: 'put',
    data: data
  })
}

export function delOutbound(orderId) {
  return request({
    url: '/wms/outbound/' + orderId,
    method: 'delete'
  })
}

export function submitOutbound(orderId) {
  return request({
    url: '/wms/outbound/submit/' + orderId,
    method: 'put'
  })
}

export function pickOutbound(orderId, detailId, pickQty) {
  return request({
    url: '/wms/outbound/pick/' + orderId + '/' + detailId,
    method: 'put',
    params: { pickQty }
  })
}

export function checkOutbound(orderId, detailId, qty) {
  return request({
    url: '/wms/outbound/check/' + orderId + '/' + detailId,
    method: 'put',
    params: { qty }
  })
}

/** FEFO批次分配预览（三重拦截：分配明细 + 被拦截批次原因） */
export function fefoPreview(materialId, warehouseId, qty) {
  return request({
    url: '/wms/outbound/fefoPreview',
    method: 'get',
    params: { materialId, warehouseId, qty }
  })
}

/** 仓库内可发批次列表（FEFO排序，已过三重拦截） */
export function shippableBatches(materialId, warehouseId) {
  return request({
    url: '/wms/outbound/shippableBatches',
    method: 'get',
    params: { materialId, warehouseId }
  })
}
