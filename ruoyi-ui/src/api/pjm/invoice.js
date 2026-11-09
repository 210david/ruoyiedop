import request from '@/utils/request'

// 查询开票记录列表
export function listInvoice(query) {
  return request({ url: '/pjm/invoice/list', method: 'get', params: query })
}

// 查询开票记录详情
export function getInvoice(invoiceId) {
  return request({ url: '/pjm/invoice/' + invoiceId, method: 'get' })
}

// 新增开票记录
export function addInvoice(data) {
  return request({ url: '/pjm/invoice', method: 'post', data: data })
}

// 修改开票记录
export function updateInvoice(data) {
  return request({ url: '/pjm/invoice', method: 'put', data: data })
}

// 删除开票记录
export function delInvoice(invoiceIds) {
  return request({ url: '/pjm/invoice/' + invoiceIds, method: 'delete' })
}

// 开票确认（待开票 -> 已开票）
export function issueInvoice(invoiceId) {
  return request({ url: '/pjm/invoice/issue/' + invoiceId, method: 'put' })
}

// 开票作废（已开票 -> 已作废）
export function voidInvoice(invoiceId) {
  return request({ url: '/pjm/invoice/void/' + invoiceId, method: 'put' })
}

// 发票识别（上传PDF，返回识别结果）
export function recognizeInvoice(data, config) {
  return request({ url: '/pjm/invoice/recognize', method: 'post', data, timeout: 60000, headers: { 'repeatSubmit': false, 'Content-Type': false }, ...config })
}
