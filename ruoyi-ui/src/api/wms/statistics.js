import request from '@/utils/request'

// 概览统计
export function getOverview() {
  return request({
    url: '/wms/statistics/overview',
    method: 'get'
  })
}

// 按仓库统计库存分布
export function getInventoryByWarehouse() {
  return request({
    url: '/wms/statistics/inventoryByWarehouse',
    method: 'get'
  })
}

// 按物料类型统计库存分布
export function getInventoryByMaterialType() {
  return request({
    url: '/wms/statistics/inventoryByMaterialType',
    method: 'get'
  })
}

// 出入库趋势（按日统计）
export function getInboundOutboundTrend(query) {
  return request({
    url: '/wms/statistics/inboundOutboundTrend',
    method: 'get',
    params: query
  })
}

// 入库按类型统计
export function getInboundByType(query) {
  return request({
    url: '/wms/statistics/inboundByType',
    method: 'get',
    params: query
  })
}

// 出库按类型统计
export function getOutboundByType(query) {
  return request({
    url: '/wms/statistics/outboundByType',
    method: 'get',
    params: query
  })
}

// 物料库存排行 Top N
export function getMaterialTop(count) {
  return request({
    url: '/wms/statistics/materialTop',
    method: 'get',
    params: { count }
  })
}

// 库存预警（低于安全库存下限）
export function getStockAlert() {
  return request({
    url: '/wms/statistics/stockAlert',
    method: 'get'
  })
}
