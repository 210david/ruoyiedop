import request from '@/utils/request'

// 查询异常列表
export function listAbnormal(query) {
  return request({
    url: '/mms/abnormal/list',
    method: 'get',
    params: query
  })
}

// 查询异常详情
export function getAbnormal(abnormalId) {
  return request({
    url: '/mms/abnormal/' + abnormalId,
    method: 'get'
  })
}

// 新增异常
export function addAbnormal(data) {
  return request({
    url: '/mms/abnormal',
    method: 'post',
    data: data
  })
}

// 修改异常
export function updateAbnormal(data) {
  return request({
    url: '/mms/abnormal',
    method: 'put',
    data: data
  })
}

// 删除异常
export function delAbnormal(abnormalId) {
  return request({
    url: '/mms/abnormal/' + abnormalId,
    method: 'delete'
  })
}

// 异常响应
export function respondAbnormal(abnormalId, responseBy) {
  return request({
    url: '/mms/abnormal/respond/' + abnormalId,
    method: 'put',
    params: { responseBy }
  })
}

// 异常处理关闭
export function resolveAbnormal(abnormalId, handleResult) {
  return request({
    url: '/mms/abnormal/resolve/' + abnormalId,
    method: 'put',
    params: { handleResult }
  })
}
