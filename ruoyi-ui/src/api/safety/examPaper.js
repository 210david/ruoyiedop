import request from '@/utils/request'

// 查询试卷列表（分页）
export function listExamPaper(query) {
  return request({ url: '/safety/exam/paper/list', method: 'get', params: query })
}
// 试卷详情（含题目快照明细）
export function getExamPaper(paperId) {
  return request({ url: '/safety/exam/paper/' + paperId, method: 'get' })
}
// 新增试卷（基础信息）
export function addExamPaper(data) {
  return request({ url: '/safety/exam/paper', method: 'post', data: data })
}
// 修改试卷基础信息（仅草稿/停用）
export function updateExamPaper(data) {
  return request({ url: '/safety/exam/paper', method: 'put', data: data })
}
// 保存试卷题目集合（全量替换，仅草稿，保存即快照并回写总分）
export function savePaperQuestions(data) {
  return request({ url: '/safety/exam/paper/questions', method: 'put', data: data })
}
// 随机抽题（预览，不入库）
export function randomPickExamQuestion(query) {
  return request({ url: '/safety/exam/paper/randomPick', method: 'get', params: query })
}
// 发布试卷
export function publishExamPaper(paperId) {
  return request({ url: '/safety/exam/paper/publish/' + paperId, method: 'put' })
}
// 撤回发布（无考试引用时退回草稿）
export function unpublishExamPaper(paperId) {
  return request({ url: '/safety/exam/paper/unpublish/' + paperId, method: 'put' })
}
// 删除试卷
export function delExamPaper(paperId) {
  return request({ url: '/safety/exam/paper/' + paperId, method: 'delete' })
}
