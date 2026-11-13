import request from '@/utils/request'

// 查询指标字典列表
export function listMetric(query) {
  return request({
    url: '/bi/metric/list',
    method: 'get',
    params: query
  })
}

// 查询指标字典详情
export function getMetric(id) {
  return request({
    url: '/bi/metric/' + id,
    method: 'get'
  })
}

// 新增指标字典
export function addMetric(data) {
  return request({
    url: '/bi/metric',
    method: 'post',
    data: data
  })
}

// 修改指标字典
export function updateMetric(data) {
  return request({
    url: '/bi/metric',
    method: 'put',
    data: data
  })
}

// 删除指标字典
export function delMetric(ids) {
  return request({
    url: '/bi/metric/' + ids,
    method: 'delete'
  })
}

// 指标口径变更留痕
export function listChangeLog(metricCode) {
  return request({
    url: '/bi/metric/changeLog/' + metricCode,
    method: 'get'
  })
}

// 对账日志查询
export function listCheckLog(query) {
  return request({
    url: '/bi/metric/checkLog',
    method: 'get',
    params: query
  })
}

// F7 预警记录查询
export function listAlert(query) {
  return request({
    url: '/bi/metric/alert/list',
    method: 'get',
    params: query
  })
}
