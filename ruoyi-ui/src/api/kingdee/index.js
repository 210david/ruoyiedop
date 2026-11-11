import request from '@/utils/request'

// ===== 金蝶配置 =====
export function getKingdeeConfig(showSecret) {
  const url = '/kingdee/config' + (showSecret ? '?showSecret=true' : '')
  return request({ url, method: 'get' })
}

export function updateKingdeeConfig(data) {
  return request({ url: '/kingdee/config', method: 'put', data })
}

export function testKingdeeConfig() {
  return request({ url: '/kingdee/config/test', method: 'post', timeout: 45000 })
}

// ===== 同步操作 =====
export function syncCustomer() {
  return request({ url: '/kingdee/sync/customer', method: 'post', timeout: 120000 })
}

export function syncMaterial() {
  return request({ url: '/kingdee/sync/material', method: 'post', timeout: 120000 })
}

export function syncSupplier() {
  return request({ url: '/kingdee/sync/supplier', method: 'post', timeout: 120000 })
}

export function syncAll() {
  return request({ url: '/kingdee/sync/all', method: 'post', timeout: 180000 })
}
