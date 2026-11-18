import request from '@/utils/request'

// 查询加班列表
export function listHrOvertime(query) {
  return request({
    url: '/hr/overtime/list',
    method: 'get',
    params: query
  })
}

// 查询加班详细
export function getHrOvertime(overtimeId) {
  return request({
    url: '/hr/overtime/' + overtimeId,
    method: 'get'
  })
}

// 新增加班
export function addHrOvertime(data) {
  return request({
    url: '/hr/overtime',
    method: 'post',
    data: data
  })
}

// 修改加班
export function updateHrOvertime(data) {
  return request({
    url: '/hr/overtime',
    method: 'put',
    data: data
  })
}

// 删除加班
export function delHrOvertime(overtimeId) {
  return request({
    url: '/hr/overtime/' + overtimeId,
    method: 'delete'
  })
}
