import request from '@/utils/request'

// 查询金蝶销售出库单列表
export function listSalOutbound(query) {
  return request({
    url: '/kingdee/sal-outbound/list',
    method: 'get',
    params: query
  })
}

// 从金蝶同步销售出库单
export function syncSalOutbound() {
  return request({
    url: '/kingdee/sal-outbound/sync',
    method: 'post',
    timeout: 300000
  })
}
