import request from '@/utils/request'

// 查询客户标签列表
export function listTag(query) {
  return request({ url: '/mk/tag/list', method: 'get', params: query })
}

// 查询客户标签详细
export function getTag(tagId) {
  return request({ url: '/mk/tag/' + tagId, method: 'get' })
}

// 新增客户标签
export function addTag(data) {
  return request({ url: '/mk/tag', method: 'post', data: data })
}

// 修改客户标签
export function updateTag(data) {
  return request({ url: '/mk/tag', method: 'put', data: data })
}

// 删除客户标签
export function delTag(tagId) {
  return request({ url: '/mk/tag/' + tagId, method: 'delete' })
}
