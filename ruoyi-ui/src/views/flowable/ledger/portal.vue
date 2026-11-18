<template>
  <div class="app-container flowable-ledger-portal-page">
    <div class="ledger-layout">
      <!-- 左侧：可见台账（单台账菜单模式下隐藏） -->
      <aside class="surface ledger-side" v-if="!singleMode">
        <div class="side-head">
          <span class="side-title">流程台账</span>
          <span class="side-count" v-if="ledgers.length">{{ ledgers.length }}</span>
        </div>
        <div class="side-body">
          <div v-for="l in ledgers" :key="l.ledgerId" class="ledger-item"
            :class="{ 'is-active': current && current.ledgerId === l.ledgerId }" @click="selectLedger(l)">
            <div class="ledger-name">{{ l.ledgerName }}</div>
            <div class="ledger-desc">{{ l.ledgerDesc || l.formName || '流程数据台账' }}</div>
          </div>
          <el-empty v-if="!ledgers.length && !sideLoading" description="暂无可用台账" :image-size="72" />
        </div>
      </aside>

      <!-- 右侧：台账数据 -->
      <section class="surface ledger-main" v-if="current">
        <div class="main-head">
          <div class="head-left">
            <div class="main-title">{{ current.ledgerName }}</div>
            <div class="main-desc">{{ current.ledgerDesc || '按流程表单配置的台账视图' }}</div>
          </div>
        </div>

        <!-- 筛选 -->
        <div class="filter-card ledger-filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
            <a class="adv-link" v-if="hasAdvanced" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
              <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </a>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>关键字</label>
              <div class="control">
                <el-input v-model="query.keyword" placeholder="标题/流程编号/发起人" clearable @keyup.enter="handleQuery" @clear="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>流程状态</label>
              <div class="control is-select">
                <el-select v-model="query.status" placeholder="全部" clearable @change="handleQuery">
                  <el-option label="审批中" value="running" />
                  <el-option label="已办结" value="completed" />
                  <el-option label="已撤销" value="canceled" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>发起时间</label>
              <div class="control">
                <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                  start-placeholder="开始日期" end-placeholder="结束日期" range-separator="至"
                  unlink-panels style="width:100%" @change="handleQuery" />
              </div>
            </div>
            <div class="field" v-for="c in basicFilterColumns" :key="c.field">
              <label>{{ c.label }}</label>
              <div class="control">
                <el-input v-model="query.filters[c.field]" :placeholder="'筛选' + c.label" clearable
                  @keyup.enter="handleQuery" @clear="handleQuery" />
              </div>
            </div>
            <div class="field" v-for="c in advancedFilterColumns" :key="c.field" v-show="showAdvanced">
              <label>{{ c.label }}</label>
              <div class="control">
                <el-input v-model="query.filters[c.field]" :placeholder="'筛选' + c.label" clearable
                  @keyup.enter="handleQuery" @clear="handleQuery" />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info"><el-icon><Filter /></el-icon> 共 {{ total }} 条数据，支持回车快速搜索</div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- 流程状态统计 -->
        <div class="status-tabs">
          <div class="tabs-track">
            <button class="status-tab" :class="{ 'is-active': activeStatus === 'all' }" @click="handleStatusTab('all')">
              <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
            </button>
            <button v-for="s in STATUS_TABS" :key="s.value" class="status-tab"
              :class="[s.cls, { 'is-active': activeStatus === s.value }]" @click="handleStatusTab(s.value)">
              <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
            </button>
          </div>
        </div>

        <!-- 操作栏 -->
        <div class="ledger-toolbar">
          <div class="left">
            <el-button v-if="canExport" type="primary" plain icon="Download" :loading="exporting" @click="handleExport">
              {{ selection.length ? `导出选中(${selection.length})` : '导出' }}
            </el-button>
            <span v-if="selection.length" class="sel-tip">已勾选 {{ selection.length }} 条</span>
          </div>
          <div class="right">
            <right-toolbar v-if="columns.length" :key="current.ledgerId" v-model:showSearch="showSearch"
              :columns="columns" storageKey="flowable_ledger_portal_columns" @queryTable="loadData" />
          </div>
        </div>

        <!-- 动态台账表格 -->
        <div class="table-wrap">
          <el-table ref="tableRef" v-loading="loading" :data="rows" border class="app-table"
            @sort-change="handleSortChange" @header-dragend="onHeaderDragEnd" @selection-change="handleSelectionChange">
            <el-table-column type="selection" :width="colWidth('勾选', 46)" align="center" />
            <el-table-column type="index" label="序号" :width="colWidth('序号', 66)" align="center" />
            <el-table-column v-for="c in visibleColumns" :key="c.field" :prop="c.field" :label="c.label"
              :min-width="colWidth(c.field, c.width || 140)" :align="c.align || 'left'"
              :sortable="c.sortable ? 'custom' : false" :show-overflow-tooltip="c.format !== 'status'">
              <template #default="scope">
                <template v-if="c.field === 'status'">
                  <span class="badge" :class="statusClass(scope.row[c.field])">
                    <span class="dot"></span>{{ statusLabel(scope.row[c.field]) }}
                  </span>
                </template>
                <template v-else-if="c.field === 'title'">
                  <span class="col-strong link-like" @click="openDetail(scope.row)">{{ scope.row[c.field] || '—' }}</span>
                </template>
                <template v-else-if="c.field === 'processNo' || c.field === 'requestId'">
                  <span class="col-mono link-like" @click="openDetail(scope.row)">{{ scope.row[c.field] === null || scope.row[c.field] === undefined || scope.row[c.field] === '' ? '—' : scope.row[c.field] }}</span>
                </template>
                <template v-else-if="c.format === 'money' || c.format === 'number'">
                  <span class="col-mono">{{ formatNumber(scope.row[c.field]) }}</span>
                </template>
                <template v-else>
                  <span :class="{ 'col-mono': c.field === 'processNo' }">{{ scope.row[c.field] === null || scope.row[c.field] === undefined || scope.row[c.field] === '' ? '—' : scope.row[c.field] }}</span>
                </template>
              </template>
            </el-table-column>
            <el-table-column label="操作" :width="colWidth('操作', 88)" align="center" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无台账数据" :image-size="72" />
            </template>
          </el-table>
        </div>

        <div class="pagination-container">
          <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
            v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
            :page-sizes="[10, 20, 50, 100]" @change="loadData" />
        </div>
      </section>

      <section class="surface ledger-main" v-else-if="!sideLoading">
        <el-empty :description="singleMode ? '暂无权限查看该台账，请联系管理员授权' : '暂无可查看的流程台账，请联系管理员配置台账及数据权限'" :image-size="90" />
      </section>
    </div>
  </div>
