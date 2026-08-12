import request from '@/utils/request'

// 看板汇总数据
export function getDashboardSummary() {
  return request({
    url: '/qms/dashboard/summary',
    method: 'get'
  })
}

// 检验类型分布
export function getInspTypeDist() {
  return request({
    url: '/qms/dashboard/inspTypeDist',
    method: 'get'
  })
}

// 缺陷等级分布
export function getDefectLevelDist() {
  return request({
    url: '/qms/dashboard/defectLevelDist',
    method: 'get'
  })
}

// 最近检验任务
export function getRecentInspTasks() {
  return request({
    url: '/qms/dashboard/recentInspTasks',
    method: 'get'
  })
}

// 最近NCR记录
export function getRecentNcrs() {
  return request({
    url: '/qms/dashboard/recentNcrs',
    method: 'get'
  })
}
