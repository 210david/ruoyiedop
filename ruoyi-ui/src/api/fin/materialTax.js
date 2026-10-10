import request from '@/utils/request'

// ==================== 物料税率档案 ====================

// 查询税率档案列表
export function listMaterialTax(query) {
  return request({
    url: '/fin/materialTax/list',
    method: 'get',
    params: query
  })
}

// 按物料编码查生效税率
export function getEffectiveTax(materialCode) {
  return request({
    url: '/fin/materialTax/effective/' + materialCode,
    method: 'get'
  })
}

// 新增税率档案
export function addMaterialTax(data) {
  return request({
    url: '/fin/materialTax',
    method: 'post',
    data: data
  })
}

// 修改税率档案
export function updateMaterialTax(data) {
  return request({
    url: '/fin/materialTax',
    method: 'put',
    data: data
  })
}

// 删除税率档案
export function delMaterialTax(ids) {
  return request({
    url: '/fin/materialTax/' + ids,
    method: 'delete'
  })
}

// 未建档物料
export function listUntaxedMaterials(limit = 100) {
  return request({
    url: '/fin/materialTax/untaxed',
    method: 'get',
    params: { limit }
  })
}

// 批量初始化税率
export function batchInitTax(taxRate) {
  return request({
    url: '/fin/materialTax/batchInit',
    method: 'post',
    params: { taxRate }
  })
}

// 档案统计
export function countMaterialTax() {
  return request({
    url: '/fin/materialTax/count',
    method: 'get'
  })
}