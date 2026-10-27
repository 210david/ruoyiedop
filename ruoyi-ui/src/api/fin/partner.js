import request from '@/utils/request'

// ==================== 财务往来单位 ====================

export function listPartner(query) {
  return request({
    url: '/fin/partner/list',
    method: 'get',
    params: query
  })
}

export function getPartner(partnerId) {
  return request({
    url: '/fin/partner/' + partnerId,
    method: 'get'
  })
}

export function addPartner(data) {
  return request({
    url: '/fin/partner',
    method: 'post',
    data: data
  })
}

export function updatePartner(data) {
  return request({
    url: '/fin/partner',
    method: 'put',
    data: data
  })
}

export function delPartner(partnerId) {
  return request({
    url: '/fin/partner/' + partnerId,
    method: 'delete'
  })
}