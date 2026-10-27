import request from '@/utils/request'

// ==================== 应付管理 ====================

// 查询应付单列表
export function listAp(query) {
  return request({
    url: '/fin/ap/list',
    method: 'get',
    params: query
  })
}

// 查询应付单详情
export function getAp(apId) {
  return request({
    url: '/fin/ap/' + apId,
    method: 'get'
  })
}

// 从已审核发票生成应付单
export function generateAp() {
  return request({
    url: '/fin/ap/generate',
    method: 'post'
  })
}

// 同步已付金额（从 PMS）
export function syncPaid() {
  return request({
    url: '/fin/ap/syncPaid',
    method: 'post'
  })
}

// 付款核销
export function writeoffAp(apId, amount, remark) {
  return request({
    url: '/fin/ap/writeoff/' + apId,
    method: 'post',
    params: { amount, remark }
  })
}

// 作废应付单
export function voidAp(apId, reason) {
  return request({
    url: '/fin/ap/void/' + apId,
    method: 'put',
    params: { reason }
  })
}

// 账龄分析
export function getApAging() {
  return request({
    url: '/fin/ap/aging',
    method: 'get'
  })
}

// 逾期预警
export function getApOverdue() {
  return request({
    url: '/fin/ap/overdue',
    method: 'get'
  })
}

// 应付汇总
export function getApSummary(period) {
  return request({
    url: '/fin/ap/summary',
    method: 'get',
    params: { period }
  })
}