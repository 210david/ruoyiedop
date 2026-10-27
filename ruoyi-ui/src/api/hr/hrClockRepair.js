import request from '@/utils/request'

// 查询补卡列表
export function listHrClockRepair(query) {
  return request({
    url: '/hr/clockRepair/list',
    method: 'get',
    params: query
  })
}

// 查询补卡详细
export function getHrClockRepair(clockRepairId) {
  return request({
    url: '/hr/clockRepair/' + clockRepairId,
    method: 'get'
  })
}

// 新增补卡
export function addHrClockRepair(data) {
  return request({
    url: '/hr/clockRepair',
    method: 'post',
    data: data
  })
}

// 修改补卡
export function updateHrClockRepair(data) {
  return request({
    url: '/hr/clockRepair',
    method: 'put',
    data: data
  })
}

// 删除补卡
export function delHrClockRepair(clockRepairId) {
  return request({
    url: '/hr/clockRepair/' + clockRepairId,
    method: 'delete'
  })
}

// 补卡审批
export function auditHrClockRepair(clockRepairId, auditAction, auditRemark) {
  return request({
    url: '/hr/clockRepair/audit/' + clockRepairId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}
