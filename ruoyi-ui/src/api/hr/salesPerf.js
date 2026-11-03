import request from '@/utils/request'

// 营销岗档案扩展（需求三）
export function listProfile(query) {
  return request({ url: '/hr/salesProfile/list', method: 'get', params: query })
}
export function getProfile(profileId) {
  return request({ url: '/hr/salesProfile/' + profileId, method: 'get' })
}
export function addProfile(data) {
  return request({ url: '/hr/salesProfile', method: 'post', data: data })
}
export function updateProfile(data) {
  return request({ url: '/hr/salesProfile', method: 'put', data: data })
}
export function delProfile(profileIds) {
  return request({ url: '/hr/salesProfile/' + profileIds, method: 'delete' })
}

// 业绩归集与补录
export function listPerf(query) {
  return request({ url: '/hr/perf/list', method: 'get', params: query })
}
export function perfRank(period) {
  return request({ url: '/hr/perf/rank', method: 'get', params: { period } })
}
export function aggregatePerf(period) {
  return request({ url: '/hr/perf/aggregate/' + period, method: 'post' })
}
export function listManual(query) {
  return request({ url: '/hr/perf/manual/list', method: 'get', params: query })
}
export function addManual(data) {
  return request({ url: '/hr/perf/manual', method: 'post', data: data })
}
export function approveManual(data) {
  return request({ url: '/hr/perf/manual/approve', method: 'put', data: data })
}
