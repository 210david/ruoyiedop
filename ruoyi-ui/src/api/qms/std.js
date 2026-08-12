import request from '@/utils/request'

export function listStd(query) {
  return request({
    url: '/qms/std/list',
    method: 'get',
    params: query
  })
}

export function getStd(stdId) {
  return request({
    url: '/qms/std/' + stdId,
    method: 'get'
  })
}

export function addStd(data) {
  return request({
    url: '/qms/std',
    method: 'post',
    data: data
  })
}

export function updateStd(data) {
  return request({
    url: '/qms/std',
    method: 'put',
    data: data
  })
}

export function delStd(stdId) {
  return request({
    url: '/qms/std/' + stdId,
    method: 'delete'
  })
}
