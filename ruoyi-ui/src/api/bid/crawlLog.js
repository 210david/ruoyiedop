import request from '@/utils/request'

export function listCrawlLog(query) {
  return request({ url: '/bid/crawlLog/list', method: 'get', params: query })
}
export function getCrawlLog(logId) {
  return request({ url: '/bid/crawlLog/' + logId, method: 'get' })
}
export function statusCountsCrawlLog(query) {
  return request({ url: '/bid/crawlLog/statusCounts', method: 'get', params: query })
}
