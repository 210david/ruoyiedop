import request from '@/utils/request'

// 获取适配器列表
export function listAdapters() {
  return request({
    url: '/dms/gateway/admin/adapters',
    method: 'get'
  })
}

// 启动适配器
export function startAdapter(adapterType) {
  return request({
    url: `/dms/gateway/admin/adapter/${adapterType}/start`,
    method: 'post'
  })
}

// 停止适配器
export function stopAdapter(adapterType) {
  return request({
    url: `/dms/gateway/admin/adapter/${adapterType}/stop`,
    method: 'post'
  })
}

// 获取适配器配置
export function getAdapterConfig(adapterType) {
  return request({
    url: `/dms/gateway/admin/config/${adapterType}`,
    method: 'get'
  })
}

// 保存适配器配置
export function saveAdapterConfig(data) {
  return request({
    url: '/dms/gateway/admin/config',
    method: 'post',
    data: data
  })
}

// 生成设备接入密钥
export function generateDeviceKey(equipmentId) {
  return request({
    url: `/dms/gateway/admin/device/${equipmentId}/key`,
    method: 'post'
  })
}

// 获取接入文档
export function getIntegrationDocs() {
  return request({
    url: '/dms/gateway/admin/docs',
    method: 'get'
  })
}
