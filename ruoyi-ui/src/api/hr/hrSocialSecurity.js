import request from '@/utils/request'

// 查询社保公积金列表
export function listHrSocialSecurity(query) {
  return request({
    url: '/hr/socialSecurity/list',
    method: 'get',
    params: query
  })
}

// 查询社保公积金详细
export function getHrSocialSecurity(ssId) {
  return request({
    url: '/hr/socialSecurity/' + ssId,
    method: 'get'
  })
}

// 新增社保公积金
export function addHrSocialSecurity(data) {
  return request({
    url: '/hr/socialSecurity',
    method: 'post',
    data: data
  })
}

// 修改社保公积金
export function updateHrSocialSecurity(data) {
  return request({
    url: '/hr/socialSecurity',
    method: 'put',
    data: data
  })
}

// 删除社保公积金
export function delHrSocialSecurity(ssId) {
  return request({
    url: '/hr/socialSecurity/' + ssId,
    method: 'delete'
  })
}
