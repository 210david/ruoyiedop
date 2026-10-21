import { ref, computed, reactive, toRefs, getCurrentInstance, onMounted } from 'vue'
import { useColumnResize } from './useColumnResize'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled } from '@element-plus/icons-vue'

/**
 * 生产管控模块列表页面通用 composable
 * 统一处理：列宽拖拽、列显隐、筛选条件计数、状态页签计数、字典标签/Badge 辅助
 *
 * @param {string} pageKey - 页面唯一标识（用于 localStorage 隔离）
 * @param {string[]} dictNames - 需要加载的字典名称数组
 * @param {object} defaultColumnsConfig - 列显隐默认配置 { prop: { label, visible } }
 * @param {object} options - 额外选项
 * @param {string} options.statusDict - 状态字典名（用于状态页签和 Badge）
 * @param {object} options.statusBadgeMap - 状态值到 Badge 颜色的映射
 * @param {object} options.statusTabMap - 状态值到 tab class 的映射
 * @param {object} options.extraBadgeDicts - 额外的字典 Badge 映射 { dictName: { valueMap, dictRef } }
 */
export function useMmsListPage(pageKey, dictNames = [], defaultColumnsConfig, options = {}) {
  const { proxy } = getCurrentInstance()
  const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize(pageKey + '_index')

  // 加载字典
  const dictRefs = dictNames.length > 0 ? proxy.useDict(...dictNames) : {}
  const statusDict = options.statusDict ? dictRefs[options.statusDict] : ref([])

  // 列显隐
  const defaultColumns = defaultColumnsConfig

  function loadColumnVisibility() {
    try {
      const saved = localStorage.getItem(pageKey + '_columns')
      if (saved) {
        const parsed = JSON.parse(saved)
        const result = {}
        Object.keys(defaultColumns).forEach(key => {
          result[key] = {
            label: defaultColumns[key].label,
            visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
          }
        })
        return result
      }
    } catch (e) {}
    return JSON.parse(JSON.stringify(defaultColumns))
  }

  const columns = ref(loadColumnVisibility())

  // 状态页签
  const activeStatusTab = ref('all')
  const statusCounts = ref({})

  const statusTabList = computed(() => {
    if (!statusDict.value) return []
    return statusDict.value.map(d => ({ label: d.label, value: d.value }))
  })

  function updateStatusCounts(rows, statusField = 'status') {
    const counts = { all: total.value }
    if (statusDict.value) {
      statusDict.value.forEach(d => {
        counts[d.value] = rows.filter(r => r[statusField] === d.value).length
      })
    }
    statusCounts.value = counts
  }

  function handleStatusTabClick(status, queryParams) {
    activeStatusTab.value = status
    queryParams.status = status === 'all' ? undefined : status
  }

  // 字典辅助函数
  function dictLabel(dictRef, value) {
    if (value === null || value === undefined || value === '') return '—'
    const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef
    if (!arr || !Array.isArray(arr)) return '—'
    const item = arr.find(d => d.value == value)
    return item ? item.label : '—'
  }

  function badgeClass(status) {
    const map = options.statusBadgeMap || {}
    return map[status] || 'gray'
  }

  function statusTabClass(value) {
    const map = options.statusTabMap || {}
    return map[value] || ''
  }

  // 通用状态
  const loading = ref(true)
  const showSearch = ref(true)
  const showAdvanced = ref(false)
  const showStatusHelp = ref(false)
  const ids = ref([])
  const single = ref(true)
  const multiple = ref(true)
  const total = ref(0)
  const dateRange = ref([])

  function handleSelectionChange(selection, idField = 'id') {
    ids.value = selection.map(item => item[idField])
    single.value = selection.length !== 1
    multiple.value = !selection.length
  }

  return {
    proxy,
    colWidth, onHeaderDragEnd, tableRef, applySavedWidths,
    columns, loading, showSearch, showAdvanced, showStatusHelp,
    ids, single, multiple, total, dateRange,
    activeStatusTab, statusCounts, statusTabList,
    updateStatusCounts, handleStatusTabClick,
    dictLabel, badgeClass, statusTabClass,
    Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled,
    ...dictRefs
  }
}
