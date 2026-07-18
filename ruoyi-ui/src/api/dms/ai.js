import request from '@/utils/request'

// ===== FAQ CRUD =====
export function listFaq(query) {
  return request({ url: '/dms/ai/faq/list', method: 'get', params: query })
}
export function getFaq(faqId) {
  return request({ url: '/dms/ai/faq/' + faqId, method: 'get' })
}
export function addFaq(data) {
  return request({ url: '/dms/ai/faq', method: 'post', data: data })
}
export function updateFaq(data) {
  return request({ url: '/dms/ai/faq', method: 'put', data: data })
}
export function delFaq(faqId) {
  return request({ url: '/dms/ai/faq/' + faqId, method: 'delete' })
}

// ===== AI 功能 =====
export function repairAssistant(input) {
  return request({ url: '/dms/ai/repairAssistant', method: 'post', data: { input } })
}
export function dispatchRecommend(orderId) {
  return request({ url: '/dms/ai/dispatchRecommend/' + orderId, method: 'get' })
}
export function knowledgeQA(question) {
  return request({ url: '/dms/ai/knowledgeQA', method: 'post', data: { question } })
}
