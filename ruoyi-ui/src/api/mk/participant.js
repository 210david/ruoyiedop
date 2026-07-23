import request from '@/utils/request'

// 查询活动参与人列表
export function listParticipant(query) {
  return request({ url: '/mk/participant/list', method: 'get', params: query })
}

// 查询活动参与人详细
export function getParticipant(participantId) {
  return request({ url: '/mk/participant/' + participantId, method: 'get' })
}

// 新增活动参与人
export function addParticipant(data) {
  return request({ url: '/mk/participant', method: 'post', data: data })
}

// 修改活动参与人
export function updateParticipant(data) {
  return request({ url: '/mk/participant', method: 'put', data: data })
}

// 删除活动参与人
export function delParticipant(participantId) {
  return request({ url: '/mk/participant/' + participantId, method: 'delete' })
}

// ====== P0-3: 按活动ID查询参与人 ======
export function getParticipantsByActivity(activityId) {
  return request({ url: '/mk/participant/byActivity/' + activityId, method: 'get' })
}

// ====== P1-5: 签到 & 转线索 ======
export function signInParticipant(participantId) {
  return request({ url: '/mk/participant/signIn/' + participantId, method: 'put' })
}

export function convertToLead(participantId) {
  return request({ url: '/mk/participant/convertToLead/' + participantId, method: 'post' })
}
