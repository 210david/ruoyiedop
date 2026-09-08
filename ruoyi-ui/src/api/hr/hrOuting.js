import request from '@/utils/request'

// 查询外出列表
export function listHrOuting(query) {
  return request({
    url: '/hr/outing/list',
    method: 'get',
    params: query
  })
}

// 查询外出详细
export function getHrOuting(outingId) {
  return request({
    url: '/hr/outing/' + outingId,
    method: 'get'
  })
}

// 新增外出
export function addHrOuting(data) {
  return request({
    url: '/hr/outing',
    method: 'post',
    data: data
  })
}

// 修改外出
export function updateHrOuting(data) {
  return request({
    url: '/hr/outing',
    method: 'put',
    data: data
  })
}

// 删除外出
export function delHrOuting(outingId) {
  return request({
    url: '/hr/outing/' + outingId,
    method: 'delete'
  })
}

// 外出审批
export function auditHrOuting(outingId, auditAction, auditRemark) {
  return request({
    url: '/hr/outing/audit/' + outingId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}
