import request from '@/utils/request'

// 查询计件报工列表
export function listHrPieceworkRecord(query) {
  return request({
    url: '/hr/pieceworkRecord/list',
    method: 'get',
    params: query
  })
}

// 查询计件报工详细
export function getHrPieceworkRecord(pwRecordId) {
  return request({
    url: '/hr/pieceworkRecord/' + pwRecordId,
    method: 'get'
  })
}

// 新增计件报工
export function addHrPieceworkRecord(data) {
  return request({
    url: '/hr/pieceworkRecord',
    method: 'post',
    data: data
  })
}

// 修改计件报工
export function updateHrPieceworkRecord(data) {
  return request({
    url: '/hr/pieceworkRecord',
    method: 'put',
    data: data
  })
}

// 删除计件报工
export function delHrPieceworkRecord(pwRecordId) {
  return request({
    url: '/hr/pieceworkRecord/' + pwRecordId,
    method: 'delete'
  })
}
