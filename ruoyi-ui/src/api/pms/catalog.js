import request from '@/utils/request'

export function listCatalog(query) {
  return request({
    url: '/pms/catalog/list',
    method: 'get',
    params: query
  })
}

export function getCatalog(catalogId) {
  return request({
    url: '/pms/catalog/' + catalogId,
    method: 'get'
  })
}

export function addCatalog(data) {
  return request({
    url: '/pms/catalog',
    method: 'post',
    data: data
  })
}

export function updateCatalog(data) {
  return request({
    url: '/pms/catalog',
    method: 'put',
    data: data
  })
}

export function delCatalog(catalogId) {
  return request({
    url: '/pms/catalog/' + catalogId,
    method: 'delete'
  })
}
