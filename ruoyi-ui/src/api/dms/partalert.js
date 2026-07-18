import request from '@/utils/request'

/** 库存预警列表 */
export function listPartAlert(query) {
  return request({ url: '/dms/sparepart/alert/list', method: 'get', params: query })
}

/** 删除库存预警（清除安全库存设置） */
export function delPartAlert(partId) {
  return request({ url: '/dms/sparepart/alert/' + partId, method: 'delete' })
}
