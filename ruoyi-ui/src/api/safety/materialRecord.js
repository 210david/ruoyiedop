import request from '@/utils/request'

export function listMaterialRecord(query) {
  return request({ url: '/safety/materialRecord/list', method: 'get', params: query })
}
export function getMaterialRecord(recordId) {
  return request({ url: '/safety/materialRecord/' + recordId, method: 'get' })
}
export function addMaterialRecord(data) {
  return request({ url: '/safety/materialRecord', method: 'post', data: data })
}
export function updateMaterialRecord(data) {
  return request({ url: '/safety/materialRecord', method: 'put', data: data })
}
export function delMaterialRecord(recordId) {
  return request({ url: '/safety/materialRecord/' + recordId, method: 'delete' })
}
