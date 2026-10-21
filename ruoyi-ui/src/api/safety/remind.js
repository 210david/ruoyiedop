import request from '@/utils/request'

export function listRemind(query) {
  return request({ url: '/safety/remind/list', method: 'get', params: query })
}
export function getRemind(remindId) {
  return request({ url: '/safety/remind/' + remindId, method: 'get' })
}
export function handleRemind(data) {
  return request({ url: '/safety/remind/handle', method: 'put', data: data })
}
export function delRemind(remindId) {
  return request({ url: '/safety/remind/' + remindId, method: 'delete' })
}