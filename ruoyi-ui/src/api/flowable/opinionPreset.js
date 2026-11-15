import request from '@/utils/request'

/** 我的常用意见（current=当前生效短语，defaults=系统默认，personalized=是否自定义过） */
export function listOpinionPresets() {
  return request({ url: '/flowable/opinionPreset/list', method: 'get' })
}

/** 保存我的常用意见（整体覆盖，数组顺序即展示顺序） */
export function saveOpinionPresets(presets) {
  return request({ url: '/flowable/opinionPreset/save', method: 'post', data: { presets } })
}

/** 恢复系统默认（清空个人配置） */
export function resetOpinionPresets() {
  return request({ url: '/flowable/opinionPreset/reset', method: 'delete' })
}
