import request from '@/utils/request'

export function getAiConfig() {
  return request({ url: '/dms/ai/config', method: 'get' })
}

export function updateAiConfig(data) {
  return request({ url: '/dms/ai/config', method: 'put', data: data })
}

// 测试AI大模型连接（传入当前表单配置，支持保存前验证）
export function testAiConfig(data) {
  return request({ url: '/dms/ai/config/test', method: 'post', data: data, timeout: 45000 })
}
