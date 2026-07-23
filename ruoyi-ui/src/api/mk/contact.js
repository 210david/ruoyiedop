import request from '@/utils/request'

// 查询联系人列表
export function listContact(query) {
  return request({ url: '/mk/contact/list', method: 'get', params: query })
}

// 查询联系人详细
export function getContact(contactId) {
  return request({ url: '/mk/contact/' + contactId, method: 'get' })
}

// 新增联系人
export function addContact(data) {
  return request({ url: '/mk/contact', method: 'post', data: data })
}

// 修改联系人
export function updateContact(data) {
  return request({ url: '/mk/contact', method: 'put', data: data })
}

// 删除联系人
export function delContact(contactId) {
  return request({ url: '/mk/contact/' + contactId, method: 'delete' })
}

// ====== P1-7: 主要联系人管理 ======
export function setPrimary(contactId) {
  return request({ url: '/mk/contact/setPrimary/' + contactId, method: 'put' })
}

export function batchSetKey(data) {
  return request({ url: '/mk/contact/batchSetKey', method: 'put', data: data })
}

// ====== P2-8: 联系人去重合并 ======
export function checkDuplicate(phone, email) {
  return request({ url: '/mk/contact/checkDuplicate', method: 'get', params: { phone, email } })
}

export function mergeContacts(data) {
  return request({ url: '/mk/contact/merge', method: 'put', data: data })
}

// ====== P2-10: 联系人分配/转移 ======
export function assignContact(contactId, data) {
  return request({ url: '/mk/contact/assign/' + contactId, method: 'put', data: data })
}

export function releaseContact(contactId) {
  return request({ url: '/mk/contact/release/' + contactId, method: 'put' })
}

// ====== P3-13: 联系人导入 ======
export function importTemplate() {
  return request({ url: '/mk/contact/importTemplate', method: 'post', responseType: 'blob' })
}

// ====== P3-14: 跟进提醒 ======
export function getFollowUpReminders() {
  return request({ url: '/mk/contact/followUpReminders', method: 'get' })
}
