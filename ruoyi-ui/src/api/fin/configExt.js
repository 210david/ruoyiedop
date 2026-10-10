import request from '@/utils/request'

// ==================== 财务参数配置 ====================

export function listFiscal(query) {
  return request({ url: '/fin/config/fiscal/list', method: 'get', params: query })
}

export function updateFiscal(configKey, configValue) {
  return request({
    url: '/fin/config/fiscal/' + configKey,
    method: 'put',
    params: { configValue }
  })
}

export function addFiscal(data) {
  return request({ url: '/fin/config/fiscal', method: 'post', data: data })
}

export function delFiscal(configKey) {
  return request({ url: '/fin/config/fiscal/' + configKey, method: 'delete' })
}

// ==================== 成本方法配置 ====================

export function listCostMethod(query) {
  return request({ url: '/fin/config/costMethod/list', method: 'get', params: query })
}

export function addCostMethod(data) {
  return request({ url: '/fin/config/costMethod', method: 'post', data: data })
}

export function updateCostMethod(data) {
  return request({ url: '/fin/config/costMethod', method: 'put', data: data })
}

export function delCostMethod(ids) {
  return request({ url: '/fin/config/costMethod/' + ids, method: 'delete' })
}

// ==================== 进项税抵扣台账 ====================

export function listInputTax(query) {
  return request({ url: '/fin/taxProvision/inputTax/list', method: 'get', params: query })
}

export function generateInputTax(apId) {
  return request({ url: '/fin/taxProvision/inputTax/generate/' + apId, method: 'post' })
}

export function deductInputTax(taxId, deductPeriod) {
  return request({
    url: '/fin/taxProvision/inputTax/deduct/' + taxId,
    method: 'put',
    params: { deductPeriod }
  })
}

export function voidInputTax(taxId) {
  return request({ url: '/fin/taxProvision/inputTax/void/' + taxId, method: 'put' })
}

export function markInputTaxExpired() {
  return request({ url: '/fin/taxProvision/inputTax/markExpired', method: 'post' })
}

export function inputTaxSummary(deductStatus) {
  return request({
    url: '/fin/taxProvision/inputTax/summary',
    method: 'get',
    params: { deductStatus }
  })
}

export function expiringInputTax(days) {
  return request({
    url: '/fin/taxProvision/inputTax/expiring',
    method: 'get',
    params: { days }
  })
}

// ==================== 存货跌价准备 ====================

export function listProvision(query) {
  return request({ url: '/fin/taxProvision/provision/list', method: 'get', params: query })
}

export function pendingProvision(period) {
  return request({
    url: '/fin/taxProvision/provision/pending',
    method: 'get',
    params: { period }
  })
}

export function doProvision(period, materialId, ratio) {
  return request({
    url: '/fin/taxProvision/provision/do',
    method: 'post',
    params: { period, materialId, ratio }
  })
}

export function reverseProvision(valId) {
  return request({ url: '/fin/taxProvision/provision/reverse/' + valId, method: 'put' })
}

export function provisionSummary(period) {
  return request({
    url: '/fin/taxProvision/provision/summary',
    method: 'get',
    params: { period }
  })
}

// ==================== 银行对账单 ====================

export function listStatement(query) {
  return request({ url: '/fin/fund/statement/list', method: 'get', params: query })
}

export function getStatement(statementId) {
  return request({ url: '/fin/fund/statement/' + statementId, method: 'get' })
}

export function importStatement(data) {
  return request({ url: '/fin/fund/statement/import', method: 'post', data: data })
}

export function autoMatch(statementId) {
  return request({ url: '/fin/fund/statement/autoMatch/' + statementId, method: 'post' })
}

export function manualMatch(itemId, flowId) {
  return request({
    url: '/fin/fund/statement/match/' + itemId,
    method: 'put',
    params: { flowId }
  })
}

export function unmatch(itemId) {
  return request({ url: '/fin/fund/statement/unmatch/' + itemId, method: 'put' })
}

export function matchCandidates(accountId, amount, date) {
  return request({
    url: '/fin/fund/statement/candidates',
    method: 'get',
    params: { accountId, amount, date }
  })
}

export function reconcileStatement(statementId) {
  return request({ url: '/fin/fund/statement/reconcile/' + statementId, method: 'post' })
}

export function unreconciled(statementId) {
  return request({ url: '/fin/fund/statement/unreconciled/' + statementId, method: 'get' })
}

export function delStatement(ids) {
  return request({ url: '/fin/fund/statement/' + ids, method: 'delete' })
}
