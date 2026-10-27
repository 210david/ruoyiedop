import request from '@/utils/request'

// ==================== 资金管理 ====================

// 查询账户列表
export function listAccount(query) {
  return request({
    url: '/fin/fund/account/list',
    method: 'get',
    params: query
  })
}

// 查询账户详情
export function getAccount(accountId) {
  return request({
    url: '/fin/fund/account/' + accountId,
    method: 'get'
  })
}

// 新增账户
export function addAccount(data) {
  return request({
    url: '/fin/fund/account',
    method: 'post',
    data: data
  })
}

// 修改账户
export function updateAccount(data) {
  return request({
    url: '/fin/fund/account',
    method: 'put',
    data: data
  })
}

// 删除账户
export function delAccount(accountId) {
  return request({
    url: '/fin/fund/account/' + accountId,
    method: 'delete'
  })
}

// 查询资金流水
export function listFlow(query) {
  return request({
    url: '/fin/fund/flow/list',
    method: 'get',
    params: query
  })
}

// 登记资金流水
export function addFlow(data) {
  return request({
    url: '/fin/fund/flow',
    method: 'post',
    data: data
  })
}

// 账户间转账
export function transferFund(data) {
  return request({
    url: '/fin/fund/transfer',
    method: 'post',
    params: data
  })
}

// 标记已对账
export function reconcileFlow(flowIds) {
  return request({
    url: '/fin/fund/flow/reconcile/' + flowIds,
    method: 'put'
  })
}

// 资金日报
export function getDailyReport(date) {
  return request({
    url: '/fin/fund/dailyReport',
    method: 'get',
    params: { date }
  })
}

// 资金总余额
export function getTotalBalance() {
  return request({
    url: '/fin/fund/totalBalance',
    method: 'get'
  })
}

// 期间汇总
export function getFundSummary(period) {
  return request({
    url: '/fin/fund/summary',
    method: 'get',
    params: { period }
  })
}