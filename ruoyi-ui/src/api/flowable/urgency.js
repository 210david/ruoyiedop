import request from '@/utils/request'

/**
 * 紧急程度配置 API
 *
 * 管理 flow_urgency_level 配置表（系统内置字段 urgency 的可选档位）。
 * 支持自定义名称/排序/封存，替代原先硬编码的三档。
 */

// 可选档位（草稿：发起页下拉使用，无需权限）
export function urgencyOptions() {
  return request({ url: '/flowable/urgency/options', method: 'get' })
}

// 配置管理：列表
export function listUrgencyLevel(query) {
  return request({ url: '/flowable/urgency/list', method: 'get', params: query })
}

// 配置管理：详情
export function getUrgencyLevel(levelId) {
  return request({ url: '/flowable/urgency/' + levelId, method: 'get' })
}

// 配置管理：新增
export function addUrgencyLevel(data) {
  return request({ url: '/flowable/urgency', method: 'post', data: data })
}

// 配置管理：修改
export function updateUrgencyLevel(data) {
  return request({ url: '/flowable/urgency', method: 'put', data: data })
}

// 配置管理：删除
export function delUrgencyLevel(levelIds) {
  return request({ url: '/flowable/urgency/' + levelIds, method: 'delete' })
}
