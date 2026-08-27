import request from '@/utils/request'

// 查询假期余额列表
export function listHrLeaveBalance(query) {
  return request({
    url: '/hr/leaveBalance/list',
    method: 'get',
    params: query
  })
}

// 查询假期余额详细
export function getHrLeaveBalance(balanceId) {
  return request({
    url: '/hr/leaveBalance/' + balanceId,
    method: 'get'
  })
}

// 新增假期余额
export function addHrLeaveBalance(data) {
  return request({
    url: '/hr/leaveBalance',
    method: 'post',
    data: data
  })
}

// 修改假期余额
export function updateHrLeaveBalance(data) {
  return request({
    url: '/hr/leaveBalance',
    method: 'put',
    data: data
  })
}

// 删除假期余额
export function delHrLeaveBalance(balanceId) {
  return request({
    url: '/hr/leaveBalance/' + balanceId,
    method: 'delete'
  })
}
