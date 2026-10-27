import request from '@/utils/request'

// ================= CCP控制点模板 =================

// 查询CCP模板列表
export function listCcpTemplate(query) {
  return request({ url: '/food/ccp/template/list', method: 'get', params: query })
}

// 新增CCP模板
export function addCcpTemplate(data) {
  return request({ url: '/food/ccp/template', method: 'post', data: data })
}

// 修改CCP模板
export function updateCcpTemplate(data) {
  return request({ url: '/food/ccp/template', method: 'put', data: data })
}

// 删除CCP模板（软删除）
export function delCcpTemplate(templateIds) {
  return request({ url: '/food/ccp/template/' + templateIds, method: 'delete' })
}

// ================= CCP监控记录 =================

// 查询CCP监控记录列表
export function listCcpRecord(query) {
  return request({ url: '/food/ccp/list', method: 'get', params: query })
}

// CCP监控记录详情
export function getCcpRecord(recordId) {
  return request({ url: '/food/ccp/' + recordId, method: 'get' })
}

// 录入CCP监控记录（超限自动联动NCR+批次冻结）
export function addCcpRecord(data) {
  return request({ url: '/food/ccp', method: 'post', data: data })
}

// 补充记录（仅偏差说明/纠正措施）
export function updateCcpRecord(data) {
  return request({ url: '/food/ccp', method: 'put', data: data })
}

// 台账汇总（记录数/合格/超限）
export function ccpStats(query) {
  return request({ url: '/food/ccp/stats', method: 'get', params: query })
}
