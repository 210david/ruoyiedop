import request from '@/utils/request'

export function listBom(query) {
  return request({
    url: '/mms/bom/list',
    method: 'get',
    params: query
  })
}

export function getBom(bomId) {
  return request({
    url: '/mms/bom/' + bomId,
    method: 'get'
  })
}

export function addBom(data) {
  return request({
    url: '/mms/bom',
    method: 'post',
    data: data
  })
}

export function updateBom(data) {
  return request({
    url: '/mms/bom',
    method: 'put',
    data: data
  })
}

export function delBom(bomId) {
  return request({
    url: '/mms/bom/' + bomId,
    method: 'delete'
  })
}

export function publishBom(bomId) {
  return request({
    url: '/mms/bom/publish/' + bomId,
    method: 'put'
  })
}

export function disableBom(bomId) {
  return request({
    url: '/mms/bom/disable/' + bomId,
    method: 'put'
  })
}

export function enableBom(bomId) {
  return request({
    url: '/mms/bom/enable/' + bomId,
    method: 'put'
  })
}

export function copyBom(bomId) {
  return request({
    url: '/mms/bom/copy/' + bomId,
    method: 'post'
  })
}

export function getBomTree(bomId) {
  return request({
    url: '/mms/bom/tree/' + bomId,
    method: 'get'
  })
}
