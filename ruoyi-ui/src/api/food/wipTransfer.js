import request from '@/utils/request'

// 查询工序转移单列表
export function listTransfer(query) {
  return request({ url: '/food/wip/transfer/list', method: 'get', params: query })
}

// 转移单详情
export function getTransfer(transferId) {
  return request({ url: '/food/wip/transfer/' + transferId, method: 'get' })
}

// 新增工序转移（转移即生效）
export function addTransfer(data) {
  return request({ url: '/food/wip/transfer', method: 'post', data: data })
}
