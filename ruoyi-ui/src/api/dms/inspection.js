import request from '@/utils/request'

// ===== 巡检路线 =====
export function listRoute(query) {
  return request({ url: '/dms/inspection/route/list', method: 'get', params: query })
}
export function getRoute(routeId) {
  return request({ url: '/dms/inspection/route/' + routeId, method: 'get' })
}
export function addRoute(data) {
  return request({ url: '/dms/inspection/route', method: 'post', data: data })
}
export function updateRoute(data) {
  return request({ url: '/dms/inspection/route', method: 'put', data: data })
}
export function delRoute(routeId) {
  return request({ url: '/dms/inspection/route/' + routeId, method: 'delete' })
}

// ===== 点检任务 =====
export function listTask(query) {
  return request({ url: '/dms/inspection/task/list', method: 'get', params: query })
}
export function getTask(taskId) {
  return request({ url: '/dms/inspection/task/' + taskId, method: 'get' })
}
export function addTask(data) {
  return request({ url: '/dms/inspection/task', method: 'post', data: data })
}
export function updateTask(data) {
  return request({ url: '/dms/inspection/task', method: 'put', data: data })
}
export function delTask(taskId) {
  return request({ url: '/dms/inspection/task/' + taskId, method: 'delete' })
}
export function completeTask(data) {
  return request({ url: '/dms/inspection/task/complete', method: 'put', data: data })
}
export function startTask(taskId) {
  return request({ url: '/dms/inspection/task/start/' + taskId, method: 'put' })
}
