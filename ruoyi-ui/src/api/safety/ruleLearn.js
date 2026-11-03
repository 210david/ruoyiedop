import request from '@/utils/request'

// 安全守则学习任务与签阅（需求一，SR-03 名单只可追加）
export function listLearnTask(query) {
  return request({ url: '/safety/ruleLearn/task/list', method: 'get', params: query })
}
export function getLearnTask(taskId) {
  return request({ url: '/safety/ruleLearn/task/' + taskId, method: 'get' })
}
export function addLearnTask(data) {
  return request({ url: '/safety/ruleLearn/task', method: 'post', data: data })
}
// 名单追加（已存在人员自动跳过）
export function appendLearns(taskId, data) {
  return request({ url: '/safety/ruleLearn/task/' + taskId + '/learns', method: 'post', data: data })
}
// 签阅名单
export function listLearns(taskId) {
  return request({ url: '/safety/ruleLearn/task/' + taskId + '/learns', method: 'get' })
}
// 签阅确认
export function signLearn(data) {
  return request({ url: '/safety/ruleLearn/sign', method: 'put', data: data })
}
// 学习任务统计
export function learnTaskStats() {
  return request({ url: '/safety/ruleLearn/task/stats', method: 'get' })
}
