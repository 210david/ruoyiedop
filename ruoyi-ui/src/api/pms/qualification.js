import request from '@/utils/request'

export function listQualification(query) {
  return request({
    url: '/pms/qualification/list',
    method: 'get',
    params: query
  })
}

export function getQualification(qualId) {
  return request({
    url: '/pms/qualification/' + qualId,
    method: 'get'
  })
}

export function addQualification(data) {
  return request({
    url: '/pms/qualification',
    method: 'post',
    data: data
  })
}

export function updateQualification(data) {
  return request({
    url: '/pms/qualification',
    method: 'put',
    data: data
  })
}

export function delQualification(qualId) {
  return request({
    url: '/pms/qualification/' + qualId,
    method: 'delete'
  })
}

export function auditQualification(qualId, status, auditRemark) {
  return request({
    url: '/pms/qualification/audit/' + qualId,
    method: 'put',
    params: { status, auditRemark }
  })
}
