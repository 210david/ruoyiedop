import request from '@/utils/request'

// 查询在制品台账列表
export function listWip(query) {
  return request({ url: '/food/wip/list', method: 'get', params: query })
}

// 在制品详情（含温湿度记录）
export function getWip(wipId) {
  return request({ url: '/food/wip/' + wipId, method: 'get' })
}

// 新增建垛
export function addWip(data) {
  return request({ url: '/food/wip', method: 'post', data: data })
}

// 修改在制品
export function updateWip(data) {
  return request({ url: '/food/wip', method: 'put', data: data })
}

// 完成（出柜完工）
export function finishWip(wipId) {
  return request({ url: '/food/wip/finish/' + wipId, method: 'put' })
}

// 关闭
export function closeWip(wipId) {
  return request({ url: '/food/wip/close/' + wipId, method: 'put' })
}

// 删除在制品
export function delWip(wipIds) {
  return request({ url: '/food/wip/' + wipIds, method: 'delete' })
}

// 温湿度/上霉记录列表
export function listEnv(wipId) {
  return request({ url: '/food/wip/' + wipId + '/env', method: 'get' })
}

// 新增温湿度/上霉记录
export function addEnv(data) {
  return request({ url: '/food/wip/env', method: 'post', data: data })
}

// 删除温湿度/上霉记录
export function delEnv(envId) {
  return request({ url: '/food/wip/env/' + envId, method: 'delete' })
}

// WIP看板汇总（当前工序分布）
export function wipDashboard() {
  return request({ url: '/food/wip/dashboard', method: 'get' })
}
