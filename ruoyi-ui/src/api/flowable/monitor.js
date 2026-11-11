import request from '@/utils/request'

/** 流程监控：全部流程实例（支持标题/流程名称/创建人/当前节点/状态/创建日期筛选） */
export function listMonitorProcess(query) {
  return request({ url: '/flowable/monitor/list', method: 'get', params: query })
}

/** 流程监控：状态页签数量（全部 / 进行中 / 已结束 / 已撤销） */
export function countMonitorProcess() {
  return request({ url: '/flowable/monitor/counts', method: 'get' })
}

/** 干预面板数据：当前停留节点、当前待办任务、全部可跳转节点 */
export function getMonitorNodes(instanceId) {
  return request({ url: '/flowable/monitor/nodes/' + instanceId, method: 'get' })
}

/** 流程干预：强制跳转到指定节点（assigneeId 可选，为空由节点操作人配置自动派发） */
export function interveneProcess(instanceId, params) {
  return request({ url: '/flowable/monitor/intervene/' + instanceId, method: 'put', params })
}

/** 强制收回：把流程从当前节点回退到上一节点，由上一节点原办理人重新办理 */
export function recallMonitorProcess(instanceId, reason) {
  return request({ url: '/flowable/monitor/recall/' + instanceId, method: 'put', params: { reason } })
}

/** 改派当前节点办理人 */
export function assignMonitorTask(taskId, targetUserId, comment) {
  return request({ url: '/flowable/monitor/assign/' + taskId, method: 'put', params: { targetUserId, comment } })
}

/** 强制结束（action=end） / 撤销（action=cancel）流程 */
export function endMonitorProcess(instanceId, action, reason) {
  return request({ url: '/flowable/monitor/end/' + instanceId, method: 'put', params: { action, reason } })
}

/** 删除流程实例（连同历史审批记录） */
export function deleteMonitorProcess(instanceId) {
  return request({ url: '/flowable/monitor/' + instanceId, method: 'delete' })
}
