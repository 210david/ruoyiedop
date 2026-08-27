import request from '@/utils/request'

// 查询临时工日结列表
export function listHrTempDailySettlement(query) {
  return request({
    url: '/hr/tempSettlement/list',
    method: 'get',
    params: query
  })
}

// 查询临时工日结详细
export function getHrTempDailySettlement(settlementId) {
  return request({
    url: '/hr/tempSettlement/' + settlementId,
    method: 'get'
  })
}

// 新增临时工日结
export function addHrTempDailySettlement(data) {
  return request({
    url: '/hr/tempSettlement',
    method: 'post',
    data: data
  })
}

// 修改临时工日结
export function updateHrTempDailySettlement(data) {
  return request({
    url: '/hr/tempSettlement',
    method: 'put',
    data: data
  })
}

// 删除临时工日结
export function delHrTempDailySettlement(settlementId) {
  return request({
    url: '/hr/tempSettlement/' + settlementId,
    method: 'delete'
  })
}
