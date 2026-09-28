import request from '@/utils/request'

// 查询归因链接列表（params: activityId/channelId/channelCode）
export function listChannelLink(query) {
  return request({ url: '/mk/channelLink/list', method: 'get', params: query })
}

// 批量生成归因链接（data: { activityId, channelIds: [] }）
export function generateLinks(data) {
  return request({ url: '/mk/channelLink/generate', method: 'post', data: data })
}

// 删除归因链接
export function delChannelLink(linkIds) {
  return request({ url: '/mk/channelLink/' + linkIds, method: 'delete' })
}
