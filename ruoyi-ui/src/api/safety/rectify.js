import request from '@/utils/request'

export function listRectify(query) {
  return request({ url: '/safety/rectify/list', method: 'get', params: query })
}
export function getRectify(rectifyId) {
  return request({ url: '/safety/rectify/' + rectifyId, method: 'get' })
}
export function addRectify(data) {
  return request({ url: '/safety/rectify', method: 'post', data: data })
}
export function updateRectify(data) {
  return request({ url: '/safety/rectify', method: 'put', data: data })
}
export function delRectify(rectifyId) {
  return request({ url: '/safety/rectify/' + rectifyId, method: 'delete' })
}