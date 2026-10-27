import request from '@/utils/request'

// 查询班次列表
export function listHrShift(query) {
  return request({
    url: '/hr/shift/list',
    method: 'get',
    params: query
  })
}

// 查询班次详细
export function getHrShift(shiftId) {
  return request({
    url: '/hr/shift/' + shiftId,
    method: 'get'
  })
}

// 新增班次
export function addHrShift(data) {
  return request({
    url: '/hr/shift',
    method: 'post',
    data: data
  })
}

// 修改班次
export function updateHrShift(data) {
  return request({
    url: '/hr/shift',
    method: 'put',
    data: data
  })
}

// 删除班次
export function delHrShift(shiftId) {
  return request({
    url: '/hr/shift/' + shiftId,
    method: 'delete'
  })
}
