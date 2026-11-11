import request from '@/utils/request'

/** 我的待阅列表（query.readFlag=0 只看未读，当前登录人，分页） */
export function listMyCc(query) {
  return request({ url: '/flowable/cc/list', method: 'get', params: query })
}

/** 我的未读待阅数量 */
export function getCcUnreadCount() {
  return request({ url: '/flowable/cc/unreadCount', method: 'get' })
}

/** 标记单条已读 */
export function readCc(ccId) {
  return request({ url: '/flowable/cc/read/' + ccId, method: 'put' })
}

/** 全部标记已读 */
export function readAllCc() {
  return request({ url: '/flowable/cc/readAll', method: 'put' })
}

/** 某流程实例的抄送记录（办理页/详情页审批意见区展示抄送人） */
export function listCcByInstance(instanceId) {
  return request({ url: '/flowable/cc/byInstance/' + instanceId, method: 'get' })
}
