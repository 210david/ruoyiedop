import request from '@/utils/request'

export function listDefect(query) {
  return request({
    url: '/qms/defect/list',
    method: 'get',
    params: query
  })
}

export function getDefect(defectId) {
  return request({
    url: '/qms/defect/' + defectId,
    method: 'get'
  })
}

export function addDefect(data) {
  return request({
    url: '/qms/defect',
    method: 'post',
    data: data
  })
}

export function updateDefect(data) {
  return request({
    url: '/qms/defect',
    method: 'put',
    data: data
  })
}

export function delDefect(defectId) {
  return request({
    url: '/qms/defect/' + defectId,
    method: 'delete'
  })
}
