import request from '@/utils/request'

// 查询员工档案列表
export function listHrEmployee(query) {
  return request({
    url: '/hr/employee/list',
    method: 'get',
    params: query
  })
}

// 查询员工档案详细
export function getHrEmployee(employeeId) {
  return request({
    url: '/hr/employee/' + employeeId,
    method: 'get'
  })
}

// 新增员工档案
export function addHrEmployee(data) {
  return request({
    url: '/hr/employee',
    method: 'post',
    data: data
  })
}

// 修改员工档案
export function updateHrEmployee(data) {
  return request({
    url: '/hr/employee',
    method: 'put',
    data: data
  })
}

// 删除员工档案
export function delHrEmployee(employeeId) {
  const ids = Array.isArray(employeeId) ? employeeId.join(',') : employeeId;
  return request({
    url: '/hr/employee/' + ids,
    method: 'delete'
  })
}
