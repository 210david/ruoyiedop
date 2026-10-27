import request from '@/utils/request'

export function listMaterialAttr(query) {
  return request({
    url: '/qms/materialattr/list',
    method: 'get',
    params: query
  })
}

export function getMaterialAttr(attrId) {
  return request({
    url: '/qms/materialattr/' + attrId,
    method: 'get'
  })
}

export function addMaterialAttr(data) {
  return request({
    url: '/qms/materialattr',
    method: 'post',
    data: data
  })
}

export function updateMaterialAttr(data) {
  return request({
    url: '/qms/materialattr',
    method: 'put',
    data: data
  })
}

export function delMaterialAttr(attrId) {
  return request({
    url: '/qms/materialattr/' + attrId,
    method: 'delete'
  })
}
