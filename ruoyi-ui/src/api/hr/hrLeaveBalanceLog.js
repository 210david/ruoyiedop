import request from '@/utils/request'

// 查询假期余额扣减明细列表
export function listHrLeaveBalanceLog(query) {
  return request({
    url: '/hr/leaveBalanceLog/list',
    method: 'get',
    params: query
  })
}
