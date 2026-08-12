import request from '@/utils/request'

export function listMaterial(query) {
  return request({ url: '/safety/material/list', method: 'get', params: query })
}
export function getMaterial(materialId) {
  return request({ url: '/safety/material/' + materialId, method: 'get' })
}
export function addMaterial(data) {
  return request({ url: '/safety/material', method: 'post', data: data })
}
export function updateMaterial(data) {
  return request({ url: '/safety/material', method: 'put', data: data })
}
export function delMaterial(materialId) {
  return request({ url: '/safety/material/' + materialId, method: 'delete' })
}