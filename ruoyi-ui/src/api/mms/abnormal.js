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
export function respondAbnormal(abnormalId, data) {
  return request({
    url: '/mms/abnormal/respond/' + abnormalId,
    method: 'put',
    data: data
  })
}

// 异常处理关闭
export function resolveAbnormal(abnormalId, data) {
  return request({
    url: '/mms/abnormal/resolve/' + abnormalId,
    method: 'put',
    data: data
  })
}

// 联动生成停机记录
export function linkDowntime(abnormalId) {
  return request({
    url: '/mms/abnormal/linkDowntime/' + abnormalId,
    method: 'post'
  })
}
