import request from '@/utils/request'

// ==================== 工厂 ====================
export function listFactory(query) {
  return request({ url: '/mms/factory/list', method: 'get', params: query })
}
export function getFactory(factoryId) {
  return request({ url: '/mms/factory/' + factoryId, method: 'get' })
}
export function addFactory(data) {
  return request({ url: '/mms/factory', method: 'post', data })
}
export function updateFactory(data) {
  return request({ url: '/mms/factory', method: 'put', data })
}
export function delFactory(factoryId) {
  return request({ url: '/mms/factory/' + factoryId, method: 'delete' })
}

// ==================== 车间 ====================
export function listWorkshop(query) {
  return request({ url: '/mms/factory/workshop/list', method: 'get', params: query })
}
export function getWorkshop(workshopId) {
  return request({ url: '/mms/factory/workshop/' + workshopId, method: 'get' })
}
export function addWorkshop(data) {
  return request({ url: '/mms/factory/workshop', method: 'post', data })
}
export function updateWorkshop(data) {
  return request({ url: '/mms/factory/workshop', method: 'put', data })
}
export function delWorkshop(workshopId) {
  return request({ url: '/mms/factory/workshop/' + workshopId, method: 'delete' })
}

// ==================== 产线 ====================
export function listLine(query) {
  return request({ url: '/mms/factory/line/list', method: 'get', params: query })
}
export function getLine(lineId) {
  return request({ url: '/mms/factory/line/' + lineId, method: 'get' })
}
export function addLine(data) {
  return request({ url: '/mms/factory/line', method: 'post', data })
}
export function updateLine(data) {
  return request({ url: '/mms/factory/line', method: 'put', data })
}
export function delLine(lineId) {
  return request({ url: '/mms/factory/line/' + lineId, method: 'delete' })
}

// ==================== 工位 ====================
export function listStation(query) {
  return request({ url: '/mms/factory/station/list', method: 'get', params: query })
}
export function getStation(stationId) {
  return request({ url: '/mms/factory/station/' + stationId, method: 'get' })
}
export function addStation(data) {
  return request({ url: '/mms/factory/station', method: 'post', data })
}
export function updateStation(data) {
  return request({ url: '/mms/factory/station', method: 'put', data })
}
export function delStation(stationId) {
  return request({ url: '/mms/factory/station/' + stationId, method: 'delete' })
}
