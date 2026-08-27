import request from '@/utils/request'

// 查询岗位列表
export function listHrPosition(query) {
  return request({
    url: '/hr/position/list',
    method: 'get',
    params: query
  })
}

// 查询岗位详细
export function getHrPosition(positionId) {
  return request({
    url: '/hr/position/' + positionId,
    method: 'get'
  })
}

// 新增岗位
export function addHrPosition(data) {
  return request({
    url: '/hr/position',
    method: 'post',
    data: data
  })
}

// 修改岗位
export function updateHrPosition(data) {
  return request({
    url: '/hr/position',
    method: 'put',
    data: data
  })
}

// 删除岗位
export function delHrPosition(positionId) {
  return request({
    url: '/hr/position/' + positionId,
    method: 'delete'
  })
}
