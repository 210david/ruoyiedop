import request from '@/utils/request'

// 库存预警列表（分页）
export function listStockAlert(query) {
  return request({
    url: '/wms/stockAlert/list',
    method: 'get',
    params: query
  })
}

// 库存预警汇总（各类型预警数量）
export function getStockAlertSummary() {
  return request({
    url: '/wms/stockAlert/summary',
    method: 'get'
  })
}

// 删除库存预警
export function delStockAlert(data) {
  return request({
    url: '/wms/stockAlert/remove',
    method: 'post',
    data: data
  })
}
