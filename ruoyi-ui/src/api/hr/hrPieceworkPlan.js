import request from '@/utils/request'

// 查询计件方案列表
export function listHrPieceworkPlan(query) {
  return request({
    url: '/hr/pieceworkPlan/list',
    method: 'get',
    params: query
  })
}

// 查询计件方案详细
export function getHrPieceworkPlan(pwPlanId) {
  return request({
    url: '/hr/pieceworkPlan/' + pwPlanId,
    method: 'get'
  })
}

// 新增计件方案
export function addHrPieceworkPlan(data) {
  return request({
    url: '/hr/pieceworkPlan',
    method: 'post',
    data: data
  })
}

// 修改计件方案
export function updateHrPieceworkPlan(data) {
  return request({
    url: '/hr/pieceworkPlan',
    method: 'put',
    data: data
  })
}

// 删除计件方案
export function delHrPieceworkPlan(pwPlanId) {
  return request({
    url: '/hr/pieceworkPlan/' + pwPlanId,
    method: 'delete'
  })
}
