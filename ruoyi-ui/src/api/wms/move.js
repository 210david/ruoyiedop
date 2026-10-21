import request from '@/utils/request'

export function listMove(query) {
  return request({
    url: '/wms/move/list',
    method: 'get',
    params: query
  })
}

export function getMove(moveId) {
  return request({
    url: '/wms/move/' + moveId,
    method: 'get'
  })
}

export function addMove(data) {
  return request({
    url: '/wms/move',
    method: 'post',
    data: data
  })
}

export function updateMove(data) {
  return request({
    url: '/wms/move',
    method: 'put',
    data: data
  })
}

export function delMove(moveId) {
  return request({
    url: '/wms/move/' + moveId,
    method: 'delete'
  })
}

export function approveMove(moveId, approveOpinion) {
  return request({
    url: '/wms/move/approve/' + moveId,
    method: 'put',
    params: { approveOpinion }
  })
}

export function rejectMove(moveId, approveOpinion) {
  return request({
    url: '/wms/move/reject/' + moveId,
    method: 'put',
    params: { approveOpinion }
  })
}

export function executeMove(moveId) {
  return request({
    url: '/wms/move/execute/' + moveId,
    method: 'put'
  })
}

export function voidMove(moveId) {
  return request({
    url: '/wms/move/void/' + moveId,
    method: 'put'
  })
}
