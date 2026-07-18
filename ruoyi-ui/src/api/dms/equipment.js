import request from '@/utils/request'

export function listEquipment(query) {
  return request({ url: '/dms/equipment/list', method: 'get', params: query })
}

export function getEquipment(equipmentId) {
  return request({ url: '/dms/equipment/' + equipmentId, method: 'get' })
}

export function addEquipment(data) {
  return request({ url: '/dms/equipment', method: 'post', data: data })
}

export function updateEquipment(data) {
  return request({ url: '/dms/equipment', method: 'put', data: data })
}

export function delEquipment(equipmentId) {
  return request({ url: '/dms/equipment/' + equipmentId, method: 'delete' })
}

export function getEquipmentHistory(equipmentId) {
  return request({ url: '/dms/equipment/history/' + equipmentId, method: 'get' })
}

export function listEquipmentLog(query) {
  return request({ url: '/dms/equipment/log/list', method: 'get', params: query })
}
