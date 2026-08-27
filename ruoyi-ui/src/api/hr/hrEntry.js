import request from '@/utils/request'

// 查询入职列表
export function listHrEntry(query) {
  return request({
    url: '/hr/entry/list',
    method: 'get',
    params: query
  })
}

// 查询入职详细
export function getHrEntry(entryId) {
  return request({
    url: '/hr/entry/' + entryId,
    method: 'get'
  })
}

// 新增入职
export function addHrEntry(data) {
  return request({
    url: '/hr/entry',
    method: 'post',
    data: data
  })
}

// 修改入职
export function updateHrEntry(data) {
  return request({
    url: '/hr/entry',
    method: 'put',
    data: data
  })
}

// 删除入职
export function delHrEntry(entryId) {
  return request({
    url: '/hr/entry/' + entryId,
    method: 'delete'
  })
}
