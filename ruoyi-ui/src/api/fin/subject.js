import request from '@/utils/request'

// ==================== 会计科目管理 ====================

// 查询科目列表（全部节点，用于树形维护页）
export function listSubject(query) {
  return request({
    url: '/fin/subject/list',
    method: 'get',
    params: query
  })
}

// 查询全部启用科目（供凭证录入科目下拉）
export function listAllEnabledSubject() {
  return request({
    url: '/fin/subject/allEnabled',
    method: 'get'
  })
}

// 查询科目详情
export function getSubject(subjectId) {
  return request({
    url: '/fin/subject/' + subjectId,
    method: 'get'
  })
}

// 新增科目
export function addSubject(data) {
  return request({
    url: '/fin/subject',
    method: 'post',
    data: data
  })
}

// 修改科目
export function updateSubject(data) {
  return request({
    url: '/fin/subject',
    method: 'put',
    data: data
  })
}

// 删除科目
export function delSubject(subjectId) {
  return request({
    url: '/fin/subject/' + subjectId,
    method: 'delete'
  })
}

// 校验科目编码唯一性
export function checkCodeUnique(subjectCode, subjectId) {
  return request({
    url: '/fin/subject/checkCodeUnique',
    method: 'get',
    params: { subjectCode, subjectId }
  })
}