import request from '@/utils/request'

export function listLibrary(query) {
  return request({ url: '/safety/library/list', method: 'get', params: query })
}
export function getLibrary(libraryId) {
  return request({ url: '/safety/library/' + libraryId, method: 'get' })
}
export function addLibrary(data) {
  return request({ url: '/safety/library', method: 'post', data: data })
}
export function updateLibrary(data) {
  return request({ url: '/safety/library', method: 'put', data: data })
}
export function delLibrary(libraryId) {
  return request({ url: '/safety/library/' + libraryId, method: 'delete' })
}