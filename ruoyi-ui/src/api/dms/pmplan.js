import request from '@/utils/request'

export function listPmplan(query) {
  return request({ url: '/dms/pmplan/list', method: 'get', params: query })
}

export function getPmplan(planId) {
  return request({ url: '/dms/pmplan/' + planId, method: 'get' })
}

export function addPmplan(data) {
  return request({ url: '/dms/pmplan', method: 'post', data: data })
}

export function updatePmplan(data) {
  return request({ url: '/dms/pmplan', method: 'put', data: data })
}

export function delPmplan(planId) {
  return request({ url: '/dms/pmplan/' + planId, method: 'delete' })
}

export function generateWorkOrder(planId) {
  return request({ url: '/dms/pmplan/generate/' + planId, method: 'post' })
}

export function autoGeneratePm() {
  return request({ url: '/dms/pmplan/autoGenerate', method: 'post' })
}

export function getPmplanCalendar(month) {
  return request({ url: '/dms/pmplan/calendar', method: 'get', params: { month } })
}
