import request from '@/utils/request'

// 查询排班列表
export function listHrSchedule(query) {
  return request({
    url: '/hr/schedule/list',
    method: 'get',
    params: query
  })
}

// 查询排班详细
export function getHrSchedule(scheduleId) {
  return request({
    url: '/hr/schedule/' + scheduleId,
    method: 'get'
  })
}

// 新增排班
export function addHrSchedule(data) {
  return request({
    url: '/hr/schedule',
    method: 'post',
    data: data
  })
}

// 修改排班
export function updateHrSchedule(data) {
  return request({
    url: '/hr/schedule',
    method: 'put',
    data: data
  })
}

// 删除排班
export function delHrSchedule(scheduleId) {
  return request({
    url: '/hr/schedule/' + scheduleId,
    method: 'delete'
  })
}
