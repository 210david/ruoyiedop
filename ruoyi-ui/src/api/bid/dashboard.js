import request from '@/utils/request'

export function getOverview() {
  return request({ url: '/bid/dashboard/overview', method: 'get' })
}
export function getStatusDistribution() {
  return request({ url: '/bid/dashboard/statusDistribution', method: 'get' })
}
export function getTrend() {
  return request({ url: '/bid/dashboard/trend', method: 'get' })
}
export function getFailReason() {
  return request({ url: '/bid/dashboard/failReason', method: 'get' })
}
