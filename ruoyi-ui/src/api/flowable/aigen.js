import request from '@/utils/request'

/**
 * AI流程生成 API
 *
 * 上传流程开发方案 → AI解析为结构化流程/表单 → 人工确认 → 生成表单与流程模型草稿（不部署）
 */

// 任务列表
export function listAiGenTask(query) {
  return request({ url: '/flowable/aigen/list', method: 'get', params: query })
}

// 任务详情（含结构化解析结果）
export function getAiGenTask(taskId) {
  return request({ url: '/flowable/aigen/' + taskId, method: 'get' })
}

// 上传方案并解析（AI耗时较长，放宽超时）
export function analyzePlan(formData) {
  return request({
    url: '/flowable/aigen/analyze',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 180000
  })
}

// 基于已保存原文重新解析（remark 为空则沿用任务已保存的备注）
export function reanalyzeTask(taskId, remark) {
  return request({
    url: '/flowable/aigen/reanalyze/' + taskId,
    method: 'post',
    params: { remark },
    timeout: 180000
  })
}

// 按确认结果生成表单与流程模型草稿
export function generateFlow(data) {
  return request({
    url: '/flowable/aigen/generate',
    method: 'post',
    data: data,
    timeout: 180000
  })
}

// 删除任务
export function delAiGenTask(taskIds) {
  return request({ url: '/flowable/aigen/' + taskIds, method: 'delete' })
}
