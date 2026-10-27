import request from '@/utils/request'

// 查询转正列表
export function listHrRegular(query) {
  return request({
    url: '/hr/regular/list',
    method: 'get',
    params: query
  })
}

// 查询转正详细
export function getHrRegular(regularId) {
  return request({
    url: '/hr/regular/' + regularId,
    method: 'get'
  })
}

// 新增转正
export function addHrRegular(data) {
  return request({
    url: '/hr/regular',
    method: 'post',
    data: data
  })
}

// 修改转正
export function updateHrRegular(data) {
  return request({
    url: '/hr/regular',
    method: 'put',
    data: data
  })
}

// 删除转正
export function delHrRegular(regularId) {
  return request({
    url: '/hr/regular/' + regularId,
    method: 'delete'
  })
}

// 转正审批
export function auditHrRegular(regularId, auditAction, auditRemark) {
  return request({
    url: '/hr/regular/audit/' + regularId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}
