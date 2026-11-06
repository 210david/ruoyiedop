import request from '@/utils/request'

// 查询项目资料列表
export function listAttachment(query) {
  return request({ url: '/pjm/project/attachment/list', method: 'get', params: query })
}

// 查询项目资料详情
export function getAttachment(attachmentId) {
  return request({ url: '/pjm/project/attachment/' + attachmentId, method: 'get' })
}

// 按项目查询资料
export function listByProject(projectId) {
  return request({ url: '/pjm/project/attachment/project/' + projectId, method: 'get' })
}

// 新增项目资料（保存元数据，文件已通过 /common/upload 上传）
export function addAttachment(data) {
  return request({ url: '/pjm/project/attachment', method: 'post', data: data })
}

// 修改项目资料（如改类型、备注）
export function updateAttachment(data) {
  return request({ url: '/pjm/project/attachment', method: 'put', data: data })
}

// 删除项目资料
export function delAttachment(attachmentIds) {
  return request({ url: '/pjm/project/attachment/' + attachmentIds, method: 'delete' })
}
