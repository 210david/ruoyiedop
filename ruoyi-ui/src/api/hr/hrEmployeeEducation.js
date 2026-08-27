import request from '@/utils/request'

// 查询员工教育背景列表
export function listHrEmployeeEducation(query) {
  return request({
    url: '/hr/employeeeducation/list',
    method: 'get',
    params: query
  })
}

// 查询员工教育背景详细
export function getHrEmployeeEducation(educationId) {
  return request({
    url: '/hr/employeeeducation/' + educationId,
    method: 'get'
  })
}

// 新增员工教育背景
export function addHrEmployeeEducation(data) {
  return request({
    url: '/hr/employeeeducation',
    method: 'post',
    data: data
  })
}

// 修改员工教育背景
export function updateHrEmployeeEducation(data) {
  return request({
    url: '/hr/employeeeducation',
    method: 'put',
    data: data
  })
}

// 删除员工教育背景
export function delHrEmployeeEducation(educationId) {
  return request({
    url: '/hr/employeeeducation/' + educationId,
    method: 'delete'
  })
}
