import request from '@/utils/request'

export function listTrainingCourse(query) {
  return request({ url: '/safety/training/course/list', method: 'get', params: query })
}
export function getTrainingCourse(courseId) {
  return request({ url: '/safety/training/course/' + courseId, method: 'get' })
}
export function addTrainingCourse(data) {
  return request({ url: '/safety/training/course', method: 'post', data: data })
}
export function updateTrainingCourse(data) {
  return request({ url: '/safety/training/course', method: 'put', data: data })
}
export function delTrainingCourse(courseId) {
  return request({ url: '/safety/training/course/' + courseId, method: 'delete' })
}
