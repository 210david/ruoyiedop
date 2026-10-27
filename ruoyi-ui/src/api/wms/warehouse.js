import request from '@/utils/request'

// ==================== 仓库结构管理（统一） ====================

// 查询仓库结构列表（全部节点，用于树形管理页）
export function listWarehouseTree(query) {
  return request({
    url: '/wms/warehouse/list',
    method: 'get',
    params: query
  })
}

// 查询仓库详情
export function getWarehouse(warehouseId) {
  return request({
    url: '/wms/warehouse/' + warehouseId,
    method: 'get'
  })
}

// 新增仓库/仓区/仓位
export function addWarehouse(data) {
  return request({
    url: '/wms/warehouse',
    method: 'post',
    data: data
  })
}

// 修改仓库/仓区/仓位
export function updateWarehouse(data) {
  return request({
    url: '/wms/warehouse',
    method: 'put',
    data: data
  })
}

// 删除仓库/仓区/仓位
export function delWarehouse(warehouseId) {
  return request({
    url: '/wms/warehouse/' + warehouseId,
    method: 'delete'
  })
}

// 生成下一个编码
export function genWarehouseCode(nodeType, parentId) {
  return request({
    url: '/wms/warehouse/genCode',
    method: 'get',
    params: { nodeType, parentId }
  })
}

// ==================== 兼容接口（供其他模块下拉选择调用） ====================

// 查询仓库列表（仅仓库层级）
export function listWarehouse(query) {
  return request({
    url: '/wms/warehouse/warehouseList',
    method: 'get',
    params: query
  })
}

// 查询仓区列表（仅仓区层级）
export function listArea(query) {
  return request({
    url: '/wms/warehouse/area/list',
    method: 'get',
    params: query
  })
}

// 查询仓位列表（仅仓位层级）
export function listLocation(query) {
  return request({
    url: '/wms/warehouse/location/list',
    method: 'get',
    params: query
  })
}
