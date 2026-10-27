import request from '@/utils/request'

// ==================== 应收管理 ====================

// 查询应收单列表
export function listAr(query) {
  return request({
    url: '/fin/ar/list',
    method: 'get',
    params: query
  })
}

// 查询应收单详情
export function getAr(apId) {
  return request({
    url: '/fin/ar/' + apId,
    method: 'get'
  })
}

// 从已审核发货单生成应收单
export function generateAr() {
  return request({
    url: '/fin/ar/generate',
    method: 'post'
  })
}

// 同步已收金额（从营销系统）
export function syncReceived() {
  return request({
    url: '/fin/ar/syncReceived',
    method: 'post'
  })
}

// 收款核销
export function writeoffAr(apId, amount, remark) {
  return request({
    url: '/fin/ar/writeoff/' + apId,
    method: 'post',
    params: { amount, remark }
  })
}

// 作废应收单
export function voidAr(apId, reason) {
  return request({
    url: '/fin/ar/void/' + apId,
    method: 'put',
    params: { reason }
  })
}

// 账龄分析
export function getArAging() {
  return request({
    url: '/fin/ar/aging',
    method: 'get'
  })
}

// 逾期未收预警
export function getArOverdue() {
  return request({
    url: '/fin/ar/overdue',
    method: 'get'
  })
}

// 应收汇总
export function getArSummary(period) {
  return request({
    url: '/fin/ar/summary',
    method: 'get',
    params: { period }
  })
}
// 登记销项发票
export function invoiceAr(arId, invoiceNo, invoiceCode, invoiceDate) {
  return request({
    url: '/fin/ar/invoice/' + arId,
    method: 'put',
    params: { invoiceNo, invoiceCode, invoiceDate }
  })
}

// 坏账计提
export function badDebt(period) {
  return request({
    url: '/fin/ar/badDebt',
    method: 'post',
    params: { period }
  })
}

// 坏账计提记录
export function listBadDebt(period) {
  return request({
    url: '/fin/ar/badDebt/list',
    method: 'get',
    params: { period }
  })
}

// 客户维度应收余额
export function balanceByCustomer() {
  return request({
    url: '/fin/ar/balanceByCustomer',
    method: 'get'
  })
}
