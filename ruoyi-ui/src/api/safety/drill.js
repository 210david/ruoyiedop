import request from '@/utils/request'

export function listDrill(query) {
  return request({ url: '/safety/emergency/drill/list', method: 'get', params: query })
}
export function getDrill(drillId) {
  return request({ url: '/safety/emergency/drill/' + drillId, method: 'get' })
}
export function addDrill(data) {
  return request({ url: '/safety/emergency/drill', method: 'post', data: data })
}
export function updateDrill(data) {
  return request({ url: '/safety/emergency/drill', method: 'put', data: data })
}
export function delDrill(drillId) {
  return request({ url: '/safety/emergency/drill/' + drillId, method: 'delete' })
}