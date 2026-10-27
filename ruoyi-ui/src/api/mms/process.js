import request from '@/utils/request'

export function listProcess(query) {
  return request({
    url: '/mms/process/list',
    method: 'get',
    params: query
  })
}

export function getProcess(processId) {
  return request({
    url: '/mms/process/' + processId,
    method: 'get'
  })
}

export function addProcess(data) {
  return request({
    url: '/mms/process',
    method: 'post',
    data: data
  })
}

export function updateProcess(data) {
  return request({
    url: '/mms/process',
    method: 'put',
    data: data
  })
}

export function delProcess(processId) {
  return request({
    url: '/mms/process/' + processId,
    method: 'delete'
  })
}
