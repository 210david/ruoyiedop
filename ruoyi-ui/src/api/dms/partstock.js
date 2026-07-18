import request from '@/utils/request'

/** 库存流水账列表 */
export function listPartStock(query) {
  return request({ url: '/dms/sparepart/record/list', method: 'get', params: query })
}

/** 删除流水记录 */
export function delPartStock(recordId) {
  return request({ url: '/dms/sparepart/record/' + recordId, method: 'delete' })
}
