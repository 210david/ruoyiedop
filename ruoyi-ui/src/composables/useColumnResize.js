import { ref, nextTick, onMounted } from 'vue'

/**
 * 表格列宽拖拽持久化 composable
 * 配合 el-table 的 @header-dragend 事件和 :width 绑定使用
 *
 * 新增 tableRef + applySavedWidths：
 * 即使列没有绑定 :width，也能通过 doLayout() 应用保存的列宽，
 * 彻底解决「拖动后刷新恢复原样」的问题。
 *
 * @param {string} storageKey localStorage 存储键名（按页面隔离）
 * @returns {{ colWidth, onHeaderDragEnd, tableRef, applySavedWidths }}
 */
export function useColumnResize(storageKey) {
  const savedWidths = ref({})
  const tableRef = ref(null)

  try {
    savedWidths.value = JSON.parse(localStorage.getItem(storageKey) || '{}')
  } catch (e) {
    savedWidths.value = {}
  }

  /**
   * 获取列宽：优先使用 localStorage 中保存的值，否则返回默认值
   */
  function colWidth(prop, defaultWidth) {
    return savedWidths.value[prop] || defaultWidth
  }

  /**
   * el-table header-dragend 事件回调，保存拖拽后的列宽
   */
  function onHeaderDragEnd(newWidth, oldWidth, column) {
    const key = column.property || column.label
    if (key) {
      savedWidths.value[key] = Math.round(newWidth)
      localStorage.setItem(storageKey, JSON.stringify(savedWidths.value))
    }
  }

  /**
   * 将保存的列宽应用到表格（通过修改内部 columns 并 doLayout）
   * 可在 onMounted / getList 后调用，对未绑定 :width 的列也生效
   */
  function applySavedWidths() {
    nextTick(() => {
      const table = tableRef.value
      if (!table) return
      // 兼容 element-plus 不同版本：columns 可能在不同位置
      const columns = table.columns || (table.store && table.store.states && table.store.states.columns
        ? table.store.states.columns.value || table.store.states.columns
        : []) || []
      if (!columns.length) return

      let changed = false
      columns.forEach(col => {
        const key = col.property || col.label
        if (key && savedWidths.value[key]) {
          const target = savedWidths.value[key]
          // 仅在差异 > 1px 时才修改，避免无意义的 doLayout
          if (Math.abs((col.realWidth || col.width || 0) - target) > 1) {
            col.width = target
            col.realWidth = target
            changed = true
          }
        }
      })
      if (changed && typeof table.doLayout === 'function') {
        table.doLayout()
      }
    })
  }

  // 组件挂载后自动应用一次保存的列宽
  onMounted(() => {
    applySavedWidths()
    // 延迟再执行一次，确保异步渲染的表格也能生效
    setTimeout(applySavedWidths, 300)
  })

  return { colWidth, onHeaderDragEnd, tableRef, applySavedWidths }
}
