import request from '@/utils/request'

// 查询请假列表
export function listHrLeaveRequest(query) {
  return request({
    url: '/hr/leaveRequest/list',
    method: 'get',
    params: query
  })
}

// 查询请假详细
export function getHrLeaveRequest(leaveReqId) {
  return request({
    url: '/hr/leaveRequest/' + leaveReqId,
    method: 'get'
  })
}

// 新增请假
export function addHrLeaveRequest(data) {
  return request({
    url: '/hr/leaveRequest',
    method: 'post',
    data: data
  })
}

// 修改请假
export function updateHrLeaveRequest(data) {
  return request({
    url: '/hr/leaveRequest',
    method: 'put',
    data: data
  })
}

// 删除请假
export function delHrLeaveRequest(leaveReqId) {
  return request({
    url: '/hr/leaveRequest/' + leaveReqId,
    method: 'delete'
  })
}
