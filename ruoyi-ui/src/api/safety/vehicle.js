import request from '@/utils/request'

// 查询车辆台账列表
export function listVehicle(query) {
  return request({ url: '/safety/vehicle/list', method: 'get', params: query })
}

// 查询车辆详情
export function getVehicle(vehicleId) {
  return request({ url: '/safety/vehicle/' + vehicleId, method: 'get' })
}

// 新增车辆
export function addVehicle(data) {
  return request({ url: '/safety/vehicle', method: 'post', data: data })
}

// 修改车辆
export function updateVehicle(data) {
  return request({ url: '/safety/vehicle', method: 'put', data: data })
}

// 删除车辆
export function delVehicle(vehicleIds) {
  return request({ url: '/safety/vehicle/' + vehicleIds, method: 'delete' })
}

// 导出
export function exportVehicle(query) {
  return request({ url: '/safety/vehicle/export', method: 'post', params: query, responseType: 'blob' })
}
