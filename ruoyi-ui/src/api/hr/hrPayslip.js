import request from '@/utils/request'

// 查询工资条列表
export function listHrPayslip(query) {
  return request({
    url: '/hr/payslip/list',
    method: 'get',
    params: query
  })
}

// 查询工资条详细
export function getHrPayslip(payslipId) {
  return request({
    url: '/hr/payslip/' + payslipId,
    method: 'get'
  })
}

// 新增工资条
export function addHrPayslip(data) {
  return request({
    url: '/hr/payslip',
    method: 'post',
    data: data
  })
}

// 修改工资条
export function updateHrPayslip(data) {
  return request({
    url: '/hr/payslip',
    method: 'put',
    data: data
  })
}

// 删除工资条
export function delHrPayslip(payslipId) {
  return request({
    url: '/hr/payslip/' + payslipId,
    method: 'delete'
  })
}
