import request from '@/utils/request'

/** 出库记录列表 */
export function listPartOut(query) {
  return request({ url: '/dms/sparepart/record/list', method: 'get', params: { ...query, moveType: 'not0' } })
}

/** 出库操作 */
export function addPartOut(data) {
  return request({ url: '/dms/sparepart/stockMove', method: 'post', data: { ...data, moveType: '1', sourceType: '1' } })
}

/** 生成出库单号 */
export function genPartOutCode() {
  return request({ url: '/dms/sparepart/genOutCode', method: 'get' })
}

/** 删除出库记录 */
export function delPartOut(recordId) {
  return request({ url: '/dms/sparepart/record/' + recordId, method: 'delete' })
}
