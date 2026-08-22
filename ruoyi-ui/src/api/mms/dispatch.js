import request from '@/utils/request'

// 查询派工单列表
export function listDispatch(query) {
  return request({
    url: '/mms/dispatch/list',
    method: 'get',
    params: query
  })
}

// 查询派工单详情
export function getDispatch(dispatchId) {
  return request({
    url: '/mms/dispatch/' + dispatchId,
    method: 'get'
  })
}

// 新增派工单
export function addDispatch(data) {
  return request({
    url: '/mms/dispatch',
    method: 'post',
    data: data
  })
}

// 修改派工单
export function updateDispatch(data) {
  return request({
    url: '/mms/dispatch',
    method: 'put',
    data: data
  })
}

// 删除派工单
export function delDispatch(dispatchId) {
  return request({
    url: '/mms/dispatch/' + dispatchId,
    method: 'delete'
  })
}

// 派工开工（需填写操作人员）
export function startDispatch(dispatchId, data) {
  return request({
    url: '/mms/dispatch/start/' + dispatchId,
    method: 'put',
    data: data || {}
  })
}

// 派工完工（需填写合格数量、不良数量、备注）
export function finishDispatch(dispatchId, data) {
  return request({
    url: '/mms/dispatch/finish/' + dispatchId,
    method: 'put',
    data: data
  })
}

// 派工取消
export function cancelDispatch(dispatchId) {
  return request({
    url: '/mms/dispatch/cancel/' + dispatchId,
    method: 'put'
  })
}
