import request from '@/utils/request'

export function listGauge(query) {
  return request({
    url: '/qms/gauge/list',
    method: 'get',
    params: query
  })
}

export function getGauge(gaugeId) {
  return request({
    url: '/qms/gauge/' + gaugeId,
    method: 'get'
  })
}

export function addGauge(data) {
  return request({
    url: '/qms/gauge',
    method: 'post',
    data: data
  })
}

export function updateGauge(data) {
  return request({
    url: '/qms/gauge',
    method: 'put',
    data: data
  })
}

export function delGauge(gaugeId) {
  return request({
    url: '/qms/gauge/' + gaugeId,
    method: 'delete'
  })
}
