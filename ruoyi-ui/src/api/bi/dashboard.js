import request from '@/utils/request'

// 决策驾驶舱总览：最新快照指标 + 预警横幅 + 近30天趋势
export function getBiOverview() {
  return request({
    url: '/bi/dashboard/overview',
    method: 'get'
  })
}

// F6 跨模块价值链视图（销售到现金链 / 供应保障链）
export function getValueChain() {
  return request({
    url: '/bi/dashboard/valueChain',
    method: 'get'
  })
}
