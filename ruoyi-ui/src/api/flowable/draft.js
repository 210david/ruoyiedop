import request from '@/utils/request'

/** 我的草稿列表（当前登录人，分页） */
export function listDraft(query) {
  return request({ url: '/flowable/draft/list', method: 'get', params: query })
}

/** 草稿详情（含表单数据） */
export function getDraft(draftId) {
  return request({ url: '/flowable/draft/' + draftId, method: 'get' })
}

/** 保存草稿：有 draftId 则更新，否则新增；返回 { draftId } */
export function saveDraft(data) {
  return request({ url: '/flowable/draft/save', method: 'post', data: data })
}

/** 删除草稿 */
export function delDraft(draftIds) {
  return request({ url: '/flowable/draft/' + draftIds, method: 'delete' })
}
