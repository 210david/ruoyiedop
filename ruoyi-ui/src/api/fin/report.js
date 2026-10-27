import request from '@/utils/request'

// ==================== 财务报表 ====================

// 资产负债表
export function getBalanceSheet(period) {
  return request({
    url: '/fin/report/balanceSheet',
    method: 'get',
    params: { period }
  })
}

// 利润表
export function getIncomeStatement(period) {
  return request({
    url: '/fin/report/incomeStatement',
    method: 'get',
    params: { period }
  })
}

// 现金流量表
export function getCashFlow(period) {
  return request({
    url: '/fin/report/cashFlow',
    method: 'get',
    params: { period }
  })
}

// 往来对账
export function getReconciliation() {
  return request({
    url: '/fin/report/reconciliation',
    method: 'get'
  })
}

// 账龄分析
export function getReportAging(today) {
  return request({
    url: '/fin/report/aging',
    method: 'get',
    params: { today }
  })
}

// 会计恒等式校验
export function getBalanceCheck(period) {
  return request({
    url: '/fin/report/balanceCheck',
    method: 'get',
    params: { period }
  })
}