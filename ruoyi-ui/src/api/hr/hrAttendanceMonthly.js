import request from '@/utils/request'

// 查询考勤月报列表
export function listHrAttendanceMonthly(query) {
  return request({
    url: '/hr/monthly/list',
    method: 'get',
    params: query
  })
}

// 查询考勤月报详细
export function getHrAttendanceMonthly(monthlyId) {
  return request({
    url: '/hr/monthly/' + monthlyId,
    method: 'get'
  })
}

// 新增考勤月报
export function addHrAttendanceMonthly(data) {
  return request({
    url: '/hr/monthly',
    method: 'post',
    data: data
  })
}

// 修改考勤月报
export function updateHrAttendanceMonthly(data) {
  return request({
    url: '/hr/monthly',
    method: 'put',
    data: data
  })
}

// 删除考勤月报
export function delHrAttendanceMonthly(monthlyId) {
  return request({
    url: '/hr/monthly/' + monthlyId,
    method: 'delete'
  })
}
