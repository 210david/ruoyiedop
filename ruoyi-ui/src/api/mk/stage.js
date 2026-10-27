import request from '@/utils/request'

// 查询销售阶段列表
export function listStage(query) {
  return request({ url: '/mk/stage/list', method: 'get', params: query })
}

// 查询销售阶段详细
export function getStage(stageId) {
  return request({ url: '/mk/stage/' + stageId, method: 'get' })
}

// 新增销售阶段
export function addStage(data) {
  return request({ url: '/mk/stage', method: 'post', data: data })
}

// 修改销售阶段
export function updateStage(data) {
  return request({ url: '/mk/stage', method: 'put', data: data })
}

// 删除销售阶段
export function delStage(stageId) {
  return request({ url: '/mk/stage/' + stageId, method: 'delete' })
}
