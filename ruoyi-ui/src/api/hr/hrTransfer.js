import request from '@/utils/request'

// 查询调动列表
export function listHrTransfer(query) {
  return request({
    url: '/hr/transfer/list',
    method: 'get',
    params: query
  })
}

// 查询调动详细
export function getHrTransfer(transferId) {
  return request({
    url: '/hr/transfer/' + transferId,
    method: 'get'
  })
}

// 新增调动
export function addHrTransfer(data) {
  return request({
    url: '/hr/transfer',
    method: 'post',
    data: data
  })
}

// 修改调动
export function updateHrTransfer(data) {
  return request({
    url: '/hr/transfer',
    method: 'put',
    data: data
  })
}

// 删除调动
export function delHrTransfer(transferId) {
  return request({
    url: '/hr/transfer/' + transferId,
    method: 'delete'
  })
}

// 调动审批
export function auditHrTransfer(transferId, auditAction, auditRemark) {
  return request({
    url: '/hr/transfer/audit/' + transferId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}
