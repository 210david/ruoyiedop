import request from '@/utils/request'

// 首页聚合数据
export function getDashboard() {
  return request({
    url: '/home/dashboard',
    method: 'get'
  })
}

// 获取当前用户门户首页模块显示配置
export function getHomeLayout() {
  return request({
    url: '/home/layout',
    method: 'get'
  })
}

// 保存当前用户门户首页模块显示配置
export function saveHomeLayout(moduleCodes) {
  return request({
    url: '/home/layout',
    method: 'put',
    data: { moduleCodes }
  })
}
