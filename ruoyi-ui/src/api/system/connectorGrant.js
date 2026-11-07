import request from '@/utils/request'

// 管理员查询连接器授权列表
export function listGrant(query) {
  return request({
    url: '/edop/connector/auth/admin/list',
    method: 'get',
    params: query
  })
}

// 管理员强制吊销授权
export function revokeGrant(grantId) {
  return request({
    url: '/edop/connector/auth/admin/revoke/' + grantId,
    method: 'post'
  })
}

// 我的授权列表
export function listMyGrant() {
  return request({
    url: '/edop/connector/auth/my',
    method: 'get'
  })
}

// 授权登录：账号密码换取 grant_token（明文仅返回一次，网关兜底页场景）
export function createGrant(data) {
  return request({
    url: '/edop/connector/auth/login',
    method: 'post',
    data: data
  })
}

// 系统内一键授权：基于当前登录态直接颁发 grant_token（免二次输密码）
export function createMyGrant(data) {
  return request({
    url: '/edop/connector/auth/create',
    method: 'post',
    data: data
  })
}

// 获取连接器网关地址（授权页生成 MCP 配置用）
export function getConnectorConfig() {
  return request({
    url: '/edop/connector/auth/config',
    method: 'get'
  })
}

// 本人撤销授权
export function revokeMyGrant(grantId) {
  return request({
    url: '/edop/connector/auth/revoke/' + grantId,
    method: 'post'
  })
}
