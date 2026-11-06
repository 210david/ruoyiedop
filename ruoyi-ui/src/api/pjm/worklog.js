import request from '@/utils/request'

export function listWorklog(query) { return request({ url: '/pjm/worklog/list', method: 'get', params: query }) }
export function getWorklog(worklogId) { return request({ url: '/pjm/worklog/' + worklogId, method: 'get' }) }
export function addWorklog(data) { return request({ url: '/pjm/worklog', method: 'post', data }) }
export function updateWorklog(data) { return request({ url: '/pjm/worklog', method: 'put', data }) }
export function delWorklog(worklogIds) { return request({ url: '/pjm/worklog/' + worklogIds, method: 'delete' }) }
