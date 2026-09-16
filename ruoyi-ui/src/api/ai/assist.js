import request from '@/utils/request'

// ===== DMS：完工报告草稿 =====
export function aiCompleteReport(orderId) {
  return request({ url: '/dms/ai/completeReport/' + orderId, method: 'get', timeout: 60000 })
}

// ===== QMS =====
// NCR处置建议
export function aiNcrAdvice(ncrId) {
  return request({ url: '/qms/ai/ncrAdvice/' + ncrId, method: 'get', timeout: 60000 })
}
// 质量报告初稿
export function aiReportDraft(data) {
  return request({ url: '/qms/ai/reportDraft', method: 'post', data: data, timeout: 60000 })
}

// ===== MMS =====
// 生产异常诊断建议
export function aiAbnormalAdvice(abnormalId) {
  return request({ url: '/mms/ai/abnormalAdvice/' + abnormalId, method: 'get', timeout: 60000 })
}
// 生产日报生成
export function aiDailyReport(statId) {
  return request({ url: '/mms/ai/dailyReport/' + statId, method: 'get', timeout: 60000 })
}

// ===== PMS =====
// 采购申请AI建议
export function aiRequestAdvice(requestId) {
  return request({ url: '/pms/ai/requestAdvice/' + requestId, method: 'get', timeout: 60000 })
}
// 比价分析摘要
export function aiPriceSummary(inquiryId) {
  return request({ url: '/pms/ai/priceSummary/' + inquiryId, method: 'get', timeout: 60000 })
}
