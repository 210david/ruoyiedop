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

// 常用功能：当前用户的快捷入口（手动固定优先，其次按访问频次）
export function getShortcuts(limit = 8) {
  return request({
    url: '/home/shortcuts',
    method: 'get',
    params: { limit }
  })
}

// 常用功能：记录一次菜单访问（供路由守卫调用，自动沉淀频次）
export function hitShortcut(data) {
  return request({
    url: '/home/shortcuts/hit',
    method: 'post',
    data
  })
}

// 常用功能：保存用户手动固定的入口（全量覆盖手动部分）
export function savePinnedShortcuts(items) {
  return request({
    url: '/home/shortcuts/pinned',
    method: 'put',
    data: { items }
  })
}

// 新手清单：当前岗位的任务与完成状态
export function getOnboardTasks() {
  return request({
    url: '/home/onboard',
    method: 'get'
  })
}

// 新手清单：手工确认任务完成（仅 MANUAL 判定的任务）
export function doneOnboardTask(taskId) {
  return request({
    url: `/home/onboard/${taskId}/done`,
    method: 'post'
  })
}

// 新手清单：保存当前用户的新手引导开关（true=显示 false=关闭）
export function saveOnboardSetting(enabled) {
  return request({
    url: '/home/onboard/setting',
    method: 'put',
    data: { enabled }
  })
}
