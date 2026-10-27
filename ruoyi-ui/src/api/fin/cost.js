import request from '@/utils/request'

// ==================== 存货成本 ====================

// 查询成本台账列表
export function listCost(query) {
  return request({
    url: '/fin/cost/list',
    method: 'get',
    params: query
  })
}

// 入库计价（扫描已完成入库单）
export function priceInbound() {
  return request({
    url: '/fin/cost/priceInbound',
    method: 'post'
  })
}

// 出库成本结转（扫描已完成出库单）
export function carryOutbound() {
  return request({
    url: '/fin/cost/carryOutbound',
    method: 'post'
  })
}

// 期末存货计价表
export function getValuation(period) {
  return request({
    url: '/fin/cost/valuation',
    method: 'get',
    params: { period }
  })
}

// 成本总账
export function getTotalCost() {
  return request({
    url: '/fin/cost/totalCost',
    method: 'get'
  })
}

// 录入期初成本
export function initCost(data) {
  return request({
    url: '/fin/cost/init',
    method: 'post',
    data: data
  })
}

// 查询结转流水
export function listCarry(query) {
  return request({
    url: '/fin/cost/carryList',
    method: 'get',
    params: query
  })
}