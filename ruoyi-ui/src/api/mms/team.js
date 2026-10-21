import request from '@/utils/request'

export function listTeam(query) {
  return request({
    url: '/mms/team/list',
    method: 'get',
    params: query
  })
}

export function getTeam(teamId) {
  return request({
    url: '/mms/team/' + teamId,
    method: 'get'
  })
}

export function addTeam(data) {
  return request({
    url: '/mms/team',
    method: 'post',
    data: data
  })
}

export function updateTeam(data) {
  return request({
    url: '/mms/team',
    method: 'put',
    data: data
  })
}

export function delTeam(teamId) {
  return request({
    url: '/mms/team/' + teamId,
    method: 'delete'
  })
}
