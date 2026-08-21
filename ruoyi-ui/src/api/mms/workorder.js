import request from '@/utils/request'

// 查询工单列表
export function listWorkOrder(query) {
  return request({
    url: '/mms/workorder/list',
    method: 'get',
    params: query
  })
}

// 查询工单详情
export function getWorkOrder(workOrderId) {
  return request({
    url: '/mms/workorder/' + workOrderId,
    method: 'get'
  })
}

// 新增工单
export function addWorkOrder(data) {
  return request({
    url: '/mms/workorder',
    method: 'post',
    data: data
  })
}

// 修改工单
export function updateWorkOrder(data) {
  return request({
    url: '/mms/workorder',
    method: 'put',
    data: data
  })
}

// 删除工单
export function delWorkOrder(workOrderId) {
  return request({
    url: '/mms/workorder/' + workOrderId,
    method: 'delete'
  })
}

// 工单下达预览（获取BOM明细+工艺工序+校验结果）
export function getReleasePreview(workOrderId) {
  return request({
    url: '/mms/workorder/release/preview/' + workOrderId,
    method: 'get'
  })
}

// 工单下达
export function releaseWorkOrder(workOrderId) {
  return request({
    url: '/mms/workorder/release/' + workOrderId,
    method: 'put'
  })
}

// 工单暂停
export function pauseWorkOrder(workOrderId, pauseReason) {
  return request({
    url: '/mms/workorder/pause/' + workOrderId,
    method: 'put',
    params: { pauseReason }
  })
}

// 工单恢复
export function resumeWorkOrder(workOrderId) {
  return request({
    url: '/mms/workorder/resume/' + workOrderId,
    method: 'put'
  })
}

// 工单完工
export function finishWorkOrder(workOrderId) {
  return request({
    url: '/mms/workorder/finish/' + workOrderId,
    method: 'put'
  })
}

// 工单关闭
export function closeWorkOrder(workOrderId, closeRemark) {
  return request({
    url: '/mms/workorder/close/' + workOrderId,
    method: 'put',
    params: { closeRemark }
  })
}

// 工单作废
export function cancelWorkOrder(workOrderId, cancelReason) {
  return request({
    url: '/mms/workorder/cancel/' + workOrderId,
    method: 'put',
    params: { cancelReason }
  })
}

// 工单拆分
export function splitWorkOrder(workOrderId, splitQty) {
  return request({
    url: '/mms/workorder/split/' + workOrderId,
    method: 'post',
    params: { splitQty }
  })
}

// 查询工单审核日志
export function getWorkOrderAuditLog(workOrderId) {
  return request({
    url: '/mms/workorder/auditLog/' + workOrderId,
    method: 'get'
  })
}
