import request from '@/utils/request'

// 查询薪酬发放列表
export function listHrPayrollBatch(query) {
  return request({
    url: '/hr/payroll/list',
    method: 'get',
    params: query
  })
}

// 查询薪酬发放详细
export function getHrPayrollBatch(batchId) {
  return request({
    url: '/hr/payroll/' + batchId,
    method: 'get'
  })
}

// 新增薪酬发放
export function addHrPayrollBatch(data) {
  return request({
    url: '/hr/payroll',
    method: 'post',
    data: data
  })
}

// 修改薪酬发放
export function updateHrPayrollBatch(data) {
  return request({
    url: '/hr/payroll',
    method: 'put',
    data: data
  })
}

// 删除薪酬发放
export function delHrPayrollBatch(batchId) {
  return request({
    url: '/hr/payroll/' + batchId,
    method: 'delete'
  })
}
