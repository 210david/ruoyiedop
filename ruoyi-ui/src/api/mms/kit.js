import request from '@/utils/request'

// 查询齐套检查列表
export function listKit(query) {
  return request({
    url: '/mms/kit/list',
    method: 'get',
    params: query
  })
}

// 查询齐套检查详情
export function getKitCheck(kitId) {
  return request({
    url: '/mms/kit/' + kitId,
    method: 'get'
  })
}

// 新增齐套检查
export function addKitCheck(data) {
  return request({
    url: '/mms/kit',
    method: 'post',
    data: data
  })
}

// 修改齐套检查
export function updateKitCheck(data) {
  return request({
    url: '/mms/kit',
    method: 'put',
    data: data
  })
}

// 删除齐套检查
export function delKitCheck(kitId) {
  return request({
    url: '/mms/kit/' + kitId,
    method: 'delete'
  })
}

// 执行齐套检查
export function executeKitCheck(kitId) {
  return request({
    url: '/mms/kit/execute/' + kitId,
    method: 'put'
  })
}

// 查询齐套检查明细
export function getKitCheckDetail(kitId) {
  return request({
    url: '/mms/kit/detail/' + kitId,
    method: 'get'
  })
}

// 一键齐套检查（根据工单ID）
export function autoKitCheckByWorkOrderId(workOrderId) {
  return request({
    url: '/mms/kit/autoCheck/' + workOrderId,
    method: 'post'
  })
}

// 一键齐套检查（根据工单号）
export function autoKitCheckByWorkOrderNo(workOrderNo) {
  return request({
    url: '/mms/kit/autoCheckByNo/' + workOrderNo,
    method: 'post'
  })
}

// 批量一键齐套检查
export function batchAutoKitCheck(workOrderIds) {
  return request({
    url: '/mms/kit/batchAutoCheck',
    method: 'post',
    data: workOrderIds
  })
}
