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
