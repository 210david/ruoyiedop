import request from '@/utils/request'

export function listReturnMaterial(query) {
  return request({
    url: '/mms/return/list',
    method: 'get',
    params: query
  })
}

export function getReturnMaterial(returnId) {
  return request({
    url: '/mms/return/' + returnId,
    method: 'get'
  })
}

export function addReturnMaterial(data) {
  return request({
    url: '/mms/return',
    method: 'post',
    data: data
  })
}

export function delReturnMaterial(returnId) {
  return request({
    url: '/mms/return/' + returnId,
    method: 'delete'
  })
}

export function updateReturnMaterial(data) {
  return request({
    url: '/mms/return',
    method: 'put',
    data: data
  })
}

// 查询已领料的领料单列表（供退料选择）
export function listIssueForReturn(query) {
  return request({
    url: '/mms/return/issueList',
    method: 'get',
    params: query
  })
}

// 根据领料单ID查询领料单详情（带明细），供退料页面带出信息
export function getIssueInfo(issueId) {
  return request({
    url: '/mms/return/issueInfo/' + issueId,
    method: 'get'
  })
}

// 确认退料
export function confirmReturnMaterial(data) {
  return request({
    url: '/mms/return/confirm',
    method: 'put',
    data: data
  })
}

// 按工单号查询退料明细（按物料拆分，每条明细一行）
export function listReturnDetailByWorkOrder(workOrderNo) {
  return request({
    url: '/mms/return/detailListByWorkOrder',
    method: 'get',
    params: { workOrderNo }
  })
}
