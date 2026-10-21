import request from '@/utils/request'

// 查询计划列表
export function listMps(query) {
  return request({
    url: '/mms/mps/list',
    method: 'get',
    params: query
  })
}

// 查询计划详情
export function getMps(mpsId) {
  return request({
    url: '/mms/mps/' + mpsId,
    method: 'get'
  })
}

// 新增计划
export function addMps(data) {
  return request({
    url: '/mms/mps',
    method: 'post',
    data: data
  })
}

// 修改计划
export function updateMps(data) {
  return request({
    url: '/mms/mps',
    method: 'put',
    data: data
  })
}

// 删除计划
export function delMps(mpsId) {
  return request({
    url: '/mms/mps/' + mpsId,
    method: 'delete'
  })
}

// 计划提交审批
export function submitMps(mpsId) {
  return request({
    url: '/mms/mps/submit/' + mpsId,
    method: 'put'
  })
}

// 计划审批
export function auditMps(mpsId, status, auditOpinion) {
  return request({
    url: '/mms/mps/audit/' + mpsId,
    method: 'put',
    params: { status, auditOpinion }
  })
}

// 计划发布（下达生成工单）
export function releaseMps(mpsId) {
  return request({
    url: '/mms/mps/release/' + mpsId,
    method: 'put'
  })
}

// 计划取消
export function cancelMps(mpsId, cancelReason) {
  return request({
    url: '/mms/mps/cancel/' + mpsId,
    method: 'put',
    params: { cancelReason }
  })
}
