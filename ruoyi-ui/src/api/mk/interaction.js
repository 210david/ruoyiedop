import request from '@/utils/request'

// 查询互动记录列表
export function listInteraction(query) {
  return request({ url: '/mk/interaction/list', method: 'get', params: query })
}

// 查询互动记录详细
export function getInteraction(recordId) {
  return request({ url: '/mk/interaction/' + recordId, method: 'get' })
}

// 新增互动记录
export function addInteraction(data) {
  return request({ url: '/mk/interaction', method: 'post', data: data })
}

// 修改互动记录
export function updateInteraction(data) {
  return request({ url: '/mk/interaction', method: 'put', data: data })
}

// 删除互动记录
export function delInteraction(recordId) {
  return request({ url: '/mk/interaction/' + recordId, method: 'delete' })
}
