import request from '@/utils/request'

// 概览统计
export function getOverview() {
  return request({
    url: '/hr/statistics/overview',
    method: 'get'
  })
}

// 在册人员按部门分布
export function getHeadcountByOrg() {
  return request({
    url: '/hr/statistics/headcountByOrg',
    method: 'get'
  })
}

// 人员流动（按月统计入职/离职）
export function getFlowTrend(query) {
  return request({
    url: '/hr/statistics/flowTrend',
    method: 'get',
    params: query
  })
}

// 出勤月报（按部门聚合）
export function getAttendanceByOrg(query) {
  return request({
    url: '/hr/statistics/attendanceByOrg',
    method: 'get',
    params: query
  })
}

// 出勤趋势（按月）
export function getAttendanceTrend(query) {
  return request({
    url: '/hr/statistics/attendanceTrend',
    method: 'get',
    params: query
  })
}

// 人工成本（按月）
export function getCostByMonth(query) {
  return request({
    url: '/hr/statistics/costByMonth',
    method: 'get',
    params: query
  })
}

// 人工成本（按部门，指定月）
export function getCostByOrg(query) {
  return request({
    url: '/hr/statistics/costByOrg',
    method: 'get',
    params: query
  })
}

// 薪酬发放台账（批次）
export function getPayrollLedger(query) {
  return request({
    url: '/hr/statistics/payrollLedger',
    method: 'get',
    params: query
  })
}
