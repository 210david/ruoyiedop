import request from '@/utils/request'

export function listEmergencyPlan(query) {
  return request({ url: '/safety/emergency/plan/list', method: 'get', params: query })
}
export function getEmergencyPlan(planId) {
  return request({ url: '/safety/emergency/plan/' + planId, method: 'get' })
}
export function addEmergencyPlan(data) {
  return request({ url: '/safety/emergency/plan', method: 'post', data: data })
}
export function updateEmergencyPlan(data) {
  return request({ url: '/safety/emergency/plan', method: 'put', data: data })
}
export function delEmergencyPlan(planId) {
  return request({ url: '/safety/emergency/plan/' + planId, method: 'delete' })
}