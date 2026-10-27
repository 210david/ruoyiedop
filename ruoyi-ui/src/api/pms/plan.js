import request from '@/utils/request'

// 查询采购计划列表
export function listPlan(query) {
  return request({
    url: '/pms/plan/list',
    method: 'get',
    params: query
  })
}

// 查询采购计划详细
export function getPlan(planId) {
  return request({
    url: '/pms/plan/' + planId,
    method: 'get'
  })
}

// 新增采购计划
export function addPlan(data) {
  return request({
    url: '/pms/plan',
    method: 'post',
    data: data
  })
}

// 修改采购计划
export function updatePlan(data) {
  return request({
    url: '/pms/plan',
    method: 'put',
    data: data
  })
}

// 删除采购计划
export function delPlan(planId) {
  return request({
    url: '/pms/plan/' + planId,
    method: 'delete'
  })
}

// 审核采购计划
export function auditPlan(planId, status, auditOpinion) {
  return request({
    url: '/pms/plan/audit/' + planId,
    method: 'put',
    params: { status, auditOpinion }
  })
}

// 关闭采购计划
export function closePlan(planId) {
  return request({
    url: '/pms/plan/close/' + planId,
    method: 'put'
  })
}

// 导出采购计划
export function exportPlan(query) {
  return request({
    url: '/pms/plan/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
