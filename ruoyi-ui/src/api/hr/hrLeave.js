import request from '@/utils/request'

// 查询离职列表
export function listHrLeave(query) {
  return request({
    url: '/hr/leave/list',
    method: 'get',
    params: query
  })
}

// 查询离职详细
export function getHrLeave(leaveId) {
  return request({
    url: '/hr/leave/' + leaveId,
    method: 'get'
  })
}

// 新增离职
export function addHrLeave(data) {
  return request({
    url: '/hr/leave',
    method: 'post',
    data: data
  })
}

// 修改离职
export function updateHrLeave(data) {
  return request({
    url: '/hr/leave',
    method: 'put',
    data: data
  })
}

// 删除离职
export function delHrLeave(leaveId) {
  return request({
    url: '/hr/leave/' + leaveId,
    method: 'delete'
  })
}

// 离职审批
export function auditHrLeave(leaveId, auditAction, auditRemark) {
  return request({
    url: '/hr/leave/audit/' + leaveId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}
