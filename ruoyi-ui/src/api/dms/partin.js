import request from '@/utils/request'

/** 入库记录列表 */
export function listPartIn(query) {
  return request({ url: '/dms/sparepart/record/list', method: 'get', params: { ...query, moveType: '0' } })
}

/** 入库操作 */
export function addPartIn(data) {
  return request({ url: '/dms/sparepart/stockMove', method: 'post', data: { ...data, moveType: '0', targetType: '4' } })
}

/** 生成入库单号 */
export function genPartInCode() {
  return request({ url: '/dms/sparepart/genInCode', method: 'get' })
}

/** 删除入库记录 */
export function delPartIn(recordId) {
  return request({ url: '/dms/sparepart/record/' + recordId, method: 'delete' })
}
