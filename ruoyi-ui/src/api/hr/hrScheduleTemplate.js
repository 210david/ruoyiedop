import request from '@/utils/request'

// 查询排班模板列表
export function listHrScheduleTemplate(query) {
  return request({
    url: '/hr/scheduleTemplate/list',
    method: 'get',
    params: query
  })
}

// 查询排班模板详细
export function getHrScheduleTemplate(templateId) {
  return request({
    url: '/hr/scheduleTemplate/' + templateId,
    method: 'get'
  })
}

// 新增排班模板
export function addHrScheduleTemplate(data) {
  return request({
    url: '/hr/scheduleTemplate',
    method: 'post',
    data: data
  })
}

// 修改排班模板
export function updateHrScheduleTemplate(data) {
  return request({
    url: '/hr/scheduleTemplate',
    method: 'put',
    data: data
  })
}

// 删除排班模板
export function delHrScheduleTemplate(templateId) {
  return request({
    url: '/hr/scheduleTemplate/' + templateId,
    method: 'delete'
  })
}
