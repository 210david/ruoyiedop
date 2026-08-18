import request from '@/utils/request'

export function listCalendar(query) {
  return request({ url: '/mms/calendar/list', method: 'get', params: query })
}
export function getCalendar(calendarId) {
  return request({ url: '/mms/calendar/' + calendarId, method: 'get' })
}
export function addCalendar(data) {
  return request({ url: '/mms/calendar', method: 'post', data })
}
export function updateCalendar(data) {
  return request({ url: '/mms/calendar', method: 'put', data })
}
export function delCalendar(calendarId) {
  return request({ url: '/mms/calendar/' + calendarId, method: 'delete' })
}
