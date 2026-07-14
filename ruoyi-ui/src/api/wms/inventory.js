import request from '@/utils/request'

export function listInventory(query) {
  return request({
    url: '/wms/inventory/list',
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
