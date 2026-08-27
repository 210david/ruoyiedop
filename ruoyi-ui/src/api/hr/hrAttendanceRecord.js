import request from '@/utils/request'

// 查询考勤记录列表
export function listHrAttendanceRecord(query) {
  return request({
    url: '/hr/attendance/list',
    method: 'get',
    params: query
  })
}

// 查询考勤记录详细
export function getHrAttendanceRecord(recordId) {
  return request({
    url: '/hr/attendance/' + recordId,
    method: 'get'
  })
}

// 新增考勤记录
export function addHrAttendanceRecord(data) {
  return request({
    url: '/hr/attendance',
    method: 'post',
    data: data
  })
}

// 修改考勤记录
export function updateHrAttendanceRecord(data) {
  return request({
    url: '/hr/attendance',
    method: 'put',
    data: data
  })
}

// 删除考勤记录
export function delHrAttendanceRecord(recordId) {
  return request({
    url: '/hr/attendance/' + recordId,
    method: 'delete'
  })
}
