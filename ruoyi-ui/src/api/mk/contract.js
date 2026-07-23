import request from '@/utils/request'

// 查询合同列表
export function listContract(query) {
  return request({
    url: '/mk/contract/list',
    method: 'get',
    params: query
  })
}

// 查询合同详细
export function getContract(contractId) {
  return request({
    url: '/mk/contract/' + contractId,
    method: 'get'
  })
}

// 新增合同
export function addContract(data) {
  return request({
    url: '/mk/contract',
    method: 'post',
    data: data
  })
}

// 修改合同
export function updateContract(data) {
  return request({
    url: '/mk/contract',
    method: 'put',
    data: data
  })
}

// 删除合同
export function delContract(contractId) {
  return request({
    url: '/mk/contract/' + contractId,
    method: 'delete'
  })
}

// 提交审批
export function submitContract(contractId) {
  return request({
    url: '/mk/contract/submit/' + contractId,
    method: 'put'
  })
}

// 审批通过
export function approveContract(contractId, approveOpinion) {
  return request({
    url: '/mk/contract/approve/' + contractId,
    method: 'put',
    params: { approveOpinion }
  })
}

// 审批驳回
export function rejectContract(contractId, approveOpinion) {
  return request({
    url: '/mk/contract/reject/' + contractId,
    method: 'put',
    params: { approveOpinion }
  })
}

// 终止合同
export function terminateContract(contractId, terminateReason) {
  return request({
    url: '/mk/contract/terminate/' + contractId,
    method: 'put',
    params: { terminateReason }
  })
}

// 合同续签
export function renewContract(contractId, data) {
  return request({
    url: '/mk/contract/renew/' + contractId,
    method: 'post',
    data: data
  })
}

// 查询即将到期合同
export function expiringContracts(days) {
  return request({
    url: '/mk/contract/expiring/' + days,
    method: 'get'
  })
}

// 提交合同变更
export function submitContractChange(data) {
  return request({
    url: '/mk/contract/change',
    method: 'post',
    data: data
  })
}

// 查询合同变更记录
export function contractChangeLogs(contractId) {
  return request({
    url: '/mk/contract/change/list/' + contractId,
    method: 'get'
  })
}

// 审批合同变更
export function approveContractChange(logId, approved, opinion) {
  return request({
    url: '/mk/contract/change/approve/' + logId + '/' + approved,
    method: 'put',
    params: { opinion }
  })
}
