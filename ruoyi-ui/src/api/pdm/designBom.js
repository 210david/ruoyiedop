import request from '@/utils/request'

// 查询设计BOM列表
export function listDesignBom(query) {
  return request({ url: '/pdm/designBom/list', method: 'get', params: query })
}

// 查询BOM详情（含明细）
export function getDesignBom(bomId) {
  return request({ url: '/pdm/designBom/' + bomId, method: 'get' })
}

// 查询BOM明细
export function listBomItems(bomId) {
  return request({ url: '/pdm/designBom/items/' + bomId, method: 'get' })
}

// 新增设计BOM
export function addDesignBom(data) {
  return request({ url: '/pdm/designBom', method: 'post', data: data })
}

// 修改设计BOM（含明细全量替换）
export function updateDesignBom(data) {
  return request({ url: '/pdm/designBom', method: 'put', data: data })
}

// BOM生效
export function effectiveDesignBom(data) {
  return request({ url: '/pdm/designBom/effective', method: 'put', data: data })
}

// 手工升版
export function upgradeDesignBom(data) {
  return request({ url: '/pdm/designBom/upgrade', method: 'put', data: data })
}

// 删除设计BOM
export function delDesignBom(bomIds) {
  return request({ url: '/pdm/designBom/' + bomIds, method: 'delete' })
}

// 生效BOM一键下发转换为制造BOM（mms接收）
export function releaseMbom(bomId) {
  return request({ url: '/pdm/mbomRelease/release/' + bomId, method: 'post' })
}

// 查询BOM下发记录（EBOM→MBOM版本对应关系台账）
export function listRelease(query) {
  return request({ url: '/pdm/mbomRelease/list', method: 'get', params: query })
}
