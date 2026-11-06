import request from '@/utils/request'

// 查询耗用登记列表
export function listConsumption(query) {
  return request({ url: '/pms/consumption/list', method: 'get', params: query })
}

// 查询耗用登记详情
export function getConsumption(consumptionId) {
  return request({ url: '/pms/consumption/' + consumptionId, method: 'get' })
}

// 新增耗用登记
export function addConsumption(data) {
  return request({ url: '/pms/consumption', method: 'post', data: data })
}

// 修改耗用登记
export function updateConsumption(data) {
  return request({ url: '/pms/consumption', method: 'put', data: data })
}

// 删除耗用登记
export function delConsumption(consumptionIds) {
  return request({ url: '/pms/consumption/' + consumptionIds, method: 'delete' })
}
