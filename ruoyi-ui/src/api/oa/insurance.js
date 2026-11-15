import request from '@/utils/request'

// 查询车辆保险台账列表
export function listOaInsurance(query) {
  return request({
    url: '/oa/insurance/list',
    method: 'get',
    params: query
  })
}

// 查询车辆保险台账详细
export function getOaInsurance(insuranceId) {
  return request({
    url: '/oa/insurance/' + insuranceId,
    method: 'get'
  })
}

// 新增车辆保险台账
export function addOaInsurance(data) {
  return request({
    url: '/oa/insurance',
    method: 'post',
    data: data
  })
}

// 修改车辆保险台账
export function updateOaInsurance(data) {
  return request({
    url: '/oa/insurance',
    method: 'put',
    data: data
  })
}

// 删除车辆保险台账
export function delOaInsurance(insuranceId) {
  return request({
    url: '/oa/insurance/' + insuranceId,
    method: 'delete'
  })
}
