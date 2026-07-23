import request from '@/utils/request'

// 查询订单列表
export function listOrder(query) {
  return request({ url: '/mk/order/list', method: 'get', params: query })
}

// 查询订单详细
export function getOrder(orderId) {
  return request({ url: '/mk/order/' + orderId, method: 'get' })
}

// 新增订单
export function addOrder(data) {
  return request({ url: '/mk/order', method: 'post', data: data })
}

// 修改订单
export function updateOrder(data) {
  return request({ url: '/mk/order', method: 'put', data: data })
}

// 删除订单
export function delOrder(orderId) {
  return request({ url: '/mk/order/' + orderId, method: 'delete' })
}

// 订单发货
export function deliverOrder(data) {
  return request({ url: '/mk/order/deliver', method: 'put', data: data })
}

// 订单确认
export function confirmOrder(orderId) {
  return request({ url: '/mk/order/confirm/' + orderId, method: 'put' })
}

// 订单签收
export function receiveOrder(orderId, receivePerson) {
  return request({ url: '/mk/order/receive/' + orderId, method: 'put', params: { receivePerson } })
}

// 订单完成
export function completeOrder(orderId) {
  return request({ url: '/mk/order/complete/' + orderId, method: 'put' })
}

// 订单取消
export function cancelOrder(orderId, cancelReason) {
  return request({ url: '/mk/order/cancel/' + orderId, method: 'put', params: { cancelReason } })
}
