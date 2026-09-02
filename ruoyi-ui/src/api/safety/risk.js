import request from '@/utils/request'

// 查询风险点列表
export function listRiskPoint(query) {
  return request({
    url: '/safety/risk/list',
    method: 'get',
    params: query
  })
}

// 查询风险点详情
export function getRiskPoint(riskPointId) {
  return request({
    url: '/safety/risk/' + riskPointId,
    method: 'get'
  })
}

// 新增风险点
export function addRiskPoint(data) {
  return request({
    url: '/safety/risk',
    method: 'post',
    data: data
  })
}

// 修改风险点
export function updateRiskPoint(data) {
  return request({
    url: '/safety/risk',
    method: 'put',
    data: data
  })
}

// 删除风险点
export function delRiskPoint(riskPointId) {
  return request({
    url: '/safety/risk/' + riskPointId,
    method: 'delete'
  })
}

// 获取四色图数据
export function getFourColorMap(enterpriseId) {
  return request({
    url: '/safety/risk/fourColorMap',
    method: 'get',
    params: { enterpriseId }
  })
}

// 导出风险点
export function exportRiskPoint(query) {
  return request({
    url: '/safety/risk/export',
    method: 'post',
    params: query
  })
}