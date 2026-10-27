import request from '@/utils/request'

// 查询工作日历列表
export function listHrWorkdayCalendar(query) {
  return request({
    url: '/hr/workdayCalendar/list',
    method: 'get',
    params: query
  })
}

// 查询工作日历详细
export function getHrWorkdayCalendar(calendarId) {
  return request({
    url: '/hr/workdayCalendar/' + calendarId,
    method: 'get'
  })
}

// 新增工作日历
export function addHrWorkdayCalendar(data) {
  return request({
    url: '/hr/workdayCalendar',
    method: 'post',
    data: data
  })
}

// 修改工作日历
export function updateHrWorkdayCalendar(data) {
  return request({
    url: '/hr/workdayCalendar',
    method: 'put',
    data: data
  })
}

// 删除工作日历
export function delHrWorkdayCalendar(calendarId) {
  return request({
    url: '/hr/workdayCalendar/' + calendarId,
    method: 'delete'
  })
}

// 生成年度工作日历
export function generateYearlyCalendar(params) {
  return request({
    url: '/hr/workdayCalendar/generate',
    method: 'post',
    params: params
  })
}

// 批量调整日期类型
export function batchAdjustDateType(data) {
  return request({
    url: '/hr/workdayCalendar/batchAdjust',
    method: 'put',
    params: data
  })
}
