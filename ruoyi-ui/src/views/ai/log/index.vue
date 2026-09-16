<template>
  <div class="app-container ai-log-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>模块</label>
          <div class="control is-select">
            <el-select v-model="queryParams.module" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>功能</label>
          <div class="control">
            <el-input v-model="queryParams.feature" placeholder="如 repair_assistant" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>操作人</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="(label, key) in statusText" :key="key" :label="label" :value="key" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>调用时间</label>
          <div class="control is-date">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange"
              range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== List Card ===== -->
    <div class="surface">
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span>
            <span>{{ s.label }}</span>
            <span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <span class="toolbar-hint"><el-icon><DataLine /></el-icon> AI调用审计日志（只读），记录全平台AI功能调用明细</span>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="ai_log_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="logList" border
          @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="模块" prop="module" key="module" :width="colWidth('module', 110)" resizable align="center" v-if="columns.module.visible">
            <template #default="scope">
              <span class="badge violet">{{ moduleNames[scope.row.module] || scope.row.module }}</span>
            </template>
          </el-table-column>
          <el-table-column label="功能" prop="feature" key="feature" :min-width="colWidth('feature', 160)" resizable show-overflow-tooltip v-if="columns.feature.visible">
            <template #default="scope">
              <span class="col-mono">{{ scope.row.feature }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作人" prop="userName" key="userName" :width="colWidth('userName', 110)" resizable align="center" v-if="columns.userName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusText[scope.row.status] || scope.row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="耗时" prop="durationMs" key="durationMs" :width="colWidth('durationMs', 100)" resizable align="center" v-if="columns.durationMs.visible">
            <template #default="scope">{{ scope.row.durationMs || 0 }} ms</template>
          </el-table-column>
          <el-table-column label="调用时间" prop="callTime" key="callTime" :width="colWidth('callTime', 180)" resizable align="center" v-if="columns.callTime.visible" />
          <el-table-column label="错误信息" prop="errorMsg" key="errorMsg" :min-width="colWidth('errorMsg', 200)" resizable show-overflow-tooltip v-if="columns.errorMsg.visible">
            <template #default="scope">{{ scope.row.errorMsg || '—' }}</template>
          </el-table-column>
          <el-table-column label="提示词摘要" prop="promptDigest" key="promptDigest" :min-width="colWidth('promptDigest', 220)" resizable show-overflow-tooltip v-if="columns.promptDigest.visible">
            <template #default="scope">{{ scope.row.promptDigest || '—' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>
  </div>
</template>

<script setup name="AiLog">
import { listAiLog, aiLogStatusCounts } from '@/api/ai/center'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown, Search, User, Filter, DataLine } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])
const showSearch = ref(true)
const showAdvanced = ref(false)

const moduleNames = {
  dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产', platform: '平台'
}
const statusText = { '0': '成功', '1': '降级', '2': '失败', '3': '限额拦截' }
const statusTabList = [
  { value: '0', label: '成功' },
  { value: '1', label: '降级' },
  { value: '2', label: '失败' },
  { value: '3', label: '限额拦截' }
]

/* 列显隐配置（localStorage持久化） */
const defaultColumns = {
  module: { label: '模块', visible: true },
  feature: { label: '功能', visible: true },
  userName: { label: '操作人', visible: true },
  status: { label: '状态', visible: true },
  durationMs: { label: '耗时', visible: true },
  callTime: { label: '调用时间', visible: true },
  errorMsg: { label: '错误信息', visible: true },
  promptDigest: { label: '提示词摘要', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('ai_log_columns_visibility')
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
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

/* 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('ai_log_index')

/* 状态标签 */
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })

function statusTabClass(value) {
  const map = { '0': 'tab-approved', '1': 'tab-draft', '2': 'tab-reject', '3': 'tab-void' }
  return map[value] || ''
}
function badgeClass(status) {
  const map = { '0': 'green', '1': 'amber', '2': 'red', '3': 'gray' }
  return map[status] || 'gray'
}
function handleStatusTabClick(value) {
  activeStatusTab.value = value
  queryParams.value.status = value === 'all' ? undefined : value
  handleQuery()
}
function loadStatusCounts() {
  aiLogStatusCounts().then(res => {
    const rows = res.data || []
    const counts = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0 }
    rows.forEach(r => {
      const k = String(r.status)
      if (counts[k] !== undefined) counts[k] = Number(r.cnt) || 0
      counts.all += Number(r.cnt) || 0
    })
    statusCounts.value = counts
  }).catch(() => {})
}

