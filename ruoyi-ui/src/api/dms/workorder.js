import request from '@/utils/request'

export function listWorkorder(query) {
  return request({
    url: '/dms/workorder/list',
    method: 'get',
    params: query
  })
}

export function getWorkorder(orderId) {
  return request({
    url: '/dms/workorder/' + orderId,
    method: 'get'
  })
}

export function addWorkorder(data) {
  return request({
    url: '/dms/workorder',
    method: 'post',
    data: data
  })
}

export function updateWorkorder(data) {
  return request({
    url: '/dms/workorder',
    method: 'put',
    data: data
  })
}

export function delWorkorder(orderId) {
  return request({
    url: '/dms/workorder/' + orderId,
    method: 'delete'
  })
}

export function dispatchWorkorder(data) {
  return request({ url: '/dms/workorder/dispatch', method: 'put', data: data })
}

export function reassignWorkorder(data) {
  return request({ url: '/dms/workorder/reassign', method: 'put', data: data })
}

export function acceptWorkorder(orderId) {
  return request({ url: '/dms/workorder/accept/' + orderId, method: 'put' })
}

export function processWorkorder(orderId) {
  return request({ url: '/dms/workorder/process/' + orderId, method: 'put' })
}

export function completeWorkorder(data) {
  return request({ url: '/dms/workorder/complete', method: 'put', data: data })
}

export function verifyWorkorder(data) {
  return request({ url: '/dms/workorder/verify', method: 'put', data: data })
}

export function rejectWorkorder(data) {
  return request({ url: '/dms/workorder/reject', method: 'put', data: data })
}

export function cancelWorkorder(orderId) {
  return request({ url: '/dms/workorder/cancel/' + orderId, method: 'put' })
}

export function getWorkorderLog(orderId) {
  return request({ url: '/dms/workorder/log/' + orderId, method: 'get' })
}
