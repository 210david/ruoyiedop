import request from '@/utils/request'

// 查询金蝶采购入库单列表
export function listPurInStock(query) {
  return request({
    url: '/kingdee/pur-in-stock/list',
    method: 'get',
    params: query
  })
}

// 从金蝶同步采购入库单
export function syncPurInStock() {
  return request({
    url: '/kingdee/pur-in-stock/sync',
    method: 'post',
    timeout: 300000
  })
}