</template>

<script setup name="FlowLedgerPortal">
import { myLedgers, ledgerConfig, ledgerData, exportLedger } from '@/api/flowable/ledger'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, ArrowDown } from '@element-plus/icons-vue'

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_ledger_portal')

const { proxy } = getCurrentInstance()
const router = useRouter()
const route = useRoute()

/** 菜单携带的台账ID：一个台账一个菜单时进入单台账模式（隐藏左侧台账列表） */
const menuLedgerId = computed(() => {
  const v = route.query.ledgerId
  return v === undefined || v === null || v === '' ? null : String(v)
})
const singleMode = computed(() => !!menuLedgerId.value)

const ledgers = ref([])
const sideLoading = ref(true)
const current = ref(null)
const columns = ref([])
const canExport = ref(false)
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const exporting = ref(false)
const dateRange = ref([])
const showSearch = ref(true)
const selection = ref([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  status: undefined,
  orderByColumn: undefined,
  isAsc: undefined,
  filters: {}
})

const visibleColumns = computed(() => columns.value.filter(c => c.visible !== false))
const filterColumns = computed(() => visibleColumns.value.filter(c => c.filterable))
// 默认一行 4 个条件：关键字、流程状态、发起时间 + 第 1 个可筛选列，其余放入高级筛选
const basicFilterColumns = computed(() => filterColumns.value.slice(0, 1))
const advancedFilterColumns = computed(() => filterColumns.value.slice(1))
const hasAdvanced = computed(() => advancedFilterColumns.value.length > 0)
const showAdvanced = ref(false)

// 动态列渲染后应用已保存的列宽
watch(columns, () => applySavedWidths(), { deep: true })

const STATUS_MAP = {
  running: { label: '审批中', cls: 'blue' },
  completed: { label: '已办结', cls: 'green' },
  canceled: { label: '已撤销', cls: 'gray' }
}

const STATUS_TABS = [
  { value: 'running', label: '审批中', cls: 'tab-blue' },
  { value: 'completed', label: '已办结', cls: 'tab-green' },
  { value: 'canceled', label: '已撤销', cls: 'tab-gray' }
]

