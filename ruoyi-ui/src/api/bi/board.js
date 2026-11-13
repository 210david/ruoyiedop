import request from '@/utils/request'

// F3 主题看板（按域）：本域指标快照 + 预警横幅 + 近30天趋势
// 后端端点：BiDashboardController @RequestMapping("/bi/dashboard") + @GetMapping("/board/{domain}")
export function getBoardData(domain) {
  return request({
    url: '/bi/dashboard/board/' + domain,
    method: 'get'
  })
}
