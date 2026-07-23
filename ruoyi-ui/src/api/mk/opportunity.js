import request from '@/utils/request'

// 查询商机列表
export function listOpportunity(query) {
  return request({ url: '/mk/opportunity/list', method: 'get', params: query })
}

// 查询商机详细
export function getOpportunity(opportunityId) {
  return request({ url: '/mk/opportunity/' + opportunityId, method: 'get' })
}

// 新增商机
export function addOpportunity(data) {
  return request({ url: '/mk/opportunity', method: 'post', data: data })
}

// 修改商机
export function updateOpportunity(data) {
  return request({ url: '/mk/opportunity', method: 'put', data: data })
}

// 删除商机
export function delOpportunity(opportunityId) {
  return request({ url: '/mk/opportunity/' + opportunityId, method: 'delete' })
}

// 看板视图
export function kanbanOpportunity() {
  return request({ url: '/mk/opportunity/kanban', method: 'get' })
}

// 阶段推进
export function advanceOpportunity(opportunityId, data) {
  return request({ url: '/mk/opportunity/advance/' + opportunityId, method: 'put', data: data })
}

// 阶段退回
export function retreatOpportunity(opportunityId, data) {
  return request({ url: '/mk/opportunity/retreat/' + opportunityId, method: 'put', data: data })
}

// 赢单关单
export function winOpportunity(opportunityId, data) {
  return request({ url: '/mk/opportunity/win/' + opportunityId, method: 'put', data: data })
}

// 输单关单
export function loseOpportunity(opportunityId, data) {
  return request({ url: '/mk/opportunity/lose/' + opportunityId, method: 'put', data: data })
}

// 重新打开
export function reopenOpportunity(opportunityId) {
  return request({ url: '/mk/opportunity/reopen/' + opportunityId, method: 'put' })
}

// 查询阶段变更日志
export function getStageLog(opportunityId) {
  return request({ url: '/mk/opportunity/stageLog/' + opportunityId, method: 'get' })
}

// 查询商机关联数据
export function getOpportunityRelations(opportunityId) {
  return request({ url: '/mk/opportunity/relations/' + opportunityId, method: 'get' })
}

// 查询停滞商机
export function listStagnant(query) {
  return request({ url: '/mk/opportunity/stagnant', method: 'get', params: query })
}
