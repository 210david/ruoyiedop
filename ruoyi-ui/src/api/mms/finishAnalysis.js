import request from '@/utils/request'

// 工单完工分析 - 明细列表
export function listFinishAnalysis(query) {
  return request({ url: '/mms/finishAnalysis/list', method: 'get', params: query })
}

// 工单完工分析 - 汇总统计
export function getFinishAnalysisSummary(query) {
  return request({ url: '/mms/finishAnalysis/summary', method: 'get', params: query })
}
