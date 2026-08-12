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
