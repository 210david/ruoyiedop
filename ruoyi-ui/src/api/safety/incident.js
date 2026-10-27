import request from '@/utils/request'

export function listIncident(query) {
  return request({ url: '/safety/emergency/incident/list', method: 'get', params: query })
}
export function getIncident(incidentId) {
  return request({ url: '/safety/emergency/incident/' + incidentId, method: 'get' })
}
export function addIncident(data) {
  return request({ url: '/safety/emergency/incident', method: 'post', data: data })
}
export function updateIncident(data) {
  return request({ url: '/safety/emergency/incident', method: 'put', data: data })
}
export function delIncident(incidentId) {
  return request({ url: '/safety/emergency/incident/' + incidentId, method: 'delete' })
}