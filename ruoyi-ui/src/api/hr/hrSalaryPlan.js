import request from '@/utils/request'

// 查询定薪调薪列表
export function listHrSalaryPlan(query) {
  return request({
    url: '/hr/salaryPlan/list',
    method: 'get',
    params: query
  })
}

// 查询定薪调薪详细
export function getHrSalaryPlan(planId) {
  return request({
    url: '/hr/salaryPlan/' + planId,
    method: 'get'
  })
}

// 新增定薪调薪
export function addHrSalaryPlan(data) {
  return request({
    url: '/hr/salaryPlan',
    method: 'post',
    data: data
  })
}

// 修改定薪调薪
export function updateHrSalaryPlan(data) {
  return request({
    url: '/hr/salaryPlan',
    method: 'put',
    data: data
  })
}

// 删除定薪调薪
export function delHrSalaryPlan(planId) {
  return request({
    url: '/hr/salaryPlan/' + planId,
    method: 'delete'
  })
}
