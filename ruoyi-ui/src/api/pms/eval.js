import request from '@/utils/request'

export function listEval(query) {
  return request({
    url: '/pms/eval/list',
    method: 'get',
    params: query
  })
}

export function getEval(evalId) {
  return request({
    url: '/pms/eval/' + evalId,
    method: 'get'
  })
}

export function addEval(data) {
  return request({
    url: '/pms/eval',
    method: 'post',
    data: data
  })
}

export function updateEval(data) {
  return request({
    url: '/pms/eval',
    method: 'put',
    data: data
  })
}

export function delEval(evalId) {
  return request({
    url: '/pms/eval/' + evalId,
    method: 'delete'
  })
}

// 审核供应商评价
export function auditEval(evalId, status, auditRemark) {
  return request({
    url: '/pms/eval/audit/' + evalId,
    method: 'put',
    params: { status, auditRemark }
  })
}
