import request from '@/utils/request'

export function listArea(query) {
  return request({ url: '/safety/area/list', method: 'get', params: query })
}
export function getArea(areaId) {
  return request({ url: '/safety/area/' + areaId, method: 'get' })
}
export function addArea(data) {
  return request({ url: '/safety/area', method: 'post', data: data })
}
export function updateArea(data) {
  return request({ url: '/safety/area', method: 'put', data: data })
}
export function delArea(areaId) {
  return request({ url: '/safety/area/' + areaId, method: 'delete' })
}