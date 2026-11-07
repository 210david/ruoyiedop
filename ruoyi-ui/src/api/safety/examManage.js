import request from '@/utils/request'

// 查询考试场次列表（分页）
export function listExam(query) {
  return request({ url: '/safety/exam/list', method: 'get', params: query })
}
// 场次详情
export function getExam(examId) {
  return request({ url: '/safety/exam/' + examId, method: 'get' })
}
// 考生名单 / 成绩列表（分页）
export function listExamRecords(examId, query) {
  return request({ url: '/safety/exam/' + examId + '/records', method: 'get', params: query })
}
// 成绩统计（及格率/平均分/分数段分布）
export function getExamStats(examId) {
  return request({ url: '/safety/exam/' + examId + '/stats', method: 'get' })
}
// 新增场次
export function addExam(data) {
  return request({ url: '/safety/exam', method: 'post', data: data })
}
// 修改场次（仅草稿）
export function updateExam(data) {
  return request({ url: '/safety/exam', method: 'put', data: data })
}
// 发布场次（生成考生名单）
export function publishExam(examId) {
  return request({ url: '/safety/exam/publish/' + examId, method: 'put' })
}
// 撤回场次（仅未开始）
export function cancelExam(examId) {
  return request({ url: '/safety/exam/cancel/' + examId, method: 'put' })
}
// 强制结束（未交卷者标记缺考）
export function forceEndExam(examId) {
  return request({ url: '/safety/exam/forceEnd/' + examId, method: 'put' })
}
// 删除场次
export function delExam(examId) {
  return request({ url: '/safety/exam/' + examId, method: 'delete' })
}
// FR-7：错题率分析（按题目聚合，错误率降序）
export function getQuestionStats(examId) {
  return request({ url: '/safety/exam/' + examId + '/questionStats', method: 'get' })
}
// FR-7：按高错误率题目一键生成复训/补考试卷草稿
export function createRemedialPaper(examId, errorRate) {
  return request({ url: '/safety/exam/' + examId + '/remedialPaper', method: 'post', data: { errorRate: errorRate } })
}
// FR-8：一键创建补考场次（名单=不合格+缺考且有账号者）
export function createRetakeExam(examId, data) {
  return request({ url: '/safety/exam/' + examId + '/retake', method: 'post', data: data })
}
// 可选考试人员（考生来源=手动添加时的选人下拉）
export function examUserOptions(keyword) {
  return request({ url: '/safety/exam/userOptions', method: 'get', params: { keyword: keyword } })
}
// 发布后增补考生（只增不减：手动来源传 userIds，培训来源留空由后端按参训人重新同步追加）
export function addExamUsers(examId, data) {
  return request({ url: '/safety/exam/' + examId + '/users', method: 'post', data: data })
}
