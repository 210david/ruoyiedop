import request from '@/utils/request'

// 查询产品设计统计明细
export function listStatistics(query) {
  return request({
    url: '/pdm/statistics/list',
    method: 'get',
    params: query
  })
}

// 查询产品设计统计汇总
export function getStatisticsSummary(query) {
  return request({
    url: '/pdm/statistics/summary',
    method: 'get',
    params: query
  })
}

// 查询图表聚合数据（分类分布/状态分布/月度趋势/变更状态/产品产出TOP）
export function getStatisticsCharts() {
  return request({
    url: '/pdm/statistics/charts',
    method: 'get'
  })
}
