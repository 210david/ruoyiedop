import request from '@/utils/request'

export function listNotice(query) {
  return request({ url: '/bid/notice/list', method: 'get', params: query })
}
export function getNotice(noticeId) {
  return request({ url: '/bid/notice/' + noticeId, method: 'get' })
}
export function addNotice(data) {
  return request({ url: '/bid/notice', method: 'post', data })
}
export function updateNotice(data) {
  return request({ url: '/bid/notice', method: 'put', data })
}
export function delNotice(noticeIds) {
  return request({ url: '/bid/notice/' + noticeIds, method: 'delete' })
}
export function getArchive(projectKey) {
  return request({ url: '/bid/notice/archive/' + encodeURIComponent(projectKey), method: 'get' })
}
export function aiExtractNotice(noticeId) {
  return request({ url: '/bid/notice/aiExtract/' + noticeId, method: 'post' })
}
export function toProject(noticeId) {
  return request({ url: '/bid/notice/toProject/' + noticeId, method: 'post' })
}
