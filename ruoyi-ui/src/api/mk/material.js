import request from '@/utils/request'

// 查询素材列表
export function listMaterial(query, config = {}) {
  return request({ url: '/mk/material/list', method: 'get', params: query, ...config })
}

// 查询素材详细
export function getMaterial(materialId) {
  return request({ url: '/mk/material/' + materialId, method: 'get' })
}

// 新增素材
export function addMaterial(data) {
  return request({ url: '/mk/material', method: 'post', data: data })
}

// 修改素材
export function updateMaterial(data) {
  return request({ url: '/mk/material', method: 'put', data: data })
}

// 删除素材
export function delMaterial(materialId) {
  return request({ url: '/mk/material/' + materialId, method: 'delete' })
}

// 导出素材
export function exportMaterial(query) {
  return request({ url: '/mk/material/export', method: 'post', data: query, responseType: 'blob' })
}
