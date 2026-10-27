import request from '@/utils/request'

// ==================== 记账凭证（总账） ====================

// 查询凭证列表
export function listVoucher(query) {
  return request({
    url: '/fin/voucher/list',
    method: 'get',
    params: query
  })
}

// 查询凭证详情（含分录）
export function getVoucher(voucherId) {
  return request({
    url: '/fin/voucher/' + voucherId,
    method: 'get'
  })
}

// 新增凭证
export function addVoucher(data) {
  return request({
    url: '/fin/voucher',
    method: 'post',
    data: data
  })
}

// 修改凭证
export function updateVoucher(data) {
  return request({
    url: '/fin/voucher',
    method: 'put',
    data: data
  })
}

// 删除凭证
export function delVoucher(voucherId) {
  return request({
    url: '/fin/voucher/' + voucherId,
    method: 'delete'
  })
}

// 审核凭证
export function auditVoucher(voucherId, auditOpinion) {
  return request({
    url: '/fin/voucher/audit/' + voucherId,
    method: 'put',
    params: { auditOpinion }
  })
}

// 反审核
export function cancelAudit(voucherId) {
  return request({
    url: '/fin/voucher/cancelAudit/' + voucherId,
    method: 'put'
  })
}

// 试算平衡表
export function getTrialBalance(period) {
  return request({
    url: '/fin/voucher/trialBalance',
    method: 'get',
    params: { period }
  })
}

// 科目余额表
export function getSubjectBalance(period) {
  return request({
    url: '/fin/voucher/subjectBalance',
    method: 'get',
    params: { period }
  })
}
// 红字冲销：对已审核凭证生成负数冲销凭证（草稿）
export function redFlushVoucher(voucherId) {
  return request({
    url: '/fin/voucher/redflush/' + voucherId,
    method: 'post'
  })
}
