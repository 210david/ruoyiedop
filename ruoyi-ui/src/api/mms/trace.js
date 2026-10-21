import request from '@/utils/request'

export function listTrace(query) {
  return request({ url: '/mms/trace/list', method: 'get', params: query })
}
export function getTrace(traceId) {
  return request({ url: '/mms/trace/' + traceId, method: 'get' })
}
// 正向追溯（物料批次 → 下游工单/成品/客户）
export function forwardTrace(batchNo) {
  return request({ url: '/mms/trace/forward/' + encodeURIComponent(batchNo), method: 'get' })
}
// 反向追溯（成品批次 → 上游物料/供应商/检验/NCR）
export function backwardTrace(batchNo) {
  return request({ url: '/mms/trace/backward/' + encodeURIComponent(batchNo), method: 'get' })
}
// 降级追溯（谱系无数据时从业务单据辅助关联）
export function fallbackTrace(batchNo, direction = 'backward') {
  return request({ url: '/mms/trace/fallback/' + encodeURIComponent(batchNo), method: 'get', params: { direction } })
}
