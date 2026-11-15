import request from '@/utils/request'

// 某流程模型下所有节点的打印模板配置（设计器加载时取回）
export function listNodePrints(modelId) {
  return request({ url: '/flowable/nodePrint/list/' + modelId, method: 'get' })
}

// 保存节点打印模板（同一节点覆盖）
export function saveNodePrint(data) {
  return request({ url: '/flowable/nodePrint/save', method: 'post', data: data })
}

// 删除节点打印模板：恢复为「跟随节点显示模板」
export function delNodePrint(modelId, nodeId) {
  return request({ url: '/flowable/nodePrint/' + modelId + '/' + nodeId, method: 'delete' })
}
