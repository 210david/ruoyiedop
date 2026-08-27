import request from '@/utils/request'

// 查询劳务用工列表
export function listHrLaborWorker(query) {
  return request({
    url: '/hr/laborWorker/list',
    method: 'get',
    params: query
  })
}

// 查询劳务用工详细
export function getHrLaborWorker(laborId) {
  return request({
    url: '/hr/laborWorker/' + laborId,
    method: 'get'
  })
}

// 新增劳务用工
export function addHrLaborWorker(data) {
  return request({
    url: '/hr/laborWorker',
    method: 'post',
    data: data
  })
}

// 修改劳务用工
export function updateHrLaborWorker(data) {
  return request({
    url: '/hr/laborWorker',
    method: 'put',
    data: data
  })
}

// 删除劳务用工
export function delHrLaborWorker(laborId) {
  return request({
    url: '/hr/laborWorker/' + laborId,
    method: 'delete'
  })
}
