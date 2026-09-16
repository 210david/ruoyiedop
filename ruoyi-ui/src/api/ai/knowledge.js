import request from '@/utils/request'

// 查询知识库文档列表
export function listKnowledge(query) {
  return request({ url: '/ai/knowledge/list', method: 'get', params: query })
}

// 上传并解析文档
export function uploadKnowledge(data) {
  return request({ url: '/ai/knowledge/upload', method: 'post', data: data, timeout: 120000,
    headers: { 'Content-Type': 'multipart/form-data' } })
}

// 删除文档
export function delKnowledge(docIds) {
  return request({ url: '/ai/knowledge/' + docIds, method: 'delete' })
}

// 知识库问答
export function knowledgeQa(data) {
  return request({ url: '/ai/knowledge/qa', method: 'post', data: data, timeout: 90000 })
}

// 查询本人问答历史
export function listQaHistory(params) {
  return request({ url: '/ai/knowledge/history', method: 'get', params })
}

// 清空本人问答历史
export function clearQaHistory() {
  return request({ url: '/ai/knowledge/history', method: 'delete' })
}

// 知识文档基本信息
export function getDocInfo(docId) {
  return request({ url: '/ai/knowledge/doc/' + docId, method: 'get' })
}

// 按文档名称查找（历史来源无docId时兜底）
export function findDocByName(name) {
  return request({ url: '/ai/knowledge/docByName', method: 'get', params: { name } })
}

// 知识文档切片内容
export function getDocChunks(docId) {
  return request({ url: '/ai/knowledge/chunks/' + docId, method: 'get' })
}

// 下载知识文档原文件（Blob）
export function downloadDoc(docId) {
  return request({ url: '/ai/knowledge/download/' + docId, method: 'get', responseType: 'blob', timeout: 300000 })
}
