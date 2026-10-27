import request from '@/utils/request'

export function listWorker(query) {
  return request({ url: '/safety/worker/list', method: 'get', params: query })
}
export function getWorker(workerId) {
  return request({ url: '/safety/worker/' + workerId, method: 'get' })
}
export function addWorker(data) {
  return request({ url: '/safety/worker', method: 'post', data: data })
}
export function updateWorker(data) {
  return request({ url: '/safety/worker', method: 'put', data: data })
}
export function delWorker(workerId) {
  return request({ url: '/safety/worker/' + workerId, method: 'delete' })
}