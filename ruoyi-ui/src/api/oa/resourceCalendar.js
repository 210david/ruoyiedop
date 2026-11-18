import request from '@/utils/request'

/**
 * 资源使用日历（印章/车辆/司机）
 * @param {Object} params { type: 'vehicle'|'driver'|'seal', startDate, endDate }
 */
export function getResourceCalendar(params) {
  return request({
    url: '/oa/resource/calendar',
    method: 'get',
    params
  })
}
