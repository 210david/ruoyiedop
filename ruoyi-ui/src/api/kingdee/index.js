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

export function syncSalOutbound() {
  return request({ url: '/kingdee/sync/sal-outbound', method: 'post', timeout: 300000 })
}

export function syncPurInStock() {
  return request({ url: '/kingdee/sync/pur-in-stock', method: 'post', timeout: 300000 })
}

export function syncInventory() {
  return request({ url: '/kingdee/sync/inventory', method: 'post', timeout: 300000 })
}

export function syncAll() {
  return request({ url: '/kingdee/sync/all', method: 'post', timeout: 180000 })
}

// ===== appSecret 自动刷新 =====
export function refreshAppSecret() {
  return request({ url: '/kingdee/secret/refresh', method: 'post', timeout: 30000 })
}

export function getAppSecretStatus() {
  return request({ url: '/kingdee/secret/status', method: 'get' })
}
