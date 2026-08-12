import request from '@/utils/request'

// 查询隐患列表
export function listHazard(query) {
  return request({
    url: '/safety/hazard/list',
    method: 'get',
    params: query
  })
}

// 查询隐患详情
export function getHazard(hazardId) {
  return request({
    url: '/safety/hazard/' + hazardId,
    method: 'get'
  })
}

// 新增隐患
export function addHazard(data) {
  return request({
    url: '/safety/hazard',
    method: 'post',
    data: data
  })
}

// 修改隐患
export function updateHazard(data) {
  return request({
    url: '/safety/hazard',
    method: 'put',
    data: data
  })
}

// 删除隐患
export function delHazard(hazardId) {
  return request({
    url: '/safety/hazard/' + hazardId,
    method: 'delete'
  })
}

// 提交隐患
export function submitHazard(hazardId) {
  return request({
    url: '/safety/hazard/submit/' + hazardId,
    method: 'put'
  })
}

// 审批隐患
export function approveHazard(data) {
  return request({
    url: '/safety/hazard/approve',
    method: 'put',
    data: data
  })
}

// 开始整改
export function startRectify(hazardId) {
  return request({
    url: '/safety/hazard/start/' + hazardId,
    method: 'put'
  })
}

// 提交整改
export function submitRectify(data) {
  return request({
    url: '/safety/hazard/rectify',
    method: 'put',
    data: data
  })
}

// 验收隐患
export function verifyHazard(data) {
  return request({
    url: '/safety/hazard/verify',
    method: 'put',
    data: data
  })
}

// 导出隐患
export function exportHazard(query) {
  return request({
    url: '/safety/hazard/export',
    method: 'post',
    params: query
  })
}