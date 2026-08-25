import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import Sortable from 'sortablejs'
import { getTableWidths, saveColumnOrder } from '@/api/system/tableConfig'

/**
 * 表格列顺序拖拽持久化 composable
 * 使用 SortableJS 拖动 el-table 表头 th 元素，实现列的左右顺序调整
 *
 * 与 useColumnResize 共存的关键设计：
 * - el-table 原生列宽拖拽依赖 th 右边缘的拖拽区域（约6px宽）
 * - SortableJS 的 filter 选项检测 mousedown 位置，距 th 右边缘 6px 内不启动拖拽
 * - 这样列宽拖拽（拖边框）和列顺序拖拽（拖表头文字）互不干扰
 *
 * 双重存储策略（与 useColumnResize 一致）：
 * 1. localStorage —— 快速本地缓存
 * 2. 后端数据库 —— 跨用户/跨设备同步
 *
 * @param {string} storageKey localStorage 键名 + 后端 tableKey
 * @param {string[]} defaultColumnKeys 默认列顺序数组
 * @param {Ref} tableRef 表格的 ref 引用（与 useColumnResize 共享）
 * @returns {{ sortedColumnKeys, applyColumnOrder, initSortable }}
 */
export function useColumnDrag(storageKey, defaultColumnKeys, tableRef) {
  // 列顺序数组（ref，驱动模板渲染）
  const sortedColumnKeys = ref([...defaultColumnKeys])
  let sortableInstance = null

  // 1. 初始化：先从 localStorage 快速读取
  const orderStorageKey = storageKey + '_order'
  try {
    const saved = JSON.parse(localStorage.getItem(orderStorageKey) || '[]')
    if (Array.isArray(saved) && saved.length === defaultColumnKeys.length) {
      const validKeys = new Set(defaultColumnKeys)
      const allValid = saved.every(k => validKeys.has(k))
      if (allValid) {
        const missing = defaultColumnKeys.filter(k => !saved.includes(k))
        sortedColumnKeys.value = [...saved, ...missing]
      }
    }
  } catch (e) {
    // JSON 解析失败，使用默认顺序
  }

  /**
   * 从后端加载列顺序配置并应用
   */
  function loadFromBackend() {
    getTableWidths(storageKey).then(res => {
      const orders = res.orders || {}
      const orderedEntries = Object.entries(orders)
      if (orderedEntries.length > 0) {
        const sortedFromBackend = orderedEntries
          .sort((a, b) => a[1] - b[1])
          .map(item => item[0])
        const validKeys = new Set(defaultColumnKeys)
        const validFromBackend = sortedFromBackend.filter(k => validKeys.has(k))
        const missing = defaultColumnKeys.filter(k => !validFromBackend.includes(k))
        const newOrder = [...validFromBackend, ...missing]
        if (newOrder.length === defaultColumnKeys.length) {
          sortedColumnKeys.value = newOrder
          localStorage.setItem(orderStorageKey, JSON.stringify(newOrder))
        }
      }
    }).catch(() => {})
  }

  /**
   * 保存当前列顺序到 localStorage 和后端
   */
  function saveColumnOrderToLocal(newOrder) {
    localStorage.setItem(orderStorageKey, JSON.stringify(newOrder))
    newOrder.forEach((key, index) => {
      saveColumnOrder(storageKey, key, index).catch(() => {})
    })
  }

  /**
   * 检测 mousedown 事件是否发生在列边框拖拽区域
   * el-table 的列宽调整手柄在 th 的右边缘约 6px 范围
   * 如果在这个区域内，返回 true（应该跳过 SortableJS，让 el-table 处理列宽拖拽）
   */
  function isResizeHandle(evt) {
    const th = evt.target.closest('th')
    if (!th) return false
    const rect = th.getBoundingClientRect()
    const offsetX = evt.clientX - rect.left
    // 距右边缘 8px 内是列宽拖拽区域
    return offsetX > rect.width - 8
  }

  /**
   * 初始化 SortableJS 拖拽
   */
  function initSortable() {
    nextTick(() => {
      const table = tableRef.value
      if (!table) return

      const thead = table.$el.querySelector('.el-table__header thead tr')
      if (!thead) return

      // 如果已经初始化过，先销毁
      if (sortableInstance) {
        sortableInstance.destroy()
        sortableInstance = null
      }

      const ths = thead.querySelectorAll('th')
      if (ths.length === 0) return

      // 计算哪些 th 是可拖拽的数据列
      const columns = table.columns || []
      const draggableIndices = []
      columns.forEach((col, index) => {
        if (col.property) {
          draggableIndices.push(index)
        }
      })
      if (draggableIndices.length === 0) return

      let initialThOrder = []

      sortableInstance = Sortable.create(thead, {
        animation: 200,
        ghostClass: 'col-drag-ghost',
        chosenClass: 'col-drag-chosen',
        dragClass: 'col-drag-dragging',
        // 关键：filter 返回 true 时不阻止默认事件，让 el-table 的列宽拖拽继续工作
        preventOnFilter: false,
        // 关键：检测 mousedown 位置，列边框区域不启动拖拽
        filter: (evt) => {
          // 1. 排除列边框拖拽区域（让 el-table 处理列宽调整）
          if (isResizeHandle(evt)) return true
          // 2. 排除非数据列
          const th = evt.target.closest('th')
          if (!th) return true
          const allThs = Array.from(thead.querySelectorAll('th'))
          const thIndex = allThs.indexOf(th)
          return !draggableIndices.includes(thIndex)
        },
        // 阻止非数据列被拖入
        onMove: (evt) => {
          const relatedTh = evt.related
          if (!relatedTh) return false
          const allThs = Array.from(thead.querySelectorAll('th'))
          const relatedIndex = allThs.indexOf(relatedTh)
          return draggableIndices.includes(relatedIndex)
        },
        onStart: () => {
          initialThOrder = Array.from(thead.querySelectorAll('th'))
        },
        onEnd: (evt) => {
          // 1. 恢复 DOM 顺序（撤销 SortableJS 对 DOM 的修改），让 Vue 重新渲染
          nextTick(() => {
            initialThOrder.forEach(th => thead.appendChild(th))
          })

          // 2. 只处理数据列之间的拖拽
          if (!draggableIndices.includes(evt.oldIndex) || !draggableIndices.includes(evt.newIndex)) {
            return
          }

          const oldKeyIndex = draggableIndices.indexOf(evt.oldIndex)
          const newKeyIndex = draggableIndices.indexOf(evt.newIndex)
          if (oldKeyIndex === newKeyIndex) return

          // 3. 更新 sortedColumnKeys 数组，让 Vue 重新渲染列
          const newKeys = [...sortedColumnKeys.value]
          const movedKey = newKeys.splice(oldKeyIndex, 1)[0]
          newKeys.splice(newKeyIndex, 0, movedKey)
          sortedColumnKeys.value = newKeys

          // 4. 保存到 localStorage 和后端
          saveColumnOrderToLocal(newKeys)
        }
      })
    })
  }

  /**
   * 应用保存的列顺序（在数据加载后调用）
   */
  function applyColumnOrder() {
    nextTick(() => {
      initSortable()
    })
  }

  // 组件挂载后：初始化拖拽 + 从后端加载
  onMounted(() => {
    setTimeout(() => {
      initSortable()
    }, 300)
    loadFromBackend()
  })

  // 列顺序变化后重新初始化 SortableJS
  watch(sortedColumnKeys, () => {
    nextTick(() => {
      initSortable()
    })
  })

  // 组件卸载前清理
  onBeforeUnmount(() => {
    if (sortableInstance) {
      sortableInstance.destroy()
      sortableInstance = null
    }
  })

  return { sortedColumnKeys, applyColumnOrder, initSortable }
}
