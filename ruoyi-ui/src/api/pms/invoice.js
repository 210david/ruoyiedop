import request from '@/utils/request'

export function listInvoice(query) {
  return request({
    url: '/pms/invoice/list',
    method: 'get',
    params: query
  })
}

export function getInvoice(invoiceId) {
  return request({
    url: '/pms/invoice/' + invoiceId,
    method: 'get'
  })
}

export function addInvoice(data) {
  return request({
    url: '/pms/invoice',
    method: 'post',
    data: data
  })
}

export function updateInvoice(data) {
  return request({
    url: '/pms/invoice',
    method: 'put',
    data: data
  })
}

export function delInvoice(invoiceId) {
  return request({
    url: '/pms/invoice/' + invoiceId,
    method: 'delete'
  })
}

// 提交审核
export function submitInvoice(invoiceId) {
  return request({
    url: '/pms/invoice/submit/' + invoiceId,
    method: 'put'
  })
}

// 审核发票
export function auditInvoice(invoiceId, status, auditOpinion) {
  return request({
    url: '/pms/invoice/audit/' + invoiceId,
    method: 'put',
    params: { status, auditOpinion }
  })
}

export function payInvoice(data) {
  return request({
    url: '/pms/invoice/pay',
    method: 'put',
    data: data
  })
}

// 三方匹配
export function threeWayMatch(invoiceId) {
  return request({
    url: '/pms/invoice/match/' + invoiceId,
    method: 'get'
  })
}

// 发票识别（上传PDF，返回识别结果）
export function recognizeInvoice(formData) {
  return request({
    url: '/pms/invoice/recognize',
    method: 'post',
    data: formData,
    timeout: 60000,
    headers: { 'repeatSubmit': false, 'Content-Type': false }
  })
}
