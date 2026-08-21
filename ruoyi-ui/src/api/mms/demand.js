import request from '@/utils/request'

export function listDemand(query) {
  return request({
    url: '/mms/demand/list',
    method: 'get',
    params: query
  })
}

export function getDemand(demandId) {
  return request({
    url: '/mms/demand/' + demandId,
    method: 'get'
  })
}

export function addDemand(data) {
  return request({
    url: '/mms/demand',
    method: 'post',
    data: data
  })
}

export function updateDemand(data) {
  return request({
    url: '/mms/demand',
    method: 'put',
    data: data
  })
}

export function delDemand(demandId) {
  return request({
    url: '/mms/demand/' + demandId,
    method: 'delete'
  })
}

// 需求确认
export function confirmDemand(demandId) {
  return request({
    url: '/mms/demand/confirm/' + demandId,
    method: 'put'
  })
}

// 需求取消确认
export function unconfirmDemand(demandId) {
  return request({
    url: '/mms/demand/unconfirm/' + demandId,
    method: 'put'
  })
}

// 需求转计划
export function demandToMps(demandId) {
  return request({
    url: '/mms/demand/toMps/' + demandId,
    method: 'post'
  })
}
