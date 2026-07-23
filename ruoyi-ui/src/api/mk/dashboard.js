import request from '@/utils/request'

// 概览统计
export function getOverview() {
  return request({ url: '/mk/dashboard/overview', method: 'get' })
}

// 线索转化漏斗
export function getLeadFunnel() {
  return request({ url: '/mk/dashboard/leadFunnel', method: 'get' })
}

// 商机销售漏斗
export function getOpportunityPipeline() {
  return request({ url: '/mk/dashboard/opportunityPipeline', method: 'get' })
}

// 客户行业分布
export function getCustomerByIndustry() {
  return request({ url: '/mk/dashboard/customerByIndustry', method: 'get' })
}

// 客户等级分布
export function getCustomerByLevel() {
  return request({ url: '/mk/dashboard/customerByLevel', method: 'get' })
}

// 销售业绩排行
export function getSalesRanking() {
  return request({ url: '/mk/dashboard/salesRanking', method: 'get' })
}

// 合同/订单月度趋势
export function getRevenueTrend() {
  return request({ url: '/mk/dashboard/revenueTrend', method: 'get' })
}

// 活动效果统计
export function getActivityResult() {
  return request({ url: '/mk/dashboard/activityResult', method: 'get' })
}

// P3-14: 待跟进联系人提醒
export function getFollowUpReminders() {
  return request({ url: '/mk/dashboard/followUpReminders', method: 'get' })
}

// 赢单输单分析
export function getWinLossAnalysis() {
  return request({ url: '/mk/dashboard/winLossAnalysis', method: 'get' })
}

// 销售预测
export function getSalesForecast() {
  return request({ url: '/mk/dashboard/salesForecast', method: 'get' })
}
