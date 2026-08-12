import request from '@/utils/request'

export function listTrainingPlan(query) {
  return request({ url: '/safety/training/plan/list', method: 'get', params: query })
}
export function getTrainingPlan(planId) {
  return request({ url: '/safety/training/plan/' + planId, method: 'get' })
}
export function addTrainingPlan(data) {
  return request({ url: '/safety/training/plan', method: 'post', data: data })
}
export function updateTrainingPlan(data) {
  return request({ url: '/safety/training/plan', method: 'put', data: data })
}
export function delTrainingPlan(planId) {
  return request({ url: '/safety/training/plan/' + planId, method: 'delete' })
}

// 开始执行培训计划
export function startTrainingPlan(planId) {
  return request({ url: '/safety/training/plan/start/' + planId, method: 'put' })
}

// 完成培训计划
export function completeTrainingPlan(planId) {
  return request({ url: '/safety/training/plan/complete/' + planId, method: 'put' })
}

// 取消培训计划
export function cancelTrainingPlan(planId) {
  return request({ url: '/safety/training/plan/cancel/' + planId, method: 'put' })
}