import request from '@/utils/request'

// ================= 返利政策（阶梯组） =================

// 查询政策组列表（按政策编号聚合，tiers 为阶梯行数组）
export function listPolicyGroups(query) {
  return request({ url: '/mk/rebate/policyList', method: 'get', params: query })
}

// 新增政策组（body 为阶梯行数组，政策编号由后端自动生成）
export function addPolicyGroup(data) {
  return request({ url: '/mk/rebate/policy', method: 'post', data: data })
}

// 修改政策组（整组替换，每行必须含 policyNo）
export function updatePolicyGroup(data) {
  return request({ url: '/mk/rebate/policy', method: 'put', data: data })
}

// 删除政策组（整组）
export function delPolicyGroup(policyNo) {
  return request({ url: '/mk/rebate/policy/' + policyNo, method: 'delete' })
}

// ================= 返利结算 =================

// 查询结算单列表
export function listSettles(query) {
  return request({ url: '/mk/rebate/settleList', method: 'get', params: query })
}

// 返利核算（body: { customerId, period: 'yyyyMM' }），返回生成的结算单数组
export function settleRebate(data) {
  return request({ url: '/mk/rebate/settle', method: 'post', data: data })
}

// ================= 返利冲抵台账 =================

// 查询冲抵台账列表
export function listOffsets(query) {
  return request({ url: '/mk/rebate/offsetList', method: 'get', params: query })
}

// 按结算单查冲抵明细
export function listOffsetBySettle(settleId) {
  return request({ url: '/mk/rebate/offset/' + settleId, method: 'get' })
}

// 冲抵登记（支持拆分，推送财务待办）
export function addOffset(data) {
  return request({ url: '/mk/rebate/offset', method: 'post', data: data })
}

// 冲抵审核（财务一级审批）
export function auditOffset(offsetId, data) {
  return request({ url: '/mk/rebate/offset/audit/' + offsetId, method: 'put', data: data })
}
