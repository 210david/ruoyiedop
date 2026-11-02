import request from '@/utils/request'

/**
 * 获取后台配置的单文件上传上限（单位 MB）
 * 对应后端 CommonController#uploadMaxSize，参数键 system.upload.maxFileSizeMb
 */
export function getUploadMaxSize() {
  return request({
    url: '/common/uploadMaxSize',
    method: 'get',
    // 上传上限属于弱依赖配置，失败时不应打断页面，静默回落
    headers: { isRepeatSubmit: false }
  })
}
