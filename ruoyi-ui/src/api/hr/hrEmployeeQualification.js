import request from '@/utils/request'

// 查询员工培训/职称/资格认证列表
export function listHrEmployeeQualification(query) {
  return request({
    url: '/hr/employeequalification/list',
    method: 'get',
    params: query
  })
}

// 查询员工培训/职称/资格认证详细
export function getHrEmployeeQualification(qualificationId) {
  return request({
    url: '/hr/employeequalification/' + qualificationId,
    method: 'get'
  })
}

// 新增员工培训/职称/资格认证
export function addHrEmployeeQualification(data) {
  return request({
    url: '/hr/employeequalification',
    method: 'post',
    data: data
  })
}

// 修改员工培训/职称/资格认证
export function updateHrEmployeeQualification(data) {
  return request({
    url: '/hr/employeequalification',
    method: 'put',
    data: data
  })
}

// 删除员工培训/职称/资格认证
export function delHrEmployeeQualification(qualificationId) {
  return request({
    url: '/hr/employeequalification/' + qualificationId,
    method: 'delete'
  })
}
