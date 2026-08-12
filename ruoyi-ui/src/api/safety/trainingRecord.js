import request from '@/utils/request'

export function listTrainingRecord(query) {
  return request({ url: '/safety/training/record/list', method: 'get', params: query })
}
export function getTrainingRecord(recordId) {
  return request({ url: '/safety/training/record/' + recordId, method: 'get' })
}
export function addTrainingRecord(data) {
  return request({ url: '/safety/training/record', method: 'post', data: data })
}
export function updateTrainingRecord(data) {
  return request({ url: '/safety/training/record', method: 'put', data: data })
}
export function delTrainingRecord(recordId) {
  return request({ url: '/safety/training/record/' + recordId, method: 'delete' })
}