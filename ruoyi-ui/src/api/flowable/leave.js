import request from '@/utils/request'

export function listLeave(query) {
  return request({ url: '/flowable/leave/list', method: 'get', params: query })
}

export function getLeave(leaveId) {
  return request({ url: '/flowable/leave/' + leaveId, method: 'get' })
}

export function addLeave(data) {
  return request({ url: '/flowable/leave', method: 'post', data: data })
}

export function updateLeave(data) {
  return request({ url: '/flowable/leave', method: 'put', data: data })
}

export function delLeave(leaveIds) {
  return request({ url: '/flowable/leave/' + leaveIds, method: 'delete' })
}

export function submitLeave(leaveId, approverUserId) {
  return request({ url: '/flowable/leave/submit/' + leaveId, method: 'post', params: { approverUserId } })
}