/* 筛选条件计数 */
const activeFilterCount = computed(() => {
  let n = 0
  if (queryParams.value.module) n++
  if (queryParams.value.feature) n++
  if (queryParams.value.userName) n++
  if (queryParams.value.status) n++
  if (dateRange.value && dateRange.value.length) n++
  return n
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    module: undefined,
    feature: undefined,
    userName: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listAiLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    logList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryParams.value.module = undefined
  queryParams.value.feature = undefined
  queryParams.value.userName = undefined
  queryParams.value.status = undefined
  queryParams.value.params = {}
  activeStatusTab.value = 'all'
  handleQuery()
}

loadStatusCounts()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.ai-log-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.ai-log-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.ai-log-page .filter-card { padding:14px 20px 16px; }
.ai-log-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.ai-log-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.ai-log-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.ai-log-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.ai-log-page .filter-card .adv-link:hover { color:var(--brand-600); }
.ai-log-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.ai-log-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.ai-log-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.ai-log-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.ai-log-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.ai-log-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.ai-log-page .field { display:flex; flex-direction:column; gap:6px; }
.ai-log-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.ai-log-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.ai-log-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.ai-log-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.ai-log-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.ai-log-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.ai-log-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.ai-log-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.ai-log-page .field .control :deep(.el-select) { width:100%; }
.ai-log-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.ai-log-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.ai-log-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.ai-log-page .field .control :deep(.el-date-editor) { width:100%; }
.ai-log-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.ai-log-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.ai-log-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Status Tabs ===== */
.ai-log-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.ai-log-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.ai-log-page .tabs-track::-webkit-scrollbar { display:none; }
.ai-log-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.ai-log-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.ai-log-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.ai-log-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.ai-log-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.ai-log-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.ai-log-page .status-tab.is-active .dot { background:var(--brand-500); }
.ai-log-page .status-tab.tab-approved .dot { background:var(--green-500); }
.ai-log-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.ai-log-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.ai-log-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.ai-log-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.ai-log-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.ai-log-page .status-tab.tab-reject .dot { background:var(--red-500); }
.ai-log-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.ai-log-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.ai-log-page .status-tab.tab-void .dot { background:var(--ink-400); }

/* ===== Toolbar ===== */
.ai-log-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.ai-log-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.ai-log-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.ai-log-page .toolbar-hint { display:inline-flex; align-items:center; gap:6px; font-size:13px; color:var(--ink-500); }
.ai-log-page .toolbar-hint .el-icon { font-size:14px; color:var(--brand-500); }

/* ===== Table ===== */
.ai-log-page .table-wrap { overflow-x:auto; }
.ai-log-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.ai-log-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.ai-log-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.ai-log-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.ai-log-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.ai-log-page .app-table :deep(.el-table__body td) { padding:13px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.ai-log-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.ai-log-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.ai-log-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.ai-log-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:13px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badges ===== */
.ai-log-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.ai-log-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.ai-log-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.ai-log-page .badge.amber .dot { background:var(--amber-500); }
.ai-log-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.ai-log-page .badge.blue .dot { background:var(--blue-500); }
.ai-log-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.ai-log-page .badge.green .dot { background:var(--green-500); }
.ai-log-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.ai-log-page .badge.red .dot { background:var(--red-500); }
.ai-log-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.ai-log-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.ai-log-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.ai-log-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.ai-log-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.ai-log-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.ai-log-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.ai-log-page .pagination-container :deep(.el-pagination .btn-prev), .ai-log-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.ai-log-page .pagination-container :deep(.el-pagination .btn-prev:hover), .ai-log-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.ai-log-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .ai-log-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .ai-log-page .filter-card .filter-bar { grid-template-columns:1fr; } .ai-log-page .toolbar { flex-wrap:wrap; gap:10px; } .ai-log-page .status-tabs { padding:6px 8px; } }
</style>
