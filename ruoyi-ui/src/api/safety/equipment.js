import request from '@/utils/request'

export function listEquipment(query) {
  return request({ url: '/safety/equipment/list', method: 'get', params: query })
}
export function getEquipment(equipmentId) {
  return request({ url: '/safety/equipment/' + equipmentId, method: 'get' })
}
export function addEquipment(data) {
  return request({ url: '/safety/equipment', method: 'post', data: data })
}
export function updateEquipment(data) {
  return request({ url: '/safety/equipment', method: 'put', data: data })
}
export function delEquipment(equipmentId) {
  return request({ url: '/safety/equipment/' + equipmentId, method: 'delete' })
}