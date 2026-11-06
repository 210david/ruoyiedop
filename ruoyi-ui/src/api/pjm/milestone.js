import request from '@/utils/request'

// 查询里程碑列表
export function listMilestone(query) {
  return request({ url: '/pjm/milestone/list', method: 'get', params: query })
}
// 查询里程碑详细
export function getMilestone(milestoneId) {
  return request({ url: '/pjm/milestone/' + milestoneId, method: 'get' })
}
// 新增里程碑
export function addMilestone(data) {
  return request({ url: '/pjm/milestone', method: 'post', data: data })
}
// 修改里程碑
export function updateMilestone(data) {
  return request({ url: '/pjm/milestone', method: 'put', data: data })
}
// 删除里程碑
export function delMilestone(milestoneIds) {
  return request({ url: '/pjm/milestone/' + milestoneIds, method: 'delete' })
}
