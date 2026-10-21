import request from '@/utils/request'

// 查询职业健康体检列表
export function listHrHealthCheck(query) {
  return request({
    url: '/hr/healthCheck/list',
    method: 'get',
    params: query
  })
}

// 查询职业健康体检详细
export function getHrHealthCheck(healthId) {
  return request({
    url: '/hr/healthCheck/' + healthId,
    method: 'get'
  })
}

// 新增职业健康体检
export function addHrHealthCheck(data) {
  return request({
    url: '/hr/healthCheck',
    method: 'post',
    data: data
  })
}

// 修改职业健康体检
export function updateHrHealthCheck(data) {
  return request({
    url: '/hr/healthCheck',
    method: 'put',
    data: data
  })
}

// 删除职业健康体检
export function delHrHealthCheck(healthId) {
  return request({
    url: '/hr/healthCheck/' + healthId,
    method: 'delete'
  })
}
