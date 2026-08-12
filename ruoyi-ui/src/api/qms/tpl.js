import request from '@/utils/request'

export function listTpl(query) {
  return request({
    url: '/qms/tpl/list',
    method: 'get',
    params: query
  })
}

export function getTpl(tplId) {
  return request({
    url: '/qms/tpl/' + tplId,
    method: 'get'
  })
}

export function addTpl(data) {
  return request({
    url: '/qms/tpl',
    method: 'post',
    data: data
  })
}

export function updateTpl(data) {
  return request({
    url: '/qms/tpl',
    method: 'put',
    data: data
  })
}

export function delTpl(tplId) {
  return request({
    url: '/qms/tpl/' + tplId,
    method: 'delete'
  })
}
