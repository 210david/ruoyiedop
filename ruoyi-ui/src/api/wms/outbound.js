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

export function checkOutbound(orderId, detailId, actualQty) {
  return request({
    url: '/wms/outbound/check/' + orderId + '/' + detailId,
    method: 'put',
    params: { actualQty }
  })
}
