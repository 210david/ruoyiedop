import request from '@/utils/request'

export function listComplaint(query) {
  return request({ url: '/qms/complaint/list', method: 'get', params: query })
}
export function getComplaint(complaintId) {
  return request({ url: '/qms/complaint/' + complaintId, method: 'get' })
}
export function addComplaint(data) {
  return request({ url: '/qms/complaint', method: 'post', data: data })
}
export function updateComplaint(data) {
  return request({ url: '/qms/complaint', method: 'put', data: data })
}
export function delComplaint(complaintIds) {
  return request({ url: '/qms/complaint/' + complaintIds, method: 'delete' })
}
export function closeComplaint(complaintId) {
  return request({ url: '/qms/complaint/close/' + complaintId, method: 'put' })
}
// 已登记(0) → 处理中(1)
export function acceptComplaint(complaintId) {
  return request({ url: '/qms/complaint/accept/' + complaintId, method: 'put' })
}
// 处理中(1) → 待确认(2)，同时提交处理描述、处理结果等信息
export function completeComplaint(data) {
  return request({ url: '/qms/complaint/complete', method: 'put', data: data })
}
