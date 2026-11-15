import request from '@/utils/request'

// 设计器加载：模型下全部节点表单模板
export function listNodeForms(modelId) {
  return request({ url: '/flowable/nodeForm/list/' + modelId, method: 'get' })
}

// 保存节点表单模板（存在则更新）
export function saveNodeForm(data) {
  return request({ url: '/flowable/nodeForm/save', method: 'post', data })
}

// 恢复使用数据表默认表单（删除节点模板）
export function delNodeForm(modelId, nodeId) {
  return request({ url: '/flowable/nodeForm/' + modelId + '/' + nodeId, method: 'delete' })
}

// 发起页：按流程Key取发起节点模板
export function getStartNodeForm(processKey) {
  return request({ url: '/flowable/nodeForm/startForm/' + processKey, method: 'get' })
}
