import request from '@/utils/request'

// 查询题目列表（分页）
export function listExamQuestion(query) {
  return request({ url: '/safety/exam/question/list', method: 'get', params: query })
}
// 题目详情
export function getExamQuestion(questionId) {
  return request({ url: '/safety/exam/question/' + questionId, method: 'get' })
}
// 新增题目
export function addExamQuestion(data) {
  return request({ url: '/safety/exam/question', method: 'post', data: data })
}
// 修改题目
export function updateExamQuestion(data) {
  return request({ url: '/safety/exam/question', method: 'put', data: data })
}
// 删除题目（逻辑删除）
export function delExamQuestion(questionId) {
  return request({ url: '/safety/exam/question/' + questionId, method: 'delete' })
}
// 启用/停用
export function changeQuestionStatus(questionId, status) {
  return request({ url: '/safety/exam/question/changeStatus', method: 'put', params: { questionId, status } })
}
// FR-10：AI 出题结果批量收编入库（题干同分类查重自动跳过）
export function aiImportQuestions(data) {
  return request({ url: '/safety/exam/question/aiImport', method: 'post', data: data })
}
