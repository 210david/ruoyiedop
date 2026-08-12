import request from '@/utils/request'

export function listTrainingAttendee(query) {
  return request({ url: '/safety/training/attendee/list', method: 'get', params: query })
}
export function listHoursStatistics(query) {
  return request({ url: '/safety/training/attendee/hours', method: 'get', params: query })
}
export function getTrainingAttendee(attendeeId) {
  return request({ url: '/safety/training/attendee/' + attendeeId, method: 'get' })
}
export function addTrainingAttendee(data) {
  return request({ url: '/safety/training/attendee', method: 'post', data: data })
}
export function batchAddTrainingAttendee(data) {
  return request({ url: '/safety/training/attendee/batch', method: 'post', data: data })
}
export function updateTrainingAttendee(data) {
  return request({ url: '/safety/training/attendee', method: 'put', data: data })
}
export function delTrainingAttendee(attendeeId) {
  return request({ url: '/safety/training/attendee/' + attendeeId, method: 'delete' })
}
export function importTrainingAttendee(file, updateSupport) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('updateSupport', updateSupport)
  return request({ url: '/safety/training/attendee/importData', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

/** 批量维护考试成绩 */
export function batchUpdateExamScore(data) {
  return request({ url: '/safety/training/attendee/batchExamScore', method: 'post', data: data })
}
