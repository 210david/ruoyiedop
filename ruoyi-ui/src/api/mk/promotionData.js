import request from '@/utils/request'

// 查询推广数据列表
export function listPromotionData(query, config = {}) {
  return request({ url: '/mk/promotionData/list', method: 'get', params: query, ...config })
}

// 查询推广数据详细
export function getPromotionData(dataId) {
  return request({ url: '/mk/promotionData/' + dataId, method: 'get' })
}

// 新增推广数据（按 内容包+日期 upsert）
export function addPromotionData(data) {
  return request({ url: '/mk/promotionData', method: 'post', data: data })
}

// 修改推广数据（按 内容包+日期 upsert）
export function updatePromotionData(data) {
  return request({ url: '/mk/promotionData', method: 'put', data: data })
}

// 删除推广数据
export function delPromotionData(dataId) {
  return request({ url: '/mk/promotionData/' + dataId, method: 'delete' })
}

// 下载导入模板
export function importTemplate() {
  return request({ url: '/mk/promotionData/importTemplate', method: 'post', responseType: 'blob' })
}
