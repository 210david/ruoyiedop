import request from '@/utils/request'

// 项目采购台账（聚合采购管理模块关联项目的单据）
export function listPmsLedger(query) {
  return request({ url: '/pjm/pms/ledger', method: 'get', params: query })
}
