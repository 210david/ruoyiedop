import request from '@/utils/request'

// 查询询价单列表
export function listInquiry(query) {
  return request({
    url: '/pms/inquiry/list',
    method: 'get',
    params: query
  })
}

// 查询询价单详细
export function getInquiry(inquiryId) {
  return request({
    url: '/pms/inquiry/' + inquiryId,
    method: 'get'
  })
}

// 新增询价单
export function addInquiry(data) {
  return request({
    url: '/pms/inquiry',
    method: 'post',
    data: data
  })
}

// 修改询价单
export function updateInquiry(data) {
  return request({
    url: '/pms/inquiry',
    method: 'put',
    data: data
  })
}

// 删除询价单
export function delInquiry(inquiryId) {
  return request({
    url: '/pms/inquiry/' + inquiryId,
    method: 'delete'
  })
}

// 发布询价单
export function publishInquiry(inquiryId) {
  return request({
    url: '/pms/inquiry/publish/' + inquiryId,
    method: 'put'
  })
}

// 截止询价单
export function closeInquiry(inquiryId) {
  return request({
    url: '/pms/inquiry/close/' + inquiryId,
    method: 'put'
  })
}

// 比价（进入比价定标环节）
export function compareInquiry(inquiryId) {
  return request({
    url: '/pms/inquiry/compare/' + inquiryId,
    method: 'put'
  })
}

// 定标
export function awardInquiry(inquiryId, supplierId, supplierName, amount) {
  return request({
    url: '/pms/inquiry/award/' + inquiryId,
    method: 'put',
    params: { supplierId, supplierName, amount }
  })
}

// 导出询价单
export function exportInquiry(query) {
  return request({
    url: '/pms/inquiry/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}

// 录入供应商报价
export function addQuotation(data) {
  return request({
    url: '/pms/inquiry/quotation',
    method: 'post',
    data: data
  })
}

// 删除供应商报价
export function delQuotation(quotationId) {
  return request({
    url: '/pms/inquiry/quotation/' + quotationId,
    method: 'delete'
  })
}

// 提交审核
export function submitInquiry(inquiryId) {
  return request({
    url: '/pms/inquiry/submit/' + inquiryId,
    method: 'put'
  })
}

// 审核询价单
export function auditInquiry(inquiryId, status, auditOpinion) {
  return request({
    url: '/pms/inquiry/audit/' + inquiryId,
    method: 'put',
    params: { status, auditOpinion }
  })
}
