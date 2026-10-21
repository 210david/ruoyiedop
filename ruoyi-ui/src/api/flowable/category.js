import request from '@/utils/request'

// ===== 流程分类管理 =====
export function listCategory(query) {
  return request({ url: '/flowable/category/list', method: 'get', params: query })
}

export function getCategory(dictCode) {
  return request({ url: '/flowable/category/' + dictCode, method: 'get' })
}

export function addCategory(data) {
  return request({ url: '/flowable/category', method: 'post', data: data })
}

export function updateCategory(data) {
  return request({ url: '/flowable/category', method: 'put', data: data })
}

export function delCategory(dictCodes) {
  return request({ url: '/flowable/category/' + dictCodes, method: 'delete' })
}
