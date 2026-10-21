import request from '@/utils/request'

export function listSparepart(query) {
  return request({ url: '/dms/sparepart/list', method: 'get', params: query })
}

export function getSparepart(partId) {
  return request({ url: '/dms/sparepart/' + partId, method: 'get' })
}

export function addSparepart(data) {
  return request({ url: '/dms/sparepart', method: 'post', data: data })
}

export function updateSparepart(data) {
  return request({ url: '/dms/sparepart', method: 'put', data: data })
}

export function delSparepart(partId) {
  return request({ url: '/dms/sparepart/' + partId, method: 'delete' })
}

export function stockMove(data) {
  return request({ url: '/dms/sparepart/stockMove', method: 'post', data: data })
}

export function listSparepartRecord(query) {
  return request({ url: '/dms/sparepart/record/list', method: 'get', params: query })
}

/** 自动生成备件编号 */
export function genPartCode() {
  return request({ url: '/dms/sparepart/genCode', method: 'get' })
}

/** 自动生成入库单号 */
export function genInCode() {
  return request({ url: '/dms/sparepart/genInCode', method: 'get' })
}

/** 自动生成出库单号 */
export function genOutCode() {
  return request({ url: '/dms/sparepart/genOutCode', method: 'get' })
}
