import request from '@/utils/request'

export function listReturnMaterial(query) {
  return request({
    url: '/mms/return/list',
    method: 'get',
    params: query
  })
}

export function getReturnMaterial(returnId) {
  return request({
    url: '/mms/return/' + returnId,
    method: 'get'
  })
}

export function addReturnMaterial(data) {
  return request({
    url: '/mms/return',
    method: 'post',
    data: data
  })
}

export function delReturnMaterial(returnId) {
  return request({
    url: '/mms/return/' + returnId,
    method: 'delete'
  })
}

export function updateReturnMaterial(data) {
  return request({
    url: '/mms/return',
    method: 'put',
    data: data
  })
}

