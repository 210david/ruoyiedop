import request from '@/utils/request'

// 查询编号规则列表
export function listCodeRule(query) {
  return request({
    url: '/mms/numberRule/list',
    method: 'get',
    params: query
  })
}

// 查询编号规则详细
export function getCodeRule(ruleId) {
  return request({
    url: '/mms/numberRule/' + ruleId,
    method: 'get'
  })
}

// 根据规则编码获取预览编号
export function previewCodeRule(ruleCode) {
  return request({
    url: '/mms/numberRule/preview/' + ruleCode,
    method: 'get'
  })
}

// 新增编号规则
export function addCodeRule(data) {
  return request({
    url: '/mms/numberRule',
    method: 'post',
    data: data
  })
}

// 修改编号规则
export function updateCodeRule(data) {
  return request({
    url: '/mms/numberRule',
    method: 'put',
    data: data
  })
}

// 删除编号规则
export function delCodeRule(ruleId) {
  return request({
    url: '/mms/numberRule/' + ruleId,
    method: 'delete'
  })
}
