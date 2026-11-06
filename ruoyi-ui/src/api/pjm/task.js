import request from '@/utils/request'

// WBS 任务 API
export function listTask(query) {
  return request({ url: '/pjm/task/list', method: 'get', params: query })
}
export function getTask(taskId) {
  return request({ url: '/pjm/task/' + taskId, method: 'get' })
}
export function getTaskTree(projectId) {
  return request({ url: '/pjm/task/tree/' + projectId, method: 'get' })
}
export function addTask(data) {
  return request({ url: '/pjm/task', method: 'post', data: data })
}
export function updateTask(data) {
  return request({ url: '/pjm/task', method: 'put', data: data })
}
export function delTask(taskIds) {
  return request({ url: '/pjm/task/' + taskIds, method: 'delete' })
}
