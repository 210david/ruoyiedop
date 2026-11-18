import request from '@/utils/request'

// 查询司机台账列表
export function listOaDriver(query) {
  return request({
    url: '/oa/driver/list',
    method: 'get',
    params: query
  })
}

// 浏览框选择司机分页列表
export function pickerDriverList(query) {
  return request({
    url: '/oa/driver/pickerList',
    method: 'get',
    params: query
  })
}

// 查询司机台账详细
export function getOaDriver(driverId) {
  return request({
    url: '/oa/driver/' + driverId,
    method: 'get'
  })
}

// 新增司机台账
export function addOaDriver(data) {
  return request({
    url: '/oa/driver',
    method: 'post',
    data: data
  })
}

// 修改司机台账
export function updateOaDriver(data) {
  return request({
    url: '/oa/driver',
    method: 'put',
    data: data
  })
}

// 删除司机台账
export function delOaDriver(driverId) {
  return request({
    url: '/oa/driver/' + driverId,
    method: 'delete'
  })
}
