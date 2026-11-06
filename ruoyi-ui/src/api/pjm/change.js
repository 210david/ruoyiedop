import request from '@/utils/request'

export function listChange(query) { return request({ url: '/pjm/change/list', method: 'get', params: query }) }
export function getChange(changeId) { return request({ url: '/pjm/change/' + changeId, method: 'get' }) }
export function addChange(data) { return request({ url: '/pjm/change', method: 'post', data }) }
export function updateChange(data) { return request({ url: '/pjm/change', method: 'put', data }) }
export function delChange(changeIds) { return request({ url: '/pjm/change/' + changeIds, method: 'delete' }) }
export function submitChange(changeId) { return request({ url: '/pjm/change/submit/' + changeId, method: 'put' }) }
export function auditChange(data) { return request({ url: '/pjm/change/audit', method: 'put', data }) }
