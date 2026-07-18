import request from '@/utils/request'

/** 备件库存台账列表 */
export function listPartLedger(query) {
  return request({ url: '/dms/sparepart/list', method: 'get', params: query })
}

/** 删除备件（台账） */
export function delPartLedger(partId) {
  return request({ url: '/dms/sparepart/' + partId, method: 'delete' })
}
