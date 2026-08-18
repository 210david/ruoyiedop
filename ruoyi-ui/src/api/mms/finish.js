import request from '@/utils/request'

export function listFinish(query) {
  return request({
    url: '/mms/finish/list',
    method: 'get',
    params: query
  })
}

// 兼容别名：部分页面使用 listFinishReceipt 名称导入
export const listFinishReceipt = listFinish;

export function getFinishReceipt(finishId) {
  return request({
    url: '/mms/finish/' + finishId,
    method: 'get'
  })
}

export function addFinishReceipt(data) {
  return request({
    url: '/mms/finish',
    method: 'post',
    data: data
  })
}

export function delFinishReceipt(finishId) {
  return request({
    url: '/mms/finish/' + finishId,
    method: 'delete'
  })
}

export function updateFinishReceipt(data) {
  return request({
    url: '/mms/finish',
    method: 'put',
    data: data
  })
}

