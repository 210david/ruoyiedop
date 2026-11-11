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

// 部署前结构对比（结构未变无需存为新版本）
export function deployCheckModel(modelId) {
  return request({ url: '/flowable/model/deployCheck/' + modelId, method: 'get' })
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

// ===== 任务 =====
export function listTodoTask(query) {
  return request({ url: '/flowable/task/todoList', method: 'get', params: query })
}

// 我的全部未结事项（待办任务 + 草稿，行上用 bizType 区分）
export function listAllTask(query) {
  return request({ url: '/flowable/task/allList', method: 'get', params: query })
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

// 已办页签数量（全部 / 未结束 / 已结束）
export function countDoneTask() {
  return request({ url: '/flowable/task/doneCount', method: 'get' })
}

export function completeTask(data) {
  return request({ url: '/flowable/task/complete', method: 'put', data: data })
}

// 驳回（退回）时可选的节点：本人当前节点之前走过的环节 + 提交申请节点
export function getReturnNodes(taskId) {
  return request({ url: '/flowable/task/prevNodes/' + taskId, method: 'get' })
}

// 待办页签数量：{ all: 全部待办, returned: 被退回 }
export function countTodoTasks() {
  return request({ url: '/flowable/task/todoCounts', method: 'get' })
}

export function delegateTask(taskId, targetUserId, comment) {
  return request({ url: '/flowable/task/delegate/' + taskId, method: 'put', params: { targetUserId, comment } })
}

// 办理页「加签」：先委派给他人办理，办完回到当前办理人
export function addSignTask(taskId, targetUserId, comment) {
  return request({ url: '/flowable/task/addSign/' + taskId, method: 'put', params: { targetUserId, comment } })
}

// 办理页「终止」：办理人强制结束流程
export function terminateTask(taskId, reason) {
  return request({ url: '/flowable/task/terminate/' + taskId, method: 'put', params: { reason } })
}

// 办理页「抄送」：把手动选择的接收人写入待阅
export function sendTaskCc(data) {
  return request({ url: '/flowable/task/cc', method: 'post', data: data })
}

// 流程人员选择（转办/加签/抄送）：组织全量人员，不受数据权限过滤
export function listOrgPickerUsers(keyword) {
  return request({ url: '/flowable/picker/users', method: 'get', params: { keyword } })
}

// 流程人员选择：组织全量部门树，不受数据权限过滤
export function getOrgPickerDepts() {
  return request({ url: '/flowable/picker/depts', method: 'get' })
}

// 办理页「打印」：节点配置的打印模板（未配置时 configured=false，前端回退节点显示模板）
export function getTaskPrintTemplate(taskId) {
  return request({ url: '/flowable/task/printTemplate/' + taskId, method: 'get' })
}
