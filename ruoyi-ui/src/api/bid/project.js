import request from '@/utils/request'

export function listProject(query) {
  return request({ url: '/bid/project/list', method: 'get', params: query })
}
export function getProject(projectId) {
  return request({ url: '/bid/project/' + projectId, method: 'get' })
}
export function updateProject(data) {
  return request({ url: '/bid/project', method: 'put', data })
}
export function updateNode(nodeId, data) {
  return request({ url: '/bid/project/node/' + nodeId, method: 'put', data })
}
export function delProject(projectIds) {
  return request({ url: '/bid/project/' + projectIds, method: 'delete' })
}
export function changeProjectStatus(projectId, status, reason) {
  return request({ url: '/bid/project/changeStatus/' + projectId, method: 'put', params: { status, reason } })
}
export function finishNode(nodeId, blockDesc) {
  return request({ url: '/bid/project/finishNode/' + nodeId, method: 'put', params: { blockDesc } })
}
export function toPms(projectId) {
  return request({ url: '/bid/project/toPms/' + projectId, method: 'post' })
}
