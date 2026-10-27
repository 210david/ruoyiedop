import request from '@/utils/request'

export function listShift(query) {
  return request({
    url: '/mms/shift/list',
    method: 'get',
    params: query
  })
}

export function getShift(shiftId) {
  return request({
    url: '/mms/shift/' + shiftId,
    method: 'get'
  })
}

export function addShift(data) {
  return request({
    url: '/mms/shift',
    method: 'post',
    data: data
  })
}

export function updateShift(data) {
  return request({
    url: '/mms/shift',
    method: 'put',
    data: data
  })
}

export function delShift(shiftId) {
  return request({
    url: '/mms/shift/' + shiftId,
    method: 'delete'
  })
}
