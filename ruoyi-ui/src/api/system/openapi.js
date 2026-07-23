import request from '@/utils/request'

// 查询开放接口应用列表
export function listOpenApi(query) {
  return request({
    url: '/system/openapi/list',
    method: 'get',
    params: query
  })
}

// 查询开放接口应用详细
export function getOpenApi(appId) {
  return request({
    url: '/system/openapi/' + appId,
    method: 'get'
  })
}

// 新增开放接口应用
export function addOpenApi(data) {
  return request({
    url: '/system/openapi',
    method: 'post',
    data: data
  })
}

// 修改开放接口应用
export function updateOpenApi(data) {
  return request({
    url: '/system/openapi',
    method: 'put',
    data: data
  })
}

// 删除开放接口应用
export function delOpenApi(appId) {
  return request({
    url: '/system/openapi/' + appId,
    method: 'delete'
  })
}
