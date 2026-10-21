import request from '@/utils/request'

// 查询企业信息（根据企业名称或统一信用代码）
export function lookupEnterprise(keyword) {
  return request({
    url: '/pms/enterprise/lookup',
    method: 'get',
    params: { keyword }
  })
}

// ===== 配置管理 =====

// 获取企业查询配置
export function getEnterpriseConfig() {
  return request({
    url: '/pms/enterprise/config',
    method: 'get'
  })
}

// 保存企业查询配置
export function saveEnterpriseConfig(data) {
  return request({
    url: '/pms/enterprise/config',
    method: 'put',
    data: data
  })
}

// 测试查询
export function testEnterpriseLookup(keyword) {
  return request({
    url: '/pms/enterprise/test',
    method: 'get',
    params: { keyword }
  })
}
