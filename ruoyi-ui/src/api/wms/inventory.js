import request from '@/utils/request'

export function listInventory(query) {
  return request({
    url: '/wms/inventory/list',
    method: 'get',
    params: query
  })
}

// 按物料+仓库汇总查询（管理视角）
export function listInventorySummary(query) {
  return request({
    url: '/wms/inventory/summary',
    method: 'get',
    params: query
  })
}

export function getInventory(inventoryId) {
  return request({
    url: '/wms/inventory/' + inventoryId,
    method: 'get'
  })
}

export function listInventoryLog(query) {
  return request({
    url: '/wms/log/list',
    method: 'get',
    params: query
  })
}

export function delInventory(inventoryId) {
  return request({
    url: '/wms/inventory/' + inventoryId,
    method: 'delete'
  })
}

export function delInventoryLog(logId) {
  return request({
    url: '/wms/log/' + logId,
    method: 'delete'
  })
}
