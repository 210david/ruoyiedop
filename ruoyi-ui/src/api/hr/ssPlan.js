import request from '@/utils/request'

// 查询社保方案列表
export function listSsPlan(query) {
  return request({ url: '/hr/ssPlan/list', method: 'get', params: query })
}

// 查询社保方案详情
export function getSsPlan(planId) {
  return request({ url: '/hr/ssPlan/' + planId, method: 'get' })
}

// 查询指定年度默认方案
export function getDefaultSsPlan(year) {
  return request({ url: '/hr/ssPlan/default/' + year, method: 'get' })
}

// 新增社保方案
export function addSsPlan(data) {
  return request({ url: '/hr/ssPlan', method: 'post', data })
}

// 修改社保方案
export function updateSsPlan(data) {
  return request({ url: '/hr/ssPlan', method: 'put', data })
}

// 删除社保方案
export function delSsPlan(planId) {
  return request({ url: '/hr/ssPlan/' + planId, method: 'delete' })
}

// 复制方案到目标年度
export function copySsPlan(planId, targetYear) {
  return request({ url: `/hr/ssPlan/copy/${planId}/${targetYear}`, method: 'post' })
}

// 批量初始化员工社保记录（按年度默认方案）
export function batchInitSs(year) {
  return request({ url: '/hr/socialSecurity/batchInit/' + year, method: 'post' })
}
