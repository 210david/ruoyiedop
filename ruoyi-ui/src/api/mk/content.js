import request from '@/utils/request'

// 查询内容列表
export function listContent(query, config = {}) {
  return request({ url: '/mk/content/list', method: 'get', params: query, ...config })
}

// 查询内容详细（含内容包集合 packageList）
export function getContent(contentId) {
  return request({ url: '/mk/content/' + contentId, method: 'get' })
}

// 新增内容（主档+内容包集合整体保存）
export function addContent(data) {
  return request({ url: '/mk/content', method: 'post', data: data })
}

// 修改内容（主档+内容包集合整体保存）
export function updateContent(data) {
  return request({ url: '/mk/content', method: 'put', data: data })
}

// 删除内容
export function delContent(contentId) {
  return request({ url: '/mk/content/' + contentId, method: 'delete' })
}

// 提交审核（草稿/已驳回 → 待审核）
export function submitContent(contentId) {
  return request({ url: '/mk/content/submit/' + contentId, method: 'put' })
}

// 审核内容 { pass, auditRemark }
export function auditContent(contentId, data) {
  return request({ url: '/mk/content/audit/' + contentId, method: 'put', data: data })
}

// 内容包发布回填 { publishUrl }
export function publishContent(packageId, data) {
  return request({ url: '/mk/content/publish/' + packageId, method: 'put', data: data })
}

// AI 文案生成 { contentId, channelTypes[] }，结果仅填充草稿不落库
export function aiGenerateContent(data) {
  return request({ url: '/mk/content/aiGenerate', method: 'post', data: data })
}
