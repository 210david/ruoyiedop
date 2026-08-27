import request from '@/utils/request'

// 查询员工工作经历列表
export function listHrEmployeeWorkExp(query) {
  return request({
    url: '/hr/employeeworkexp/list',
    method: 'get',
    params: query
  })
}

// 查询员工工作经历详细
export function getHrEmployeeWorkExp(workExpId) {
  return request({
    url: '/hr/employeeworkexp/' + workExpId,
    method: 'get'
  })
}

// 新增员工工作经历
export function addHrEmployeeWorkExp(data) {
  return request({
    url: '/hr/employeeworkexp',
    method: 'post',
    data: data
  })
}

// 修改员工工作经历
export function updateHrEmployeeWorkExp(data) {
  return request({
    url: '/hr/employeeworkexp',
    method: 'put',
    data: data
  })
}

// 删除员工工作经历
export function delHrEmployeeWorkExp(workExpId) {
  return request({
    url: '/hr/employeeworkexp/' + workExpId,
    method: 'delete'
  })
}
