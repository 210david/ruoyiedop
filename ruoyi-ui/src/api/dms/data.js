import request from '@/utils/request'

// ===== 采集配置 =====
export function listConfig(query) {
  return request({ url: '/dms/data/config/list', method: 'get', params: query })
}
export function getConfig(configId) {
  return request({ url: '/dms/data/config/' + configId, method: 'get' })
}
export function addConfig(data) {
  return request({ url: '/dms/data/config', method: 'post', data: data })
}
export function updateConfig(data) {
  return request({ url: '/dms/data/config', method: 'put', data: data })
}
export function delConfig(configId) {
  return request({ url: '/dms/data/config/' + configId, method: 'delete' })
}

// ===== 数据记录 =====
export function listRecord(query) {
  return request({ url: '/dms/data/record/list', method: 'get', params: query })
}
export function getRecord(recordId) {
  return request({ url: '/dms/data/record/' + recordId, method: 'get' })
}
export function addRecord(data) {
  return request({ url: '/dms/data/record', method: 'post', data: data })
}
export function updateRecord(data) {
  return request({ url: '/dms/data/record', method: 'put', data: data })
}
export function delRecord(recordId) {
  return request({ url: '/dms/data/record/' + recordId, method: 'delete' })
}
