import request from '@/utils/request'

export function listInbound(query) {
  return request({
    url: '/wms/inbound/list',
    method: 'get',
    params: query
  })
}

export function getInbound(orderId) {
  return request({
    url: '/wms/inbound/' + orderId,
    method: 'get'
  })
}

export function addInbound(data) {
  return request({
    url: '/wms/inbound',
    method: 'post',
    data: data
  })
}

export function updateInbound(data) {
  return request({
    url: '/wms/inbound',
    method: 'put',
    data: data
  })
}

export function delInbound(orderId) {
  return request({
    url: '/wms/inbound/' + orderId,
    method: 'delete'
  })
}

export function submitInbound(orderId) {
  return request({
    url: '/wms/inbound/submit/' + orderId,
    method: 'put'
  })
}

export function receiveInbound(orderId, detailId, receivedQty) {
  return request({
    url: '/wms/inbound/receive/' + orderId + '/' + detailId,
    method: 'put',
    params: { receivedQty }
  })
}

export function putawayInbound(orderId, detailId, locationId, putawayQty) {
  return request({
    url: '/wms/inbound/putaway/' + orderId + '/' + detailId,
    method: 'put',
    params: { locationId, putawayQty }
  })
}
