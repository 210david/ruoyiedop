import request from '@/utils/request'

// 查询价格比价列表
export function listPriceCompare(query) {
  return request({
    url: '/pms/priceCompare/list',
    method: 'get',
    params: query
  })
}

// 导出价格比价
export function exportPriceCompare(query) {
  return request({
    url: '/pms/priceCompare/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
