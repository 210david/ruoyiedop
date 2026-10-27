import request from '@/utils/request'

// 查询电子签名记录列表
export function listEsigRecord(query) {
  return request({
    url: '/qms/esig/list',
    method: 'get',
    params: query
  })
}

// 查询业务关联的签名记录
export function listEsigByBiz(bizType, bizId) {
  return request({
    url: '/qms/esig/byBiz/' + bizType + '/' + bizId,
    method: 'get'
  })
}

// 详情
export function getEsigRecord(esigId) {
  return request({
    url: '/qms/esig/' + esigId,
    method: 'get'
  })
}

// 新增
export function addEsigRecord(data) {
  return request({
    url: '/qms/esig',
    method: 'post',
    data: data
  })
}

// 删除
export function delEsigRecord(esigIds) {
  return request({
    url: '/qms/esig/' + esigIds,
    method: 'delete'
  })
}