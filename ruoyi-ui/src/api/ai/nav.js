import request from '@/utils/request'

// ===== AI 菜单直达：意图管理 =====

// 意图列表（可按关键字过滤）
export function listNavIntent(keyword) {
  return request({ url: '/ai/nav/list', method: 'get', params: { keyword } })
}

// 意图详情（含该菜单对当前用户解析出的真实路径）
export function getNavIntent(intentId) {
  return request({ url: '/ai/nav/' + intentId, method: 'get' })
}

// 新增意图
export function addNavIntent(data) {
  return request({ url: '/ai/nav', method: 'post', data })
}

// 修改意图
export function updateNavIntent(data) {
  return request({ url: '/ai/nav', method: 'put', data })
}

// 删除意图
export function delNavIntent(intentId) {
  return request({ url: '/ai/nav/' + intentId, method: 'delete' })
}

// 可选目标菜单（按当前登录人权限裁剪）
export function navMenuOptions() {
  return request({ url: '/ai/nav/menuOptions', method: 'get' })
}

// 当前用户可被推荐的菜单索引（自查用）
export function myNavMenus() {
  return request({ url: '/ai/nav/myMenus', method: 'get' })
}

// ===== 知识文档关联功能入口（P2）=====

// 设置/清除文档绑定的功能入口（navMenuId 为空即解绑）
export function updateDocNav(docId, navMenuId) {
  return request({
    url: '/ai/knowledge/nav/' + docId,
    method: 'put',
    params: { navMenuId }
  })
}
