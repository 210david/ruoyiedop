import request from '@/utils/request'

/** 备件库存台账列表（查询 stock 表，关联主数据） */
export function listPartLedger(query) {
  return request({ url: '/dms/sparepart/stock/list', method: 'get', params: query })
}

/** 删除库存记录（只删库存，不删主数据） */
export function delPartLedger(stockId) {
  return request({ url: '/dms/sparepart/stock/' + stockId, method: 'delete' })
}
