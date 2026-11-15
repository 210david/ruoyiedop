import request from '@/utils/request'

// 流程附件：文件本体走 /common/upload 落盘，此处只登记附件与流程实例的归属关系
export function listFlowAttach(instanceId) {
  return request({ url: '/flowable/attach/list/' + instanceId, method: 'get' })
}

export function addFlowAttach(data) {
  return request({ url: '/flowable/attach', method: 'post', data: data })
}

export function delFlowAttach(attachId) {
  return request({ url: '/flowable/attach/' + attachId, method: 'delete' })
}
