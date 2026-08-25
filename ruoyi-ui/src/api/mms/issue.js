import request from '@/utils/request'

export function listIssue(query) {
  return request({
    url: '/mms/issue/list',
    method: 'get',
    params: query
  })
}

export function getIssue(issueId) {
  return request({
    url: '/mms/issue/' + issueId,
    method: 'get'
  })
}

export function addIssue(data) {
  return request({
    url: '/mms/issue',
    method: 'post',
    data: data
  })
}

export function updateIssue(data) {
  return request({
    url: '/mms/issue',
    method: 'put',
    data: data
  })
}

export function delIssue(issueId) {
  return request({
    url: '/mms/issue/' + issueId,
    method: 'delete'
  })
}

// 确认领料
export function confirmIssue(data) {
  return request({
    url: '/mms/issue/confirm',
    method: 'put',
    data: data
  })
}

// 按工单号查询领料明细（按物料拆分，每条明细一行）
export function listIssueDetailByWorkOrder(workOrderNo) {
  return request({
    url: '/mms/issue/detailListByWorkOrder',
    method: 'get',
    params: { workOrderNo }
  })
}
