import request from '@/utils/request'

// 查询金蝶库存列表
export function listInventory(query) {
  return request({
    url: '/kingdee/inventory/list',
    method: 'get',
    params: query
  })
}

// 查询库存分类列表
export function listInventoryCategories() {
  return request({
    url: '/kingdee/inventory/categories',
    method: 'get'
  })
}

// 同步金蝶库存
export function syncInventory() {
  return request({
    url: '/kingdee/inventory/sync',
    method: 'post',
    timeout: 300000
  })
}
