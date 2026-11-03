import request from '@/utils/request'

// 供应商整改单（客诉供应商责任处理）
export function listRectify(query) {
  return request({ url: '/qms/rectify/list', method: 'get', params: query })
}
export function getRectify(rectifyId) {
  return request({ url: '/qms/rectify/' + rectifyId, method: 'get' })
}
export function addRectify(data) {
  return request({ url: '/qms/rectify', method: 'post', data: data })
}
export function updateRectify(data) {
  return request({ url: '/qms/rectify', method: 'put', data: data })
}
export function delRectify(rectifyIds) {
  return request({ url: '/qms/rectify/' + rectifyIds, method: 'delete' })
}
// 关闭整改单（已回复/逾期升级 → 已关闭）
export function closeRectify(rectifyId) {
  return request({ url: '/qms/rectify/close/' + rectifyId, method: 'put' })
}
