import request from '@/utils/request'

/** 默认连接器（未传时后端按 dingtalk 处理，保持向后兼容） */
const DEFAULT_CONNECTOR = 'dingtalk'

// 查询外部平台用户绑定列表（钉钉 / 企业微信）
export function listIntegrationBinding(query) {
  return request({
    url: '/integration/attendance/bindings',
    method: 'get',
    params: query
  })
}

// 绑定状态统计（全部 / 已匹配 / 未匹配）
export function getBindingStats(connectorType = DEFAULT_CONNECTOR) {
  return request({
    url: '/integration/attendance/bindingStats',
    method: 'get',
    params: { connectorType }
  })
}

// 同步通讯录并自动匹配员工
export function syncIntegrationContacts(connectorType = DEFAULT_CONNECTOR) {
  return request({
    url: '/integration/attendance/syncContacts',
    method: 'post',
    params: { connectorType }
  })
}

// 按日期范围同步打卡数据
export function syncIntegrationAttendance(beginDate, endDate, connectorType = DEFAULT_CONNECTOR) {
  return request({
    url: '/integration/attendance/sync/' + beginDate + '/' + endDate,
    method: 'post',
    params: { connectorType }
  })
}

// 人工绑定：外部平台用户绑定到员工工号（已绑定时为变更绑定）
export function bindIntegrationUser(data) {
  return request({
    url: '/integration/attendance/binding/bind',
    method: 'put',
    data: data
  })
}

// 解绑：清空外部平台用户与平台员工的关联
export function unbindIntegrationUser(bindingId) {
  return request({
    url: '/integration/attendance/binding/unbind/' + bindingId,
    method: 'put'
  })
}

// 检查员工是否已被其他外部平台用户绑定（一一绑定约束）
export function checkEmployeeOccupant(params) {
  return request({
    url: '/integration/attendance/binding/occupant',
    method: 'get',
    params: params
  })
}

// 查询集成配置（凭据不回显），connectorType 区分钉钉 / 企业微信
export function getIntegrationConfig(connectorType = DEFAULT_CONNECTOR) {
  return request({
    url: '/integration/attendance/config',
    method: 'get',
    params: { connectorType }
  })
}

// 保存集成配置（凭据留空或占位符则不修改）
export function saveIntegrationConfig(data) {
  return request({
    url: '/integration/attendance/config',
    method: 'put',
    data: data
  })
}
