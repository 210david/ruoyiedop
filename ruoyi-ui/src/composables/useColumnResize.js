import { ref } from 'vue'

/**
 * 表格列宽拖拽持久化 composable
 * 配合 el-table 的 @header-dragend 事件和 :width 绑定使用
 *
 * @param {string} storageKey localStorage 存储键名（按页面隔离）
 * @returns {{ colWidth, onHeaderDragEnd }}
 */
export function useColumnResize(storageKey) {
  const savedWidths = ref({})
  try {
    savedWidths.value = JSON.parse(localStorage.getItem(storageKey) || '{}')
  } catch (e) {
    savedWidths.value = {}
  }

  /**
   * 获取列宽：优先使用 localStorage 中保存的值，否则返回默认值
   * @param {string} prop 列的 prop 属性
   * @param {number} defaultWidth 默认宽度
   */
  function colWidth(prop, defaultWidth) {
    return savedWidths.value[prop] || defaultWidth
  }

  /**
   * el-table header-dragend 事件回调，保存拖拽后的列宽
   * 优先用 column.property 作为 key，没有则用 column.label
   */
  function onHeaderDragEnd(newWidth, oldWidth, column) {
    const key = column.property || column.label
    if (key) {
      savedWidths.value[key] = Math.round(newWidth)
      localStorage.setItem(storageKey, JSON.stringify(savedWidths.value))
    }
  }

  return { colWidth, onHeaderDragEnd }
}
