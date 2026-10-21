import request from '@/utils/request'

// 检查表模板列表
export function listCheckTemplates(query) {
  return request({ url: '/qms/auditCheck/templates', method: 'get', params: query })
}

// 按模板生成检查表（覆盖式）
export function generateCheckItems(auditPlanId, templateId) {
  return request({ url: `/qms/auditCheck/generate/${auditPlanId}/${templateId}`, method: 'post' })
}

// 查询计划的检查项
export function getCheckItems(auditPlanId) {
  return request({ url: `/qms/auditCheck/items/${auditPlanId}`, method: 'get' })
}

// 保存检查项（批量）
export function saveCheckItems(auditPlanId, data) {
  return request({ url: `/qms/auditCheck/items/${auditPlanId}`, method: 'put', data: data })
}
