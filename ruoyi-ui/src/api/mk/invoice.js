import request from '@/utils/request'

// 查询发票列表
export function listInvoice(query) {
  return request({ url: '/mk/invoice/list', method: 'get', params: query })
}

// 查询发票详细
export function getInvoice(invoiceId) {
  return request({ url: '/mk/invoice/' + invoiceId, method: 'get' })
}

// 新增发票
export function addInvoice(data) {
  return request({ url: '/mk/invoice', method: 'post', data: data })
}

// 修改发票
export function updateInvoice(data) {
  return request({ url: '/mk/invoice', method: 'put', data: data })
}

// 删除发票
export function delInvoice(invoiceId) {
  return request({ url: '/mk/invoice/' + invoiceId, method: 'delete' })
}

// 开票
export function issueInvoice(invoiceId) {
  return request({ url: '/mk/invoice/issue/' + invoiceId, method: 'put' })
}

// 作废发票
export function voidInvoice(invoiceId) {
  return request({ url: '/mk/invoice/void/' + invoiceId, method: 'put' })
}

// 发票识别（上传PDF，返回识别结果）
// 使用 request service 实例，通过 repeatSubmit:false 跳过防重复提交检查（FormData 无法序列化）
// 注意：Content-Type 设为 false，让 axios 检测到 FormData 后自动设置带 boundary 的 multipart/form-data
export function recognizeInvoice(formData) {
  return request({
    url: '/mk/invoice/recognize',
    method: 'post',
    data: formData,
    timeout: 60000,
    headers: {
      'repeatSubmit': false,
      'Content-Type': false
    }
  })
}
