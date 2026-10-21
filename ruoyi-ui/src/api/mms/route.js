import request from '@/utils/request'

export function listRoute(query) {
  return request({
    url: '/mms/route/list',
    method: 'get',
    params: query
  })
}

export function getRoute(routeId) {
  return request({
    url: '/mms/route/' + routeId,
    method: 'get'
  })
}

export function addRoute(data) {
  return request({
    url: '/mms/route',
    method: 'post',
    data: data
  })
}

export function updateRoute(data) {
  return request({
    url: '/mms/route',
    method: 'put',
    data: data
  })
}

export function delRoute(routeId) {
  return request({
    url: '/mms/route/' + routeId,
    method: 'delete'
  })
}

/** 启用路线（草稿→已启用） */
export function enableRoute(routeId) {
  return request({
    url: '/mms/route/enable/' + routeId,
    method: 'put'
  })
}

/** 审核路线（已启用→已审核/已驳回） */
export function auditRoute(routeId, auditAction, auditRemark) {
  return request({
    url: '/mms/route/audit/' + routeId,
    method: 'put',
    params: { auditAction, auditRemark }
  })
}

/** 停用路线（→已停用） */
export function disableRoute(routeId) {
  return request({
    url: '/mms/route/disable/' + routeId,
    method: 'put'
  })
}

/** 复制路线为新版本 */
export function copyRoute(routeId) {
  return request({
    url: '/mms/route/copy/' + routeId,
    method: 'post'
  })
}

/** 版本变更记录 */
export function getRouteVersionLog(routeId) {
  return request({
    url: '/mms/route/log/' + routeId,
    method: 'get'
  })
}
