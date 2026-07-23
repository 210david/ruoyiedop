import request from '@/utils/request'

// 查询线索列表
export function listLead(query) {
  return request({ url: '/mk/lead/list', method: 'get', params: query })
}

// 查询线索详细
export function getLead(leadId) {
  return request({ url: '/mk/lead/' + leadId, method: 'get' })
}

// 新增线索
export function addLead(data) {
  return request({ url: '/mk/lead', method: 'post', data: data })
}

// 修改线索
export function updateLead(data) {
  return request({ url: '/mk/lead', method: 'put', data: data })
}

// 删除线索
export function delLead(leadId) {
  return request({ url: '/mk/lead/' + leadId, method: 'delete' })
}

// 领取线索
export function receiveLead(leadId, data) {
  return request({ url: '/mk/lead/receive/' + leadId, method: 'put', data: data })
}

// 审批通过领取申请
export function approveReceive(leadId, data) {
  return request({ url: '/mk/lead/approve/' + leadId, method: 'put', data: data })
}

// 拒绝领取申请
export function rejectReceive(leadId, data) {
  return request({ url: '/mk/lead/reject/' + leadId, method: 'put', data: data })
}

// 分配线索
export function assignLead(leadId, data) {
  return request({ url: '/mk/lead/assign/' + leadId, method: 'put', data: data })
}

// 线索转化
export function convertLead(leadId, data) {
  return request({ url: '/mk/lead/convert/' + leadId, method: 'put', data: data })
}

// ====== P1: 批量操作 + 退回公海 ======

// 批量分配线索
export function batchAssignLead(data) {
  return request({ url: '/mk/lead/batchAssign', method: 'put', data: data })
}

// 批量变更线索状态
export function batchUpdateLeadStatus(data) {
  return request({ url: '/mk/lead/batchStatus', method: 'put', data: data })
}

// 释放线索到公海
export function releaseLeadToPool(leadId) {
  return request({ url: '/mk/lead/release/' + leadId, method: 'put' })
}

// 更新跟进时间
export function updateFollowTime(leadId) {
  return request({ url: '/mk/lead/followTime/' + leadId, method: 'put' })
}

// ====== P2: 无效线索 ======

// 标记线索无效
export function invalidateLead(leadId, data) {
  return request({ url: '/mk/lead/invalidate/' + leadId, method: 'put', data: data })
}

// ====== P3: 查重 ======

// 线索查重
export function checkLeadDuplicate(companyName, contactPhone) {
  return request({ url: '/mk/lead/checkDuplicate', method: 'get', params: { companyName, contactPhone } })
}
