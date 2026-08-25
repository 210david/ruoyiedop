import request from '@/utils/request'

// 查询消息列表
export function listMessage(query) {
  return request({
    url: '/system/message/list',
    method: 'get',
    params: query
  })
}

// 查询消息详细
export function getMessage(messageId) {
  return request({
    url: '/system/message/' + messageId,
    method: 'get'
  })
}

// 新增消息
export function addMessage(data) {
  return request({
    url: '/system/message',
    method: 'post',
    data: data
  })
}

// 首页顶部消息列表（带已读状态）
export function listMessageTop() {
  return request({
    url: '/system/message/listTop',
    method: 'get'
  })
}

// 标记消息已读
export function markMessageRead(messageId) {
  return request({
    url: '/system/message/markRead',
    method: 'post',
    params: { messageId }
  })
}

// 批量标记已读
export function markMessageReadAll(ids) {
  return request({
    url: '/system/message/markReadAll',
    method: 'post',
    params: { ids }
  })
}
