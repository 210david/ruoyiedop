import request from '@/utils/request'

// 产出率损耗核算列表
export function analysisYield(query) {
  return request({ url: '/food/yield/analysis', method: 'get', params: query })
}