const statusCounts = ref({ all: 0, running: 0, completed: 0, canceled: 0 })
const activeStatus = computed(() => query.status || 'all')

function handleStatusTab(status) {
  query.status = status === 'all' ? undefined : status
  handleQuery()
}

function statusLabel(v) { return STATUS_MAP[v] ? STATUS_MAP[v].label : '—' }
function statusClass(v) { return STATUS_MAP[v] ? STATUS_MAP[v].cls : 'gray' }

function formatNumber(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  return Number.isNaN(n) ? v : n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function findLedger(ledgerId) {
  return ledgers.value.find(l => String(l.ledgerId) === String(ledgerId))
}

function loadMyLedgers() {
  sideLoading.value = true
  myLedgers().then(res => {
    ledgers.value = res.data || []
    if (menuLedgerId.value) {
      const hit = findLedger(menuLedgerId.value)
      if (hit) {
        selectLedger(hit)
      } else {
        current.value = null
      }
    } else if (ledgers.value.length) {
      selectLedger(ledgers.value[0])
    }
  }).finally(() => { sideLoading.value = false })
}

// 同一组件复用于多个台账菜单，菜单切换时按路由参数切换台账
watch(() => route.query.ledgerId, (val) => {
  if (!val) return
  const hit = findLedger(val)
  if (hit) {
    selectLedger(hit)
  } else {
    current.value = null
  }
})

function selectLedger(l) {
  if (current.value && current.value.ledgerId === l.ledgerId) return
  current.value = l
  query.pageNum = 1
  query.keyword = undefined
  query.status = undefined
  query.orderByColumn = undefined
  query.isAsc = undefined
  query.filters = {}
  dateRange.value = []
  rows.value = []
  total.value = 0
  selection.value = []
  statusCounts.value = { all: 0, running: 0, completed: 0, canceled: 0 }
  ledgerConfig(l.ledgerId).then(res => {
    const cfg = res.data || {}
    columns.value = (cfg.columns || []).map((c, i) => ({ ...c, key: c.field || i, visible: c.visible !== false }))
    canExport.value = !!cfg.canExport
    query.orderByColumn = cfg.defaultSort || 'startTime'
    query.isAsc = cfg.defaultSortOrder === 'asc' ? 'ascending' : 'descending'
    loadData()
  }).catch(e => {
    proxy.$modal.msgError('台账配置加载失败：' + (e?.message || e))
  })
}

function buildParams() {
  const params = {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: query.keyword,
    status: query.status,
    orderByColumn: query.orderByColumn,
    isAsc: query.isAsc
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  Object.keys(query.filters || {}).forEach(k => {
    const v = query.filters[k]
    if (v !== undefined && v !== null && String(v).trim() !== '') {
      params['f_' + k] = v
    }
  })
  return params
}

function loadData() {
  if (!current.value) return
  loading.value = true
  ledgerData(current.value.ledgerId, buildParams()).then(res => {
    const data = res.data || {}
    rows.value = data.rows || []
    total.value = data.total || 0
    if (data.statusCounts) {
      statusCounts.value = data.statusCounts
    }
    selection.value = []
    nextTick(() => tableRef.value && tableRef.value.clearSelection())
    canExport.value = !!data.canExport
  }).catch(e => {
    proxy.$modal.msgError('台账数据加载失败：' + (e?.message || e))
  }).finally(() => { loading.value = false })
}

function handleQuery() {
  showAdvanced.value = false
  query.pageNum = 1
  loadData()
}

function resetQuery() {
  showAdvanced.value = false
  query.pageNum = 1
  query.keyword = undefined
  query.status = undefined
  query.filters = {}
  dateRange.value = []
  loadData()
}

function handleSortChange({ prop, order }) {
  if (!order) {
    query.orderByColumn = 'startTime'
    query.isAsc = 'descending'
  } else {
    query.orderByColumn = prop
    query.isAsc = order
  }
  loadData()
}

function openDetail(row) {
  if (!row.instanceId) return
  router.push({
    path: '/flow/detail/create',
    query: { instanceId: row.instanceId, defName: current.value ? current.value.ledgerName : '' }
  })
}

function handleSelectionChange(rows) {
  selection.value = rows || []
}

function handleExport() {
  if (!current.value) return
  const params = buildParams()
  if (selection.value.length) {
    params.instanceIds = selection.value.map(r => r.instanceId).filter(id => !!id).join(',')
  }
  exporting.value = true
  exportLedger(current.value.ledgerId, params)
    .catch(e => {
      if (e !== 'cancel' && e !== 'close') {
        proxy.$modal.msgError('导出失败：' + (e?.message || e || '请稍后重试'))
      }
    })
    .finally(() => { exporting.value = false })
}

loadMyLedgers()
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.flowable-ledger-portal-page {
  .ledger-layout {
    display: flex;
    gap: 10px;
    align-items: flex-start;
  }

  .ledger-side {
    width: 260px;
    flex: 0 0 260px;
    padding: 0 0 8px;
  }
  .side-head {
    display: flex; align-items: center; justify-content: space-between;
    padding: 14px 16px 10px; border-bottom: 1px solid var(--ink-200);
  }
  .side-title { font-size: 15px; font-weight: 600; color: var(--ink-900); }
  .side-count {
    min-width: 20px; height: 20px; padding: 0 6px; border-radius: 999px;
    background: var(--brand-50); color: var(--brand-600); font-size: 12px;
    display: inline-flex; align-items: center; justify-content: center; font-weight: 600;
  }
  .side-body { max-height: calc(100vh - 210px); overflow-y: auto; padding: 8px; }

  .ledger-item {
    padding: 10px 12px; border-radius: 8px; cursor: pointer; margin-bottom: 4px;
    border: 1px solid transparent; transition: background .15s var(--ease-out), border-color .15s;
  }
  .ledger-item:hover { background: var(--ink-50); }
  .ledger-item.is-active { background: var(--brand-50); border-color: var(--brand-200); }
  .ledger-name { font-size: 14px; font-weight: 600; color: var(--ink-900); line-height: 20px; }
  .ledger-desc {
    margin-top: 2px; font-size: 12px; color: var(--ink-400); line-height: 18px;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }

  .ledger-main { flex: 1; min-width: 0; }
  .main-head {
    display: flex; align-items: flex-start; justify-content: space-between;
    padding: 16px 20px 12px; border-bottom: 1px solid var(--ink-100);
  }
  .main-title { font-size: 17px; font-weight: 600; color: var(--ink-900); }
  .main-desc { margin-top: 3px; font-size: 12px; color: var(--ink-400); }

  .ledger-filter-card {
    border-bottom: 1px solid var(--ink-100);
  }

  .status-tabs {
    display: flex; align-items: center; gap: 12px;
    padding: 8px 12px; border-bottom: 1px solid var(--ink-200); background: #fff;
  }
  .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0; overflow-x: auto; }
  .tabs-track::-webkit-scrollbar { display: none; }
  .status-tab {
    display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px;
    border: 1px solid transparent; border-radius: var(--r-sm, 6px); background: transparent;
    font-size: 14px; color: var(--ink-500); cursor: pointer; white-space: nowrap;
    transition: all .15s var(--ease-out, ease);

    .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--ink-300); }
    .count {
      min-width: 18px; padding: 1px 6px; border-radius: 999px; background: var(--ink-100);
      color: var(--ink-500); font-size: 12px; font-weight: 600; line-height: 1.4; text-align: center;
    }
    &:hover { background: var(--ink-50); color: var(--ink-700); }
    &.is-active {
      background: var(--brand-50); border-color: var(--brand-200); color: var(--brand-700); font-weight: 600;
      .count { background: var(--brand-600); color: #fff; }
      .dot { background: var(--brand-500); }
    }
  }
  .status-tab.tab-blue .dot { background: #3b82f6; }
  .status-tab.tab-green .dot { background: #10b981; }
  .status-tab.tab-gray .dot { background: #9ca3af; }
  .status-tab.tab-blue.is-active .dot,
  .status-tab.tab-green.is-active .dot,
  .status-tab.tab-gray.is-active .dot { background: var(--brand-500); }

  .ledger-toolbar {
    display: flex; align-items: center; justify-content: space-between;
    padding: 10px 20px; border-bottom: 1px solid var(--ink-200); background: #fff;
    .left { display: flex; align-items: center; gap: 10px; }
    .sel-tip { font-size: 12px; color: var(--ink-500, #64748b); }
  }

  .table-wrap { padding-top: 4px; }

  @media (max-width: 1100px) {
    .ledger-layout { flex-direction: column; }
    .ledger-side { width: 100%; flex: none; }
  }
}
</style>
