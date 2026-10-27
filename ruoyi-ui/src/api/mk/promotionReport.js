import request from '@/utils/request'

// 推广看板 KPI 汇总（params: { beginDate, endDate }）
export function getOverview(params) {
  return request({ url: '/mk/promotionReport/overview', method: 'get', params: params })
}

// 渠道对比（params: { beginDate, endDate }）
export function getChannelCompare(params) {
  return request({ url: '/mk/promotionReport/channelCompare', method: 'get', params: params })
}

// 内容包效果排行 TOP10（params: { beginDate, endDate }）
export function getContentRank(params) {
  return request({ url: '/mk/promotionReport/contentRank', method: 'get', params: params })
}

// 活动归因漏斗（params: { activityId }）
export function getFunnel(params) {
  return request({ url: '/mk/promotionReport/funnel', method: 'get', params: params })
}

// 按日推广趋势（params: { beginDate, endDate }）
export function getTrend(params) {
  return request({ url: '/mk/promotionReport/trend', method: 'get', params: params })
}
