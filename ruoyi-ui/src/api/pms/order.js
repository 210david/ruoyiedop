import request from '@/utils/request'

export function listOrder(query) {
  return request({
    url: '/pms/order/list',
    method: 'get',
    params: query
  })
}

export function getOrder(orderId) {
  return request({
    url: '/pms/order/' + orderId,
    method: 'get'
  })
}

export function addOrder(data) {
  return request({
    url: '/pms/order',
    method: 'post',
    data: data
  })
}

export function updateOrder(data) {
  return request({
    url: '/pms/order',
    method: 'put',
    data: data
  })
}

export function delOrder(orderId) {
  return request({
    url: '/pms/order/' + orderId,
    method: 'delete'
  })
}

export function submitOrder(orderId) {
  return request({
    url: '/pms/order/submit/' + orderId,
    method: 'put'
  })
}

export function auditOrder(orderId, status, auditOpinion) {
  return request({
    url: '/pms/order/audit/' + orderId,
    method: 'put',
    params: { status, auditOpinion }
  })
}
