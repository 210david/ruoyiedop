import request from '@/utils/request'

// 查询员工家庭成员列表
export function listHrEmployeeFamily(query) {
  return request({
    url: '/hr/employeefamily/list',
    method: 'get',
    params: query
  })
}

// 查询员工家庭成员详细
export function getHrEmployeeFamily(familyId) {
  return request({
    url: '/hr/employeefamily/' + familyId,
    method: 'get'
  })
}

// 新增员工家庭成员
export function addHrEmployeeFamily(data) {
  return request({
    url: '/hr/employeefamily',
    method: 'post',
    data: data
  })
}

// 修改员工家庭成员
export function updateHrEmployeeFamily(data) {
  return request({
    url: '/hr/employeefamily',
    method: 'put',
    data: data
  })
}

// 删除员工家庭成员
export function delHrEmployeeFamily(familyId) {
  return request({
    url: '/hr/employeefamily/' + familyId,
    method: 'delete'
  })
}
