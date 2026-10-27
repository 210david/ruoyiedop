import request from '@/utils/request'

export function listContract(query) {
  return request({
    url: '/pms/contract/list',
    method: 'get',
    params: query
  })
}

export function getContract(contractId) {
  return request({
    url: '/pms/contract/' + contractId,
    method: 'get'
  })
}

export function addContract(data) {
  return request({
    url: '/pms/contract',
    method: 'post',
    data: data
  })
}

export function updateContract(data) {
  return request({
    url: '/pms/contract',
    method: 'put',
    data: data
  })
}

export function delContract(contractId) {
  return request({
    url: '/pms/contract/' + contractId,
    method: 'delete'
  })
}

export function addContractChange(data) {
  return request({
    url: '/pms/contract/change',
    method: 'post',
    data: data
  })
}

export function auditContractChange(changeId, auditStatus, auditRemark) {
  return request({
    url: '/pms/contract/change/audit/' + changeId,
    method: 'put',
    params: { auditStatus, auditRemark }
  })
}

// 按合同ID批量审核变更
export function auditContractChangeByContractId(contractId, auditStatus, auditRemark) {
  return request({
    url: '/pms/contract/change/audit/contract/' + contractId + '/' + auditStatus,
    method: 'put',
    params: { auditRemark }
  })
}

// 提交审核
export function submitContract(contractId) {
  return request({
    url: '/pms/contract/submit/' + contractId,
    method: 'put'
  })
}

// 审核合同
export function auditContract(contractId, status, auditOpinion) {
  return request({
    url: '/pms/contract/audit/' + contractId,
    method: 'put',
    params: { status, auditOpinion }
  })
}

// 终止合同
export function terminateContract(contractId, terminateReason) {
  return request({
    url: '/pms/contract/terminate/' + contractId,
    method: 'put',
    params: { terminateReason }
  })
}
