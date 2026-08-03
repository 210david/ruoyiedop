import request from '@/utils/request'

// 看板汇总数据
export function getDashboardSummary() {
  return request({
    url: '/pms/dashboard/summary',
    method: 'get'
  })
}

// 最近采购订单
export function getRecentOrders() {
  return request({
    url: '/pms/dashboard/recentOrders',
    method: 'get'
  })
}

// 采购订单状态分布
export function getOrderStatusDist() {
  return request({
    url: '/pms/dashboard/orderStatusDist',
    method: 'get'
  })
}

// 采购合同状态分布
export function getContractStatusDist() {
  return request({
    url: '/pms/dashboard/contractStatusDist',
    method: 'get'
  })
}

// 询比价状态分布
export function getInquiryStatusDist() {
  return request({
    url: '/pms/dashboard/inquiryStatusDist',
    method: 'get'
  })
}

// 采购月度趋势（近12个月）
export function getMonthlyTrend() {
  return request({
    url: '/pms/dashboard/monthlyTrend',
    method: 'get'
  })
}

// 供应商采购金额排行 TOP10
export function getSupplierRanking() {
  return request({
    url: '/pms/dashboard/supplierRanking',
    method: 'get'
  })
}
