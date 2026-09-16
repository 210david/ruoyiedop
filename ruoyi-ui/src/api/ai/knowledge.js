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
