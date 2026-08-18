import request from '@/utils/request'

// 查询流转卡列表
export function listFlowCard(query) {
  return request({
    url: '/mms/flowcard/list',
    method: 'get',
    params: query
  })
}

// 查询流转卡详情
export function getFlowCard(cardId) {
  return request({
    url: '/mms/flowcard/' + cardId,
    method: 'get'
  })
}

// 新增流转卡
export function addFlowCard(data) {
  return request({
    url: '/mms/flowcard',
    method: 'post',
    data: data
  })
}

// 修改流转卡
export function updateFlowCard(data) {
  return request({
    url: '/mms/flowcard',
    method: 'put',
    data: data
  })
}

// 删除流转卡
export function delFlowCard(cardId) {
  return request({
    url: '/mms/flowcard/' + cardId,
    method: 'delete'
  })
}

// 打印流转卡
export function printFlowCard(cardId) {
  return request({
    url: '/mms/flowcard/print/' + cardId,
    method: 'put'
  })
}

// 扫码定位
export function scanFlowCard(cardCode) {
  return request({
    url: '/mms/flowcard/scan/' + cardCode,
    method: 'get'
  })
}
