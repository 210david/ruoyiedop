import request from '@/utils/request'

export function listWave(query) {
  return request({
    url: '/wms/wave/list',
    method: 'get',
    params: query
  })
}

export function getWave(waveId) {
  return request({
    url: '/wms/wave/' + waveId,
    method: 'get'
  })
}

export function addWave(data) {
  return request({
    url: '/wms/wave',
    method: 'post',
    data: data
  })
}

export function updateWave(data) {
  return request({
    url: '/wms/wave',
    method: 'put',
    data: data
  })
}

export function delWave(waveId) {
  return request({
    url: '/wms/wave/' + waveId,
    method: 'delete'
  })
}

export function generateWave(waveId, orderIds) {
  return request({
    url: '/wms/wave/generate/' + waveId,
    method: 'put',
    data: orderIds
  })
}

export function releaseWave(waveId) {
  return request({
    url: '/wms/wave/release/' + waveId,
    method: 'put'
  })
}

export function getWaveOrders(waveId) {
  return request({
    url: '/wms/wave/orders/' + waveId,
    method: 'get'
  })
}
