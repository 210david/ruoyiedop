import request from '@/utils/request'

/**
 * 表格列配置 API（列宽 + 列显隐）
 *
 * 优先级：个人配置 > 全局配置 > 默认值
 * - 管理员设置 → 保存为全局配置
 * - 普通用户设置 → 保存为个人配置
 */

// 获取某页面的合并列配置（个人覆盖全局）
// 返回 { widths: { colProp: width }, columns: { colProp: visible } }
export function getTableWidths(tableKey) {
  return request({
    url: '/system/tableConfig/' + tableKey,
    method: 'get',
    suppressError: true
  })
}

// 保存单列列宽配置
export function saveTableWidth(tableKey, colProp, colWidth) {
  return request({
    url: '/system/tableConfig',
    method: 'post',
    data: { tableKey, colProp, colWidth },
    headers: { repeatSubmit: false },
    suppressError: true
  })
}

// 保存单列列显隐配置
export function saveColumnVisible(tableKey, colProp, colVisible) {
  return request({
    url: '/system/tableConfig/visible',
    method: 'post',
    data: { tableKey, colProp, colVisible },
    headers: { repeatSubmit: false },
    suppressError: true
  })
}

// 删除列宽配置
export function delTableWidth(configId) {
  return request({
    url: '/system/tableConfig/' + configId,
    method: 'delete'
  })
}
