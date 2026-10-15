import request from '@/utils/request'

// 检验任务证件附件列表
export function listInspCert(taskId) {
  return request({ url: '/food/inspcert/list/' + taskId, method: 'get' })
}

// 必传证件配置（开关+类型列表）
export function requiredCertTypes() {
  return request({ url: '/food/inspcert/requiredTypes', method: 'get' })
}

// 登记证件附件
export function addInspCert(data) {
  return request({ url: '/food/inspcert', method: 'post', data: data })
}

// 删除证件附件
export function delInspCert(certIds) {
  return request({ url: '/food/inspcert/' + certIds, method: 'delete' })
}
