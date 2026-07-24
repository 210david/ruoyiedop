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
