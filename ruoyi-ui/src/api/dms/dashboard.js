import request from '@/utils/request'

// 获取设备大屏总览统计数据（旧接口，保留兼容）
export function getStats() {
  return request({ url: '/dms/dashboard/stats', method: 'get' })
}

// 获取设备列表（用于大屏设备下拉选择器）
export function listDashboardEquipment() {
  return request({ url: '/dms/dashboard/equipment-list', method: 'get' })
}

// 获取设备全生命周期大屏数据
export function getEquipmentDashboard(equipmentId) {
  return request({ url: '/dms/dashboard/equipment/' + equipmentId, method: 'get' })
}
