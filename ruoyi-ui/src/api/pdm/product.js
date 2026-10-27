import request from '@/utils/request'

// 查询产品档案列表（分页）
export function listProduct(query) {
  return request({
    url: '/pdm/product/list',
    method: 'get',
    params: query
  })
}

// 查询产品档案列表（全量，供选择器）
export function listAllProduct(query) {
  return request({
    url: '/pdm/product/listAll',
    method: 'get',
    params: query
  })
}

// 查询产品档案详细
export function getProduct(productId) {
  return request({
    url: '/pdm/product/' + productId,
    method: 'get'
  })
}

// 新增产品档案（产品编码=物料编码）
export function addProduct(data) {
  return request({
    url: '/pdm/product',
    method: 'post',
    data: data
  })
}

// 修改产品档案
export function updateProduct(data) {
  return request({
    url: '/pdm/product',
    method: 'put',
    data: data
  })
}

// 删除产品档案（仅限"设计中"）
export function delProduct(productIds) {
  return request({
    url: '/pdm/product/' + productIds,
    method: 'delete'
  })
}

// 产品停产（生命周期终止动作，不走变更单）
export function discontinueProduct(productId, remark) {
  return request({
    url: '/pdm/product/discontinue/' + productId,
    method: 'put',
    data: { remark }
  })
}

// 产品复产（已停产 → 已定型）
export function resumeProduct(productId) {
  return request({
    url: '/pdm/product/resume/' + productId,
    method: 'put'
  })
}
