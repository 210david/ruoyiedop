import request from '@/utils/request'

// 排产确认
export function confirmSchedule(scheduleId) {
  return request({
    url: '/mms/schedule/confirm/' + scheduleId,
    method: 'put'
  })
}

// 排产下达（生成/更新工单计划时间+派工单）
export function releaseSchedule(scheduleId) {
  return request({
    url: '/mms/schedule/release/' + scheduleId,
    method: 'put'
  })
}

// 排产取消
export function cancelSchedule(scheduleId, cancelReason) {
  return request({
    url: '/mms/schedule/cancel/' + scheduleId,
    method: 'put',
    params: { cancelReason }
  })
}

// ========== 甘特图排产工作台 ==========

// 按日期范围查询排产任务（甘特图数据）
export function getGanttData(startDate, endDate) {
  return request({
    url: '/mms/schedule/gantt',
    method: 'get',
    params: { startDate, endDate }
  })
}

// 拖拽保存：更新排产任务的时间和资源
export function dragSchedule(data) {
  return request({
    url: '/mms/schedule/drag',
    method: 'put',
    data: data
  })
}

// 批量排产
export function batchSchedule(data) {
  return request({
    url: '/mms/schedule/batch',
    method: 'post',
    data: data
  })
}

// 冲突检测
export function checkConflict(params) {
  return request({
    url: '/mms/schedule/checkConflict',
    method: 'get',
    params: params
  })
}

// 查询排产任务行列表（甘特图条目）
export function listScheduleTasks(query) {
  return request({
    url: '/mms/schedule/task/list',
    method: 'get',
    params: query
  })
}

// 保存排产任务行（拖拽调整后提交）
export function saveScheduleTask(data) {
  return request({
    url: '/mms/schedule/task',
    method: 'put',
    data: data
  })
}

// 查询所有有已下达排产记录的工单ID（不限日期范围，供待排产列表排除）
export function getScheduledWoIds() {
  return request({
    url: '/mms/schedule/wo/scheduled-ids',
    method: 'get'
  })
}
