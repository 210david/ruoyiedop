import request from '@/utils/request'

// ==================== 财务配置（结账 + 凭证规则） ====================

// 查询期间列表
export function listPeriods() {
  return request({
    url: '/fin/config/periods',
    method: 'get'
  })
}

// 结账前置检查
export function closingCheck(yearMonth) {
  return request({
    url: '/fin/config/closing/check',
    method: 'get',
    params: { yearMonth }
  })
}

// 执行结账
export function closePeriod(yearMonth) {
  return request({
    url: '/fin/config/closing/close',
    method: 'post',
    params: { yearMonth }
  })
}

// 反结账
export function unlockPeriod(yearMonth, reason) {
  return request({
    url: '/fin/config/closing/unlock',
    method: 'post',
    params: { yearMonth, reason }
  })
}

// 结账检查记录
export function closingRecords(yearMonth) {
  return request({
    url: '/fin/config/closing/records',
    method: 'get',
    params: { yearMonth }
  })
}

// 查询凭证规则
export function listVoucherRules() {
  return request({
    url: '/fin/config/voucherRules',
    method: 'get'
  })
}

// 修改凭证规则
export function updateVoucherRule(data) {
  return request({
    url: '/fin/config/voucherRule',
    method: 'put',
    data: data
  })
}

// 单张生成凭证
export function generateVoucher(sourceModule, sourceType, sourceId) {
  return request({
    url: '/fin/config/voucherRule/generate',
    method: 'post',
    params: { sourceModule, sourceType, sourceId }
  })
}

// 批量生成凭证
export function generateBatch(sourceModule, sourceType, limit = 50) {
  return request({
    url: '/fin/config/voucherRule/generateBatch',
    method: 'post',
    params: { sourceModule, sourceType, limit }
  })
}