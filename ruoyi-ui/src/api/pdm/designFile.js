import request from '@/utils/request'

// 查询图纸技术文件列表
export function listDesignFile(query) {
  return request({ url: '/pdm/designFile/list', method: 'get', params: query })
}

// 查询文件详情
export function getDesignFile(fileId) {
  return request({ url: '/pdm/designFile/' + fileId, method: 'get' })
}

// 上传资料（上传即纳入版本管理）
export function addDesignFile(data) {
  return request({ url: '/pdm/designFile', method: 'post', data: data })
}

// 修改资料（文件替换自动升版）
export function updateDesignFile(data) {
  return request({ url: '/pdm/designFile', method: 'put', data: data })
}

// 文件生效（版本生效 + 置为受控状态）
export function effectiveDesignFile(data) {
  return request({ url: '/pdm/designFile/effective', method: 'put', data: data })
}

// 手工升版
export function upgradeDesignFile(data) {
  return request({ url: '/pdm/designFile/upgrade', method: 'put', data: data })
}

// 删除资料
export function delDesignFile(fileIds) {
  return request({ url: '/pdm/designFile/' + fileIds, method: 'delete' })
}
