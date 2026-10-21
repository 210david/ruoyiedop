import request from '@/utils/request'

export function listTask(query) {
  return request({ url: '/safety/task/list', method: 'get', params: query })
}
export function getTask(taskId) {
  return request({ url: '/safety/task/' + taskId, method: 'get' })
}
export function addTask(data) {
  return request({ url: '/safety/task', method: 'post', data: data })
}
export function updateTask(data) {
  return request({ url: '/safety/task', method: 'put', data: data })
}
export function delTask(taskId) {
  return request({ url: '/safety/task/' + taskId, method: 'delete' })
}
export function submitFeedback(data) {
  return request({ url: '/safety/task/feedback', method: 'put', data: data })
}
export function cancelTask(taskId) {
  return request({ url: '/safety/task/cancel/' + taskId, method: 'put' })
}