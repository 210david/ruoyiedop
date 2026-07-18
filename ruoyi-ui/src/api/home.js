import request from '@/utils/request'

// 首页聚合数据
export function getDashboard() {
  return request({
    url: '/home/dashboard',
    method: 'get'
  })
}
