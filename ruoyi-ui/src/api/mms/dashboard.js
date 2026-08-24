import request from '@/utils/request'

// 综合看板
export function getOverview() {
  return request({
    url: '/mms/dashboard/overview',
    method: 'get'
  })
}

// 工单统计
export function getWorkOrderStats() {
  return request({
    url: '/mms/dashboard/workorder/stats',
    method: 'get'
  })
}

// 计划统计
export function getMpsStats() {
  return request({
    url: '/mms/dashboard/mps/stats',
    method: 'get'
  })
}

// 异常统计
export function getAbnormalStats() {
  return request({
    url: '/mms/dashboard/abnormal/stats',
    method: 'get'
  })
}

// 报工统计
export function getReportStats() {
  return request({
    url: '/mms/dashboard/report/stats',
    method: 'get'
  })
}

// 工单状态分布
export function getWorkOrderStatusDist() {
  return request({
    url: '/mms/dashboard/workorder/statusDist',
    method: 'get'
  })
}

// 工单月度趋势
export function getWorkOrderTrend() {
  return request({
    url: '/mms/dashboard/workorder/trend',
    method: 'get'
  })
}

// 产品产量TOP10
export function getProductTop10() {
  return request({
    url: '/mms/dashboard/product/top10',
    method: 'get'
  })
}

// 异常类型分布
export function getAbnormalTypeDist() {
  return request({
    url: '/mms/dashboard/abnormal/typeDist',
    method: 'get'
  })
}

// 异常严重等级分布
export function getAbnormalSeverityDist() {
  return request({
    url: '/mms/dashboard/abnormal/severityDist',
    method: 'get'
  })
}

// 报工产量趋势
export function getReportTrend() {
  return request({
    url: '/mms/dashboard/report/trend',
    method: 'get'
  })
}

// 工单来源分析
export function getWorkOrderSourceDist() {
  return request({
    url: '/mms/dashboard/workorder/sourceDist',
    method: 'get'
  })
}

// 产能单元负荷统计
export function getResourceLoad() {
  return request({
    url: '/mms/dashboard/resource/load',
    method: 'get'
  })
}

// 工单类型分布
export function getWorkOrderTypeDist() {
  return request({
    url: '/mms/dashboard/workorder/typeDist',
    method: 'get'
  })
}
