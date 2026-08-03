import request from '@/utils/request'

export function listRequest(query) {
  return request({
    url: '/pms/request/list',
    method: 'get',
    params: query
  })
}

export function getRequest(requestId) {
  return request({
    url: '/pms/request/' + requestId,
    method: 'get'
  })
}

export function addRequest(data) {
  return request({
    url: '/pms/request',
    method: 'post',
    data: data
  })
}

export function updateRequest(data) {
  return request({
    url: '/pms/request',
    method: 'put',
    data: data
  })
}

export function delRequest(requestId) {
  return request({
    url: '/pms/request/' + requestId,
    method: 'delete'
  })
}

export function submitRequest(requestId) {
  return request({
    url: '/pms/request/submit/' + requestId,
    method: 'put'
  })
}

export function auditRequest(requestId, status, auditOpinion) {
  return request({
    url: '/pms/request/audit/' + requestId,
    method: 'put',
    params: { status, auditOpinion }
  })
}
