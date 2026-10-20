import request from '@/utils/request'

// 查询资料版本列表（按资料类型+业务ID，时间轴展示）
export function listVersion(bizType, bizId) {
  return request({
    url: '/pdm/docVersion/list',
    method: 'get',
    params: { bizType: bizType, bizId: bizId }
  })
}

// 查询产品档案聚合的全部资料版本（方案1/图纸2/BOM3）
export function listProductVersions(productId) {
  return request({
    url: '/pdm/docVersion/listByProduct',
    method: 'get',
    params: { productId: productId }
  })
}

// 查询资料当前生效版本
export function getEffectiveVersion(bizType, bizId) {
  return request({
    url: '/pdm/docVersion/effective',
    method: 'get',
    params: { bizType: bizType, bizId: bizId }
  })
}

// 查询版本详细
export function getVersion(versionId) {
  return request({
    url: '/pdm/docVersion/' + versionId,
    method: 'get'
  })
}
