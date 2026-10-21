import request from '@/utils/request'

// ===== 流程模型 =====
export function listModel(query) {
  return request({ url: '/flowable/model/list', method: 'get', params: query })
}

export function getModel(modelId) {
  return request({ url: '/flowable/model/' + modelId, method: 'get' })
}

export function addModel(data) {
  return request({ url: '/flowable/model', method: 'post', data: data })
}

export function updateModel(data) {
  return request({ url: '/flowable/model', method: 'put', data: data })
}

export function delModel(modelIds) {
  return request({ url: '/flowable/model/' + modelIds, method: 'delete' })
}

export function deployModel(modelId, mode, remark, setActive) {
  return request({
    url: '/flowable/model/deploy/' + modelId,
    method: 'post',
    params: { mode: mode || 'new', remark: remark || undefined, setActive: setActive === false ? false : true }
  })
}

// ===== 流程定义 =====
export function listDefinition(query) {
  return request({ url: '/flowable/definition/list', method: 'get', params: query })
}

export function getDefinitionXml(definitionId) {
  return request({ url: '/flowable/definition/xml/' + definitionId, method: 'get' })
}

export function changeDefinitionState(definitionId, suspend) {
  return request({ url: '/flowable/definition/state/' + definitionId, method: 'put', params: { suspend } })
}

export function updateDefinitionRemark(definitionId, remark) {
  return request({ url: '/flowable/definition/remark/' + definitionId, method: 'put', params: { remark } })
}

export function setActiveDefinition(definitionId) {
  return request({ url: '/flowable/definition/active/' + definitionId, method: 'put' })
}

// ===== 流程实例 =====
export function startProcess(data) {
  return request({ url: '/flowable/process/start', method: 'post', data: data })
}

export function listMyProcess(query) {
  return request({ url: '/flowable/process/myList', method: 'get', params: query })
}

export function getProcess(instanceId) {
  return request({ url: '/flowable/process/' + instanceId, method: 'get' })
}

export function getProcessHistory(instanceId) {
  return request({ url: '/flowable/process/history/' + instanceId, method: 'get' })
}

export function cancelProcess(instanceId, reason) {
  return request({ url: '/flowable/process/cancel/' + instanceId, method: 'put', params: { reason } })
}

// ===== 任务 =====
export function listTodoTask(query) {
  return request({ url: '/flowable/task/todoList', method: 'get', params: query })
}

// 流程分类树（待办/查询页左侧树）
export function getProcessTree() {
  return request({ url: '/flowable/process/tree', method: 'get' })
}

export function countTodoTask() {
  return request({ url: '/flowable/task/todoCount', method: 'get' })
}

export function listDoneTask(query) {
  return request({ url: '/flowable/task/doneList', method: 'get', params: query })
}

export function completeTask(data) {
  return request({ url: '/flowable/task/complete', method: 'put', data: data })
}

export function delegateTask(taskId, targetUserId, comment) {
  return request({ url: '/flowable/task/delegate/' + taskId, method: 'put', params: { targetUserId, comment } })
}
