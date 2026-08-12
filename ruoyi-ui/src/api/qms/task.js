import request from '@/utils/request'

export function listTask(query) {
  return request({
    url: '/qms/task/list',
    method: 'get',
    params: query
  })
}

export function getTask(taskId) {
  return request({
    url: '/qms/task/' + taskId,
    method: 'get'
  })
}

export function getTaskReport(taskId) {
  return request({
    url: '/qms/task/report/' + taskId,
    method: 'get'
  })
}

export function addTask(data) {
  return request({
    url: '/qms/task',
    method: 'post',
    data: data
  })
}

export function updateTask(data) {
  return request({
    url: '/qms/task',
    method: 'put',
    data: data
  })
}

export function delTask(taskId) {
  return request({
    url: '/qms/task/' + taskId,
    method: 'delete'
  })
}

export function saveInspectResult(data) {
  return request({
    url: '/qms/task/saveResult',
    method: 'put',
    data: data
  })
}

export function createRecheckTask(taskId) {
  return request({
    url: '/qms/task/recheck/' + taskId,
    method: 'post'
  })
}
