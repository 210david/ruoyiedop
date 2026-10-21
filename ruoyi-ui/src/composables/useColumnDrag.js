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
 * 支持 type="index" 的序号列：
 * - 序号列没有 prop，用 label（如"序号"）作为业务 key
 * - 模板需给序号列显式加 key="序号"，保证 el-table 组件 key 与业务 key 一致
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
  // 顺序可能被后续加载的后端配置覆盖（loadFromBackend），因此这里先给出本地缓存的最优顺序
  const orderStorageKey = storageKey + '_order'
  syncFromList(JSON.parse(localStorage.getItem(orderStorageKey) || '[]'))

  /** 用外部顺序数组同步 sortedColumnKeys（过滤无效 key + 补齐缺失列） */
  function syncFromList(saved) {
    if (!Array.isArray(saved) || saved.length === 0) return false
    const validKeys = new Set(defaultColumnKeys)
    const valid = saved.filter(k => validKeys.has(k))
    if (valid.length === 0) return false
    const missing = defaultColumnKeys.filter(k => !valid.includes(k))
    const merged = [...valid, ...missing]
    sortedColumnKeys.value = merged
    return true
  }

  /**
   * 从后端加载列顺序配置并应用（个人配置 > 全局配置）
   */
  function loadFromBackend() {
    getTableWidths(storageKey).then(res => {
      const orders = res.orders || {}
      const orderedEntries = Object.entries(orders)
      if (orderedEntries.length === 0) return
      const sortedFromBackend = orderedEntries
        .sort((a, b) => a[1] - b[1])
        .map(item => item[0])
      if (syncFromList(sortedFromBackend)) {
        localStorage.setItem(orderStorageKey, JSON.stringify(sortedColumnKeys.value))
        nextTick(initSortable)
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
   * 取列的唯一键：优先 property，其次 key，最后 label
   * 支持 type="index" 的序号列（无 property、无 key 时用 label='序号' 兜底）
   */
  function colKeyOf(col) {
    return col.property || col.columnKey || col.key || col.label || ''
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
      // 排除：多选框（selection）、展开列（expand）、右侧固定操作列
      const columns = table.columns || []
      const draggableIndices = []
      columns.forEach((col, index) => {
        const type = col.type || ''
        if (type === 'selection' || type === 'expand') return
        if (col.fixed === 'right') return
        const key = colKeyOf(col)
        if (key && sortedColumnKeys.value.includes(key)) {
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

          // 3. 更新 sortedColumnKeys 数组并重新渲染列
          const fromTh = initialThOrder[evt.oldIndex]
          const toTh = initialThOrder[evt.newIndex]
          const fromKey = thKeyOf(fromTh)
          const toKey = thKeyOf(toTh)
          const keys = [...sortedColumnKeys.value]

          // 用 th 上的真实 key 更稳妥；取不到时回退到索引映射
          let fromPos = fromKey ? keys.indexOf(fromKey) : -1
          let toPos = toKey ? keys.indexOf(toKey) : -1
          if (fromPos < 0) fromPos = oldKeyIndex
          if (toPos < 0) toPos = newKeyIndex
          if (fromPos < 0 || toPos < 0 || fromPos === toPos) return

          const movedKey = keys.splice(fromPos, 1)[0]
          keys.splice(toPos, 0, movedKey)
          sortedColumnKeys.value = keys

          // 4. 重新排列真实 DOM 表头与表体（Vue 仅管理列组件，不重排已有 th/td 节点）
          applyHeaderOrder(keys)

          // 5. 保存到 localStorage 和后端
          saveColumnOrderToLocal(keys)
        }
      })
    })
  }

  /**
   * 读取 th 上记录的业务列 key（写入位置见 applyHeaderOrder）
   */
  function thKeyOf(th) {
    if (!th) return ''
    return th.dataset.colKey || ''
  }

  /**
   * 按 key 顺序重排真实 DOM 表头与表体，保证刷新前视觉效果立即正确
   * Vue 只管理列组件配置，不会重排已渲染的 th / td 节点，因此这里手动同步
   * @param {string[]} keys 目标列顺序
   */
  function applyHeaderOrder(keys) {
    const table = tableRef.value
    if (!table) return
    const root = table.$el
    const columns = table.columns || []
    const keyList = columns.map(col => colKeyOf(col))

    // 表头 + 表体（含 fixed 层）统一按 key 顺序重排
    const rows = []
    const headerTr = root.querySelector('.el-table__header thead tr')
    if (headerTr) rows.push(headerTr)
    root.querySelectorAll('.el-table__body tbody tr').forEach(tr => rows.push(tr))
    root.querySelectorAll('.el-table__footer tbody tr').forEach(tr => rows.push(tr))
    root.querySelectorAll('.el-table__fixed tbody tr, .el-table__fixed-header-wrapper thead tr').forEach(tr => rows.push(tr))

    rows.forEach(tr => {
      const cells = Array.from(tr.children)
      const used = new Set()
      const keyed = cells.map((cell, i) => ({ cell, key: keyList[i] || '' }))
      const ordered = []
      keys.forEach(key => {
        const hit = keyed.find(item => item.key === key && !used.has(item))
        if (hit) { used.add(hit); ordered.push(hit.cell) }
      })
      // 未纳入顺序管理的列（选择列/操作列等）保持原有相对位置，追加在末尾
      keyed.forEach(item => { if (!used.has(item)) ordered.push(item.cell) })
      ordered.forEach(cell => tr.appendChild(cell))
    })

    if (typeof table.doLayout === 'function') table.doLayout()
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
