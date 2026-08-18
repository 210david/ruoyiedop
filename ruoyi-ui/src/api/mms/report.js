import request from '@/utils/request'

// 查询报工列表
export function listWorkReport(query) {
  return request({
    url: '/mms/report/list',
    method: 'get',
    params: query
  })
}

// 查询报工详情
export function getWorkReport(reportId) {
  return request({
    url: '/mms/report/' + reportId,
    method: 'get'
  })
}

// 新增报工
export function addWorkReport(data) {
  return request({
    url: '/mms/report',
    method: 'post',
    data: data
  })
}

// 修改报工
export function updateWorkReport(data) {
  return request({
    url: '/mms/report',
    method: 'put',
    data: data
  })
}

// 删除报工
export function delWorkReport(reportId) {
  return request({
    url: '/mms/report/' + reportId,
    method: 'delete'
  })
}

// 报工审核
export function auditWorkReport(reportId, status, auditRemark) {
  return request({
    url: '/mms/report/audit/' + reportId,
    method: 'put',
    params: { status, auditRemark }
  })
}
