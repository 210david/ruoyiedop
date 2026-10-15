import request from '@/utils/request'

// 查询物料行业参数列表
export function listParam(query) {
  return request({ url: '/food/materialParam/list', method: 'get', params: query })
}

// 行业参数详情
export function getParam(paramId) {
  return request({ url: '/food/materialParam/' + paramId, method: 'get' })
}

// 按物料查询行业参数（未配置返回null）
export function getParamByMaterial(materialId) {
  return request({ url: '/food/materialParam/material/' + materialId, method: 'get' })
}

// 新增行业参数
export function addParam(data) {
  return request({ url: '/food/materialParam', method: 'post', data: data })
}

// 修改行业参数
export function updateParam(data) {
  return request({ url: '/food/materialParam', method: 'put', data: data })
}

// 删除行业参数
export function delParam(paramIds) {
  return request({ url: '/food/materialParam/' + paramIds, method: 'delete' })
}
