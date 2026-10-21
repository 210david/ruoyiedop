import request from '@/utils/request'

// 销售人员综合统计
export function getSalespersonOverview() {
  return request({ url: '/mk/dashboard/salespersonOverview', method: 'get' })
}

// 销售人员月度业绩趋势
export function getSalespersonTrend(userId) {
  return request({ url: '/mk/dashboard/salespersonTrend', method: 'get', params: { userId } })
}
