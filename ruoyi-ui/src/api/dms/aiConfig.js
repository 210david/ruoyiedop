import request from '@/utils/request'

export function getAiConfig() {
  return request({ url: '/dms/ai/config', method: 'get' })
}

export function updateAiConfig(data) {
  return request({ url: '/dms/ai/config', method: 'put', data: data })
}
