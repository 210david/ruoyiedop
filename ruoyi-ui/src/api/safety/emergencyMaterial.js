import request from '@/utils/request'

export function listEmergencyMaterial(query) {
  return request({ url: '/safety/emergency/material/list', method: 'get', params: query })
}
export function getEmergencyMaterial(materialId) {
  return request({ url: '/safety/emergency/material/' + materialId, method: 'get' })
}
export function addEmergencyMaterial(data) {
  return request({ url: '/safety/emergency/material', method: 'post', data: data })
}
export function updateEmergencyMaterial(data) {
  return request({ url: '/safety/emergency/material', method: 'put', data: data })
}
export function delEmergencyMaterial(materialId) {
  return request({ url: '/safety/emergency/material/' + materialId, method: 'delete' })
}