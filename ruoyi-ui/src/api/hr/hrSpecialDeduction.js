import request from '@/utils/request'

// 查询专项附加扣除列表
export function listHrSpecialDeduction(query) {
  return request({
    url: '/hr/specialDeduction/list',
    method: 'get',
    params: query
  })
}

// 查询专项附加扣除详细
export function getHrSpecialDeduction(sdId) {
  return request({
    url: '/hr/specialDeduction/' + sdId,
    method: 'get'
  })
}

// 新增专项附加扣除
export function addHrSpecialDeduction(data) {
  return request({
    url: '/hr/specialDeduction',
    method: 'post',
    data: data
  })
}

// 修改专项附加扣除
export function updateHrSpecialDeduction(data) {
  return request({
    url: '/hr/specialDeduction',
    method: 'put',
    data: data
  })
}

// 删除专项附加扣除
export function delHrSpecialDeduction(sdId) {
  return request({
    url: '/hr/specialDeduction/' + sdId,
    method: 'delete'
  })
}
