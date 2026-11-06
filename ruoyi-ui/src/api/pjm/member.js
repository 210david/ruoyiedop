import request from '@/utils/request'

export function listMember(query) { return request({ url: '/pjm/member/list', method: 'get', params: query }) }
export function getMember(memberId) { return request({ url: '/pjm/member/' + memberId, method: 'get' }) }
export function addMember(data) { return request({ url: '/pjm/member', method: 'post', data }) }
export function updateMember(data) { return request({ url: '/pjm/member', method: 'put', data }) }
export function delMember(memberIds) { return request({ url: '/pjm/member/' + memberIds, method: 'delete' }) }
/** 可选用户列表（复用系统接口） */
export function listAllUsers() { return request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 500 } }) }
