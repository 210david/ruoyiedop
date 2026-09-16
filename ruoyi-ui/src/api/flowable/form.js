import request from '@/utils/request'

export function listForm(query) {
  return request({ url: '/flowable/form/list', method: 'get', params: query })
}

export function allForms() {
  return request({ url: '/flowable/form/all', method: 'get' })
}

export function getForm(formId) {
  return request({ url: '/flowable/form/' + formId, method: 'get' })
}

export function formByProcess(processKey) {
  return request({ url: '/flowable/form/byProcess/' + processKey, method: 'get' })
}

export function addForm(data) {
  return request({ url: '/flowable/form', method: 'post', data: data })
}

export function updateForm(data) {
  return request({ url: '/flowable/form', method: 'put', data: data })
}

export function delForm(formIds) {
  return request({ url: '/flowable/form/' + formIds, method: 'delete' })
}

export function createTable(formId) {
  return request({ url: '/flowable/form/createTable/' + formId, method: 'post' })
}

export function launch(data) {
  return request({ url: '/flowable/form/launch', method: 'post', data: data })
}
