import request from '@/utils/request'

// F3 主题看板（按域）：本域指标快照 + 预警横幅 + 近30天趋势
export function getBoardData(domain) {
  return request({
    url: '/bi/board/' + domain,
    method: 'get'
  })
}
