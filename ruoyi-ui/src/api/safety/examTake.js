import request from '@/utils/request'

// 我的考试列表（聚合场次窗口与试卷规则）
export function getMyExams() {
  return request({ url: '/safety/exam/take/myList', method: 'get' })
}
// 进入考试（开始计时 / 续答，返回脱敏试卷快照 + 草稿 + 剩余秒数）
export function startExam(recordId) {
  return request({ url: '/safety/exam/take/start/' + recordId, method: 'post' })
}
// 保存答题草稿
export function saveExamDraft(data) {
  return request({ url: '/safety/exam/take/saveDraft', method: 'post', data: data })
}
// 交卷并自动判分
export function submitExam(data) {
  return request({ url: '/safety/exam/take/submit', method: 'post', data: data })
}
// 查看成绩与解析
export function getExamResult(recordId) {
  return request({ url: '/safety/exam/take/result/' + recordId, method: 'get' })
}
