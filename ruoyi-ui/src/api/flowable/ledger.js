import request, { download } from '@/utils/request'

// ===== 后台：台账定义 =====
export function listLedger(query) {
  return request({ url: '/flowable/ledger/list', method: 'get', params: query })
}

export function getLedger(ledgerId) {
  return request({ url: '/flowable/ledger/' + ledgerId, method: 'get' })
}

export function addLedger(data) {
  return request({ url: '/flowable/ledger', method: 'post', data: data })
}

export function updateLedger(data) {
  return request({ url: '/flowable/ledger', method: 'put', data: data })
}

export function delLedger(ledgerIds) {
  return request({ url: '/flowable/ledger/' + ledgerIds, method: 'delete' })
}

/** 表单可配置字段候选 */
export function ledgerFields(formId) {
  return request({ url: '/flowable/ledger/fields', method: 'get', params: { formId } })
}

// ===== 后台：授权配置 =====
export function listGrant(ledgerId) {
  return request({ url: '/flowable/ledger/grant/list', method: 'get', params: { ledgerId } })
}

export function addGrant(data) {
  return request({ url: '/flowable/ledger/grant', method: 'post', data: data })
}

export function updateGrant(data) {
  return request({ url: '/flowable/ledger/grant', method: 'put', data: data })
}

export function delGrant(grantIds) {
  return request({ url: '/flowable/ledger/grant/' + grantIds, method: 'delete' })
}

// ===== 前台：流程台账 =====
export function myLedgers() {
  return request({ url: '/flowable/ledger/mine', method: 'get' })
}

export function ledgerConfig(ledgerId) {
  return request({ url: '/flowable/ledger/' + ledgerId + '/config', method: 'get' })
}

export function ledgerData(ledgerId, params) {
  return request({ url: '/flowable/ledger/' + ledgerId + '/data', method: 'get', params })
}

/** 导出（xlsx） */
export function exportLedger(ledgerId, params) {
  return download('/flowable/ledger/' + ledgerId + '/export', params, '流程台账.xlsx')
}
