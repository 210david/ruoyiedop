import request from '@/utils/request'

// 查询营销活动列表
export function listActivity(query) {
  return request({ url: '/mk/activity/list', method: 'get', params: query })
}

// 查询营销活动详细
export function getActivity(activityId) {
  return request({ url: '/mk/activity/' + activityId, method: 'get' })
}

// 新增营销活动
export function addActivity(data) {
  return request({ url: '/mk/activity', method: 'post', data: data })
}

// 修改营销活动
export function updateActivity(data) {
  return request({ url: '/mk/activity', method: 'put', data: data })
}

// 删除营销活动
export function delActivity(activityId) {
  return request({ url: '/mk/activity/' + activityId, method: 'delete' })
}

// ====== P0-1: 活动状态流转 ======
export function changeActivityStatus(activityId, data) {
  return request({ url: '/mk/activity/changeStatus/' + activityId, method: 'put', data: data })
}

export function getActivityStatusLogs(activityId) {
  return request({ url: '/mk/activity/statusLogs/' + activityId, method: 'get' })
}

// ====== P1-4: 活动复盘 ======
export function reviewActivity(activityId) {
  return request({ url: '/mk/activity/review/' + activityId, method: 'get' })
}

export function saveReview(data) {
  return request({ url: '/mk/activity/saveReview', method: 'put', data: data })
}

// ====== P2-11: 活动复制 ======
export function copyActivity(activityId) {
  return request({ url: '/mk/activity/copy/' + activityId, method: 'post' })
}

// ====== P3-12: 批量操作 ======
export function batchChangeStatus(data) {
  return request({ url: '/mk/activity/batchChangeStatus', method: 'put', data: data })
}

export function batchUpdateOwner(data) {
  return request({ url: '/mk/activity/batchUpdateOwner', method: 'put', data: data })
}

// ====== P3-15: 报名链接 ======
export function getRegisterUrl(activityId) {
  return request({ url: '/mk/activity/registerUrl/' + activityId, method: 'get' })
}
