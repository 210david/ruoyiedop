import request from '@/utils/request'

// 分经销商汇总列表（销量/返利/账龄/流向）
export function listDealerReport(query, config = {}) {
  return request({ url: '/mk/dealerReport/list', method: 'get', params: query, ...config })
}

// 汇总合计（全量筛选结果，不受分页影响）
export function totalDealerReport(query, config = {}) {
  return request({ url: '/mk/dealerReport/total', method: 'get', params: query, ...config })
}

// 页签数量
export function countsDealerReport(config = {}) {
  return request({ url: '/mk/dealerReport/counts', method: 'get', ...config })
}
