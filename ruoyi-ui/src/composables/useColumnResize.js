import { ref, nextTick, onMounted } from 'vue'
import { getTableWidths, saveTableWidth } from '@/api/system/tableConfig'

/**
 * 表格列宽拖拽持久化 composable
 * 配合 el-table 的 @header-dragend 事件和 :width 绑定使用
 *
 * 双重存储策略：
 * 1. localStorage —— 快速本地缓存，保证即时响应和无网络时可用
 * 2. 后端数据库 —— 跨用户/跨设备同步
 *
 * 后端优先级：个人配置 > 全局配置（管理员设置） > 默认宽度
 * - 管理员拖动 → 保存为全局配置，对所有用户生效
 * - 普通用户拖动 → 保存为个人配置，仅对自己生效
 *
 * 加载流程：
 * 1. 组件初始化时先读 localStorage（同步，无闪烁）
 * 2. onMounted 后请求后端获取合并后的列宽（个人覆盖全局）
 * 3. 若后端数据与本地不同，更新并重新应用
 *
 * @param {string} storageKey localStorage 存储键名 + 后端 tableKey（按页面隔离）
 * @returns {{ colWidth, onHeaderDragEnd, tableRef, applySavedWidths }}
 */
export function useColumnResize(storageKey) {
  const savedWidths = ref({})
  const tableRef = ref(null)
  let backendLoaded = false

  // 1. 初始化：先从 localStorage 快速读取（同步，避免渲染闪烁）
  try {
    savedWidths.value = JSON.parse(localStorage.getItem(storageKey) || '{}')
  } catch (e) {
    savedWidths.value = {}
  }

  /**
   * 获取列宽：优先使用保存的值，否则返回默认值
   * 保存的宽度不能小于默认宽度的 80%，防止折行
   */
  function colWidth(prop, defaultWidth) {
    const saved = savedWidths.value[prop]
    if (saved && saved >= defaultWidth * 0.8) {
      return saved
    }
    return defaultWidth
  }

  /**
   * el-table header-dragend 事件回调，保存拖拽后的列宽
   * 同时写入 localStorage（同步）和后端（异步，不阻塞UI）
   */
  function onHeaderDragEnd(newWidth, oldWidth, column) {
    const key = column.property || column.label
    if (!key) return

    const width = Math.round(newWidth)

    // 1. 立即写入 localStorage（保证刷新后仍有效）
    savedWidths.value[key] = width
    localStorage.setItem(storageKey, JSON.stringify(savedWidths.value))

    // 2. 异步写入后端（不阻塞UI，失败时静默处理，localStorage 仍为备份）
    saveTableWidth(storageKey, key, width).catch(() => {
      // 后端保存失败时静默处理，localStorage 已有备份
    })
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

  /**
   * 从后端加载列宽配置（个人覆盖全局）
   * 加载成功后更新本地缓存并重新应用
   */
  function loadFromBackend() {
    getTableWidths(storageKey).then(res => {
      backendLoaded = true
      const widths = res.widths || {}
      // 合并：后端数据优先，但保留 localStorage 中后端没有的列
      const merged = { ...savedWidths.value, ...widths }
      savedWidths.value = merged
      // 同步更新 localStorage 作为缓存
      localStorage.setItem(storageKey, JSON.stringify(merged))
      // 重新应用列宽
      applySavedWidths()
    }).catch(() => {
      // 后端加载失败时静默处理，使用 localStorage 中的数据
    })
  }

  // 组件挂载后：先应用 localStorage 中的列宽，再从后端加载
  onMounted(() => {
    // 1. 立即应用 localStorage 中的列宽
    applySavedWidths()

    // 2. 延迟再执行一次，确保异步渲染的表格也能生效
    setTimeout(applySavedWidths, 300)

    // 3. 从后端加载列宽（个人 > 全局），加载后自动重新应用
    loadFromBackend()
  })

  return { colWidth, onHeaderDragEnd, tableRef, applySavedWidths }
}
