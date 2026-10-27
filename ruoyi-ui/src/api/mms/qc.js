import request from '@/utils/request'

export function listQc(query) {
  return request({
    url: '/mms/qc/list',
    method: 'get',
    params: query
  })
}

export function getQc(qcId) {
  return request({
    url: '/mms/qc/' + qcId,
    method: 'get'
  })
}

export function addQc(data) {
  return request({
    url: '/mms/qc',
    method: 'post',
    data: data
  })
}

export function updateQc(data) {
  return request({
    url: '/mms/qc',
    method: 'put',
    data: data
  })
}

export function delQc(qcId) {
  return request({
    url: '/mms/qc/' + qcId,
    method: 'delete'
  })
}

export function inspectQc(data) {
  return request({
    url: '/mms/qc/inspect',
    method: 'put',
    data: data
  })
}
