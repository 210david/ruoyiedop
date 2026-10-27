import request from '@/utils/request'

// 查询设计方案列表
export function listScheme(query) {
  return request({ url: '/pdm/scheme/list', method: 'get', params: query })
}

// 查询方案详情（含配方明细，按权限返回）
export function getScheme(schemeId) {
  return request({ url: '/pdm/scheme/' + schemeId, method: 'get' })
}

// 新增设计方案
export function addScheme(data) {
  return request({ url: '/pdm/scheme', method: 'post', data: data })
}

// 修改设计方案
export function updateScheme(data) {
  return request({ url: '/pdm/scheme', method: 'put', data: data })
}

// 方案定稿（版本生效 + 产品定型联动）
export function effectiveScheme(data) {
  return request({ url: '/pdm/scheme/effective', method: 'put', data: data })
}

// 方案升版
export function upgradeScheme(data) {
  return request({ url: '/pdm/scheme/upgrade', method: 'put', data: data })
}

// 删除设计方案
export function delScheme(schemeIds) {
  return request({ url: '/pdm/scheme/' + schemeIds, method: 'delete' })
}

// 停用方案（生命周期终止动作，不走变更单）
export function disableScheme(schemeId, remark) {
  return request({ url: '/pdm/scheme/disable/' + schemeId, method: 'put', data: { remark } })
}

// 恢复方案（已停用 → 已定稿）
export function enableScheme(schemeId) {
  return request({ url: '/pdm/scheme/enable/' + schemeId, method: 'put' })
}
