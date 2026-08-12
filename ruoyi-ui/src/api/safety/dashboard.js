import request from '@/utils/request'

export function getStats() {
  return request({ url: '/safety/dashboard/stats', method: 'get' })
}

export function getChartData() {
  return request({ url: '/safety/dashboard/chartData', method: 'get', suppressError: true })
}

export function getFourColorMap(enterpriseId) {
  return request({ url: '/safety/risk/fourColorMap', method: 'get', params: { enterpriseId } })
}
