import request from '@/utils/request'

// 查询薪资项字典列表
export function listHrSalaryItem(query) {
  return request({
    url: '/hr/salaryItem/list',
    method: 'get',
    params: query
  })
}

// 查询薪资项字典详细
export function getHrSalaryItem(itemId) {
  return request({
    url: '/hr/salaryItem/' + itemId,
    method: 'get'
  })
}

// 新增薪资项字典
export function addHrSalaryItem(data) {
  return request({
    url: '/hr/salaryItem',
    method: 'post',
    data: data
  })
}

// 修改薪资项字典
export function updateHrSalaryItem(data) {
  return request({
    url: '/hr/salaryItem',
    method: 'put',
    data: data
  })
}

// 删除薪资项字典
export function delHrSalaryItem(itemId) {
  return request({
    url: '/hr/salaryItem/' + itemId,
    method: 'delete'
  })
}
