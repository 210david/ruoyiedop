import request from '@/utils/request'

// 查询个税税率列表
export function listHrTaxRate(query) {
  return request({
    url: '/hr/taxRate/list',
    method: 'get',
    params: query
  })
}

// 查询个税税率详细
export function getHrTaxRate(taxRateId) {
  return request({
    url: '/hr/taxRate/' + taxRateId,
    method: 'get'
  })
}

// 新增个税税率
export function addHrTaxRate(data) {
  return request({
    url: '/hr/taxRate',
    method: 'post',
    data: data
  })
}

// 修改个税税率
export function updateHrTaxRate(data) {
  return request({
    url: '/hr/taxRate',
    method: 'put',
    data: data
  })
}

// 删除个税税率
export function delHrTaxRate(taxRateId) {
  return request({
    url: '/hr/taxRate/' + taxRateId,
    method: 'delete'
  })
}
