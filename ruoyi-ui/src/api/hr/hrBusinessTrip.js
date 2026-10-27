import request from '@/utils/request'

// 查询出差列表
export function listHrBusinessTrip(query) {
  return request({
    url: '/hr/businessTrip/list',
    method: 'get',
    params: query
  })
}

// 查询出差详细
export function getHrBusinessTrip(tripId) {
  return request({
    url: '/hr/businessTrip/' + tripId,
    method: 'get'
  })
}

// 新增出差
export function addHrBusinessTrip(data) {
  return request({
    url: '/hr/businessTrip',
    method: 'post',
    data: data
  })
}

// 修改出差
export function updateHrBusinessTrip(data) {
  return request({
    url: '/hr/businessTrip',
    method: 'put',
    data: data
  })
}

// 删除出差
export function delHrBusinessTrip(tripId) {
  return request({
    url: '/hr/businessTrip/' + tripId,
    method: 'delete'
  })
}

// 出差审批
export function auditHrBusinessTrip(tripId, auditAction, auditRemark) {
  return request({
    url: '/hr/businessTrip/audit/' + tripId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}
