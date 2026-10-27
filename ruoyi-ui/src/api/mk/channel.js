import request from '@/utils/request'

// 查询渠道账号列表
export function listChannel(query, config = {}) {
  return request({ url: '/mk/channel/list', method: 'get', params: query, ...config })
}

// 查询全部正常渠道（选择器用，不分页）
export function listAllChannel() {
  return request({ url: '/mk/channel/all', method: 'get' })
}

// 查询渠道账号详细
export function getChannel(channelId) {
  return request({ url: '/mk/channel/' + channelId, method: 'get' })
}

// 新增渠道账号
export function addChannel(data) {
  return request({ url: '/mk/channel', method: 'post', data: data })
}

// 修改渠道账号
export function updateChannel(data) {
  return request({ url: '/mk/channel', method: 'put', data: data })
}

// 删除渠道账号
export function delChannel(channelId) {
  return request({ url: '/mk/channel/' + channelId, method: 'delete' })
}

// 导出渠道账号
export function exportChannel(query) {
  return request({ url: '/mk/channel/export', method: 'post', data: query, responseType: 'blob' })
}
