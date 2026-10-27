import request from '@/utils/request'

// 查询劳动合同列表
export function listHrContract(query) {
  return request({
    url: '/hr/contract/list',
    method: 'get',
    params: query
  })
}

// 查询劳动合同详细
export function getHrContract(contractId) {
  return request({
    url: '/hr/contract/' + contractId,
    method: 'get'
  })
}

// 新增劳动合同
export function addHrContract(data) {
  return request({
    url: '/hr/contract',
    method: 'post',
    data: data
  })
}

// 修改劳动合同
export function updateHrContract(data) {
  return request({
    url: '/hr/contract',
    method: 'put',
    data: data
  })
}

// 删除劳动合同
export function delHrContract(contractId) {
  return request({
    url: '/hr/contract/' + contractId,
    method: 'delete'
  })
}
