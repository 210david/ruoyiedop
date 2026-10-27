import request from '@/utils/request'

// 查询证书台账列表
export function listHrCertificate(query) {
  return request({
    url: '/hr/certificate/list',
    method: 'get',
    params: query
  })
}

// 查询证书台账详细
export function getHrCertificate(certId) {
  return request({
    url: '/hr/certificate/' + certId,
    method: 'get'
  })
}

// 新增证书台账
export function addHrCertificate(data) {
  return request({
    url: '/hr/certificate',
    method: 'post',
    data: data
  })
}

// 修改证书台账
export function updateHrCertificate(data) {
  return request({
    url: '/hr/certificate',
    method: 'put',
    data: data
  })
}

// 删除证书台账
export function delHrCertificate(certId) {
  return request({
    url: '/hr/certificate/' + certId,
    method: 'delete'
  })
}
