import request from '@/utils/request'

// 查询经销商档案列表
export function listDealer(query, config = {}) {
  return request({ url: '/mk/dealer/list', method: 'get', params: query, ...config })
}

// 经销商层级树
export function dealerTree(query, config = {}) {
  return request({ url: '/mk/dealer/tree', method: 'get', params: query, ...config })
}

// 查询经销商档案详情
export function getDealer(profileId) {
  return request({ url: '/mk/dealer/' + profileId, method: 'get' })
}

// 按客户ID查询档案
export function getDealerByCustomer(customerId) {
  return request({ url: '/mk/dealer/customer/' + customerId, method: 'get' })
}

// 新增经销商档案
export function addDealer(data) {
  return request({ url: '/mk/dealer', method: 'post', data: data })
}

// 修改经销商档案
export function updateDealer(data) {
  return request({ url: '/mk/dealer', method: 'put', data: data })
}

// 删除经销商档案
export function delDealer(profileIds) {
  return request({ url: '/mk/dealer/' + profileIds, method: 'delete' })
}
