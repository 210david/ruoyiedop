import { reactive } from 'vue'

/**
 * 详情页卡片折叠 + 金额格式化 组合式函数
 * 配合全局 detail-page.scss 使用
 */
export function useDetailCard(initialKeys = []) {
  const collapsedCards = reactive(
    initialKeys.reduce((acc, key) => {
      acc[key] = false
      return acc
    }, {})
  )

  function toggleCard(name) {
    collapsedCards[name] = !collapsedCards[name]
  }

  return { collapsedCards, toggleCard }
}

/**
 * 金额格式化：千分位 + 两位小数
 */
export function formatAmount(val) {
  if (val == null || val === '') return '-'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 金额格式化（元 → 万元）：千分位，最少 2 位、最多 4 位小数
 */
export function formatWan(val) {
  if (val == null || val === '') return '-'
  const wan = Number(val) / 10000
  return wan.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 4 })
}

/**
 * 元 → 万元（用于表单回显）
 */
export function yuanToWan(val) {
  if (val == null || val === '') return undefined
  return Number(val) / 10000
}

/**
 * 万元 → 元（用于表单提交，空值返回 undefined）
 */
export function wanToYuan(val) {
  if (val == null || val === '') return undefined
  return Number(val) * 10000
}

/**
 * 货币格式化：￥前缀 + 千分位 + 两位小数
 */
export function formatMoney(val) {
  if (val == null || val === '') return '-'
  return '￥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
