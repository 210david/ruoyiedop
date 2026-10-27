import request from '@/utils/request'

// 查询招投标档案列表
export function listBid(query) {
  return request({
    url: '/mk/bid/list',
    method: 'get',
    params: query
  })
}

// 招投标档案详情（含投标明细 itemList）
export function getBid(bidId) {
  return request({
    url: '/mk/bid/' + bidId,
    method: 'get'
  })
}

// 新增招投标档案（body 含 itemList）
export function addBid(data) {
  return request({
    url: '/mk/bid',
    method: 'post',
    data: data
  })
}

// 修改招投标档案（body 含 itemList）
export function updateBid(data) {
  return request({
    url: '/mk/bid',
    method: 'put',
    data: data
  })
}

// 删除招投标档案
export function delBid(bidIds) {
  return request({
    url: '/mk/bid/' + bidIds,
    method: 'delete'
  })
}

// 标记开标结果（bidStatus: 2中标/3未中标/4废标）
export function markBidResult(data) {
  return request({
    url: '/mk/bid/markResult',
    method: 'put',
    data: data
  })
}

// 由中标投标生成合同草稿
export function createBidContract(bidId) {
  return request({
    url: '/mk/bid/createContract/' + bidId,
    method: 'put'
  })
}
