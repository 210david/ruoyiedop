import request from '@/utils/request'

// 查询全部启用模块的标识列表（用于全局功能按模块状态显隐）
export function getEnabledModules() {
  return request({
    url: '/system/module/enabled',
    method: 'get'
  })
}

// 查询模块开关列表（含关联角色/任务统计）
export function listModule(query) {
  return request({
    url: '/system/module/list',
    method: 'get',
    params: query
  })
}

// 切换模块启停状态（级联停用/启用菜单、角色、定时任务）
export function changeModuleStatus(data) {
  return request({
    url: '/system/module/changeStatus',
    method: 'put',
    data: data
  })
}

// 一键恢复全部已停用模块
export function enableAllModule() {
  return request({
    url: '/system/module/enableAll',
    method: 'put'
  })
}
