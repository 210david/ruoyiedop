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
