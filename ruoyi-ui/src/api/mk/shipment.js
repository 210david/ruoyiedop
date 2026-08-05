import request from '@/utils/request'

// 查询发货列表
export function listShipment(query, config = {}) {
return request({ url: '/mk/shipment/list', method: 'get', params: query, ...config })
}

// 查询发货详细
export function getShipment(shipmentId) {
  return request({ url: '/mk/shipment/' + shipmentId, method: 'get' })
}

// 新增发货
export function addShipment(data) {
  return request({ url: '/mk/shipment', method: 'post', data: data })
}

// 修改发货
export function updateShipment(data) {
  return request({ url: '/mk/shipment', method: 'put', data: data })
}

// 删除发货
export function delShipment(shipmentId) {
  return request({ url: '/mk/shipment/' + shipmentId, method: 'delete' })
}

// 确认发货
export function confirmShipment(data) {
  return request({ url: '/mk/shipment/confirm', method: 'put', data: data })
}

// 签收
export function receiveShipment(shipmentId) {
  return request({ url: '/mk/shipment/receive/' + shipmentId, method: 'put' })
}

// 获取存在进行中发货单的订单ID列表
export function getInProgressOrderIds() {
  return request({ url: '/mk/shipment/inProgressOrderIds', method: 'get' })
}
