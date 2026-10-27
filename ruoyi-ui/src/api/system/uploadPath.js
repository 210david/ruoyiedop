import request from '@/utils/request'

// 查询上传路径配置（含生效路径、来源、目录状态）
export function getUploadPathInfo() {
  return request({
    url: '/system/upload-path/info',
    method: 'get'
  })
}

// 修改上传路径（校验 + 自动创建 + 即时生效）
export function updateUploadPath(path) {
  return request({
    url: '/system/upload-path/update',
    method: 'put',
    data: { path }
  })
}

// 迁移文件：把旧目录内容迁移到当前上传目录
export function migrateUploadPath(fromPath) {
  return request({
    url: '/system/upload-path/migrate',
    method: 'post',
    data: { fromPath },
    timeout: 600000
  })
}
