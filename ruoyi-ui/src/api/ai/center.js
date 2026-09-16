import request from '@/utils/request'

// ===== AI配置 =====
export function getAiCenterConfig() {
  return request({ url: '/ai/center/config', method: 'get' })
}

export function updateAiCenterConfig(data) {
  return request({ url: '/ai/center/config', method: 'put', data: data })
}

// 测试AI大模型连接（传入当前表单配置，支持保存前验证）
export function testAiCenterConfig(data) {
  return request({ url: '/ai/center/config/test', method: 'post', data: data, timeout: 45000 })
}

// ===== 用量统计 =====
export function getAiUsage(days = 30) {
  return request({ url: '/ai/center/usage', method: 'get', params: { days } })
}

// ===== 调用日志 =====
export function listAiLog(query) {
  return request({ url: '/ai/center/log/list', method: 'get', params: query })
}
