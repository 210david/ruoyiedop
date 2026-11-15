import request from '@/utils/request'

// 查询车辆台账列表
export function listOaVehicle(query) {
  return request({
    url: '/oa/vehicle/list',
    method: 'get',
    params: query
  })
}

// 查询车辆台账详细
export function getOaVehicle(vehicleId) {
  return request({
    url: '/oa/vehicle/' + vehicleId,
    method: 'get'
  })
}

// 新增车辆台账
export function addOaVehicle(data) {
  return request({
    url: '/oa/vehicle',
    method: 'post',
    data: data
  })
}

// 修改车辆台账
export function updateOaVehicle(data) {
  return request({
    url: '/oa/vehicle',
    method: 'put',
    data: data
  })
}

// 删除车辆台账
export function delOaVehicle(vehicleId) {
  return request({
    url: '/oa/vehicle/' + vehicleId,
    method: 'delete'
  })
}
