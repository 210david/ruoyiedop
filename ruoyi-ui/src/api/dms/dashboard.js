import request from '@/utils/request'

export function getStats() {
  return request({ url: '/dms/dashboard/stats', method: 'get' })
}
