import request from '@/utils/request'

// WBS 任务 API
export function listTask(query) {
  return request({ url: '/pjm/task/list', method: 'get', params: query })
}
export function getTask(taskId) {
  return request({ url: '/pjm/task/' + taskId, method: 'get' })
}
export function getTaskTree(projectId) {
  return request({ url: '/pjm/task/tree/' + projectId, method: 'get' })
}
export function addTask(data) {
  return request({ url: '/pjm/task', method: 'post', data: data })
}
export function updateTask(data) {
  return request({ url: '/pjm/task', method: 'put', data: data })
}
export function delTask(taskIds) {
  return request({ url: '/pjm/task/' + taskIds, method: 'delete' })
}

// AI 解析附件生成 WBS 草稿（FormData 必须显式声明 multipart，否则被全局 application/json 拦截、后端 @RequestParam 全为 null）
export function aiParseWbs(formData) {
  return request({
    url: '/pjm/task/ai/parse',
    method: 'post',
    data: formData,
    timeout: 180000,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 批量导入 AI 草稿为 WBS 任务
export function aiBatchImportWbs(data) {
  return request({ url: '/pjm/task/ai/batchImport', method: 'post', data: data })
}
