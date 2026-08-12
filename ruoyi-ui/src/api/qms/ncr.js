import request from '@/utils/request'

export function listNcr(query) {
  return request({
    url: '/qms/ncr/list',
    method: 'get',
    params: query
  })
}

export function getNcr(ncrId) {
  return request({
    url: '/qms/ncr/' + ncrId,
    method: 'get'
  })
}

export function addNcr(data) {
  return request({
    url: '/qms/ncr',
    method: 'post',
    data: data
  })
}

export function updateNcr(data) {
  return request({
    url: '/qms/ncr',
    method: 'put',
    data: data
  })
}

export function delNcr(ncrId) {
  return request({
    url: '/qms/ncr/' + ncrId,
    method: 'delete'
  })
}

// 提交MRB评审
export function submitReview(ncrId) {
  return request({
    url: '/qms/ncr/submitReview/' + ncrId,
    method: 'put'
  })
}

// MRB审批通过
export function approveNcr(ncrId, approveOpinion) {
  return request({
    url: '/qms/ncr/approve/' + ncrId,
    method: 'put',
    params: { approveOpinion }
  })
}

// MRB审批驳回
export function rejectNcr(ncrId, approveOpinion) {
  return request({
    url: '/qms/ncr/reject/' + ncrId,
    method: 'put',
    params: { approveOpinion }
  })
}

// 提交处置结论
export function submitDisposition(data) {
  return request({
    url: '/qms/ncr/submitDisposition',
    method: 'put',
    data: data
  })
}

// NCR验证
export function verifyNcr(data) {
  return request({
    url: '/qms/ncr/verify',
    method: 'put',
    data: data
  })
}

// NCR关闭
export function closeNcr(ncrId) {
  return request({
    url: '/qms/ncr/close/' + ncrId,
    method: 'put'
  })
}
