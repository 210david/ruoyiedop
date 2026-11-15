import request from '@/utils/request'

// 查询印章台账列表
export function listOaSeal(query) {
  return request({
    url: '/oa/seal/list',
    method: 'get',
    params: query
  })
}

// 查询印章台账详细
export function getOaSeal(sealId) {
  return request({
    url: '/oa/seal/' + sealId,
    method: 'get'
  })
}

// 新增印章台账
export function addOaSeal(data) {
  return request({
    url: '/oa/seal',
    method: 'post',
    data: data
  })
}

// 修改印章台账
export function updateOaSeal(data) {
  return request({
    url: '/oa/seal',
    method: 'put',
    data: data
  })
}

// 删除印章台账
export function delOaSeal(sealId) {
  return request({
    url: '/oa/seal/' + sealId,
    method: 'delete'
  })
}
