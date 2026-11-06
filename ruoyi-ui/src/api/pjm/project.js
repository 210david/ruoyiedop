import request from '@/utils/request'

// 查询项目立项列表
export function listProject(query) {
  return request({ url: '/pjm/project/list', method: 'get', params: query })
}
// 查询项目立项详细
export function getProject(projectId) {
  return request({ url: '/pjm/project/' + projectId, method: 'get' })
}
// 新增项目立项
export function addProject(data) {
  return request({ url: '/pjm/project', method: 'post', data: data })
}
// 修改项目立项
export function updateProject(data) {
  return request({ url: '/pjm/project', method: 'put', data: data })
}
// 删除项目立项
export function delProject(projectId) {
  return request({ url: '/pjm/project/' + projectId, method: 'delete' })
}
// 导出
export function exportProject(query) {
  return request({ url: '/pjm/project/export', method: 'post', params: query, responseType: 'blob' })
}
// 提交审批（草稿/驳回 → 待审批）
export function submitProject(projectId) {
  return request({ url: '/pjm/project/submit/' + projectId, method: 'put' })
}
// 审批
export function auditProject(projectId, status, auditOpinion) {
  return request({
    url: '/pjm/project/audit/' + projectId,
    method: 'put',
    params: { status, auditOpinion }
  })
}
// 刷新采购汇总
export function refreshPurchaseSummary(projectId) {
  return request({ url: '/pjm/project/refresh/' + projectId, method: 'put' })
}
// ★ 项目采购汇总（读接口）
export function getPurchaseSummary(projectId) {
  return request({ url: '/pjm/project/summary/' + projectId, method: 'get' })
}
// ★ 从项目/任务发起采购
export function buildPurchase(data) {
  return request({ url: '/pjm/project/purchase', method: 'post', data: data })
}
// ★ 项目采购-库存检查（按物料批量返回各仓库可用库存）
export function stockCheckProjectPurchase(data) {
  return request({ url: '/pjm/project/purchase/stockCheck', method: 'post', data: data })
}
// ★ 项目采购-执行（有库存走出库单，无库存走采购申请）
export function executeProjectPurchase(data) {
  return request({ url: '/pjm/project/purchase/execute', method: 'post', data: data })
}
// 项目结项归档
export function closeProject(projectId) {
  return request({ url: '/pjm/project/close/' + projectId, method: 'put' })
}

// ★ AI 解析附件（multipart/form-data，支持文件或文本）
export function aiParseProject(formData) {
  return request({
    url: '/pjm/project/ai/parse',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
