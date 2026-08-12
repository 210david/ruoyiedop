<template>
  <div class="app-container safety-training-hours-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>人员姓名</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>部门</label>
          <div class="control is-select">
            <el-select v-model="queryParams.deptId" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="item in deptOptions" :key="item.deptId" :label="item.deptName" :value="item.deptId" />
            </el-select>
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

    <!-- ===== Summary Cards ===== -->
    <div class="summary-row">
      <div class="summary-card">
        <div class="summary-icon violet"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
        <div class="summary-body"><div class="summary-label">参训人次</div><div class="summary-value">{{ summaryStats.totalAttendees }}</div></div>
      </div>
      <div class="summary-card">
        <div class="summary-icon green"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
        <div class="summary-body"><div class="summary-label">合格人次</div><div class="summary-value">{{ summaryStats.passCount }}</div></div>
      </div>
      <div class="summary-card">
        <div class="summary-icon blue"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
        <div class="summary-body"><div class="summary-label">累计学时</div><div class="summary-value">{{ summaryStats.totalHours }}</div></div>
      </div>
      <div class="summary-card">
        <div class="summary-icon amber"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
        <div class="summary-body"><div class="summary-label">参训人数</div><div class="summary-value">{{ summaryStats.uniqueUsers }}</div></div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <span class="toolbar-title">学时明细</span>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hoursList" border @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column label="参训人员" prop="userName" key="userName" :width="colWidth('userName', 156)" resizable />
          <el-table-column label="所属部门" prop="deptName" key="deptName" :width="colWidth('deptName', 192)" resizable show-overflow-tooltip />
          <el-table-column label="累计学时" prop="totalHours" key="totalHours" :width="colWidth('totalHours', 144)" resizable align="center">
            <template #default="scope">
              <span class="hours-value">{{ scope.row.totalHours || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="参训次数" prop="attendCount" key="attendCount" :width="colWidth('attendCount', 144)" resizable align="center" />
          <el-table-column label="合格次数" prop="passCount" key="passCount" :width="colWidth('passCount', 144)" resizable align="center">
            <template #default="scope">
              <span class="pass-rate">{{ scope.row.passCount || 0 }} / {{ scope.row.attendCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="合格率" prop="passRate" key="passRate" :width="colWidth('passRate', 144)" resizable align="center">
            <template #default="scope">
              <div class="pass-rate-bar">
                <div class="pass-rate-fill" :style="{ width: calcPassRate(scope.row) + '%' }"></div>
                <span class="pass-rate-text">{{ calcPassRate(scope.row) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="达标状态" key="compliance" :width="colWidth('compliance', 144)" resizable align="center">
            <template #default="scope">
              <span class="badge" :class="(scope.row.totalHours || 0) >= standardHours ? 'green' : 'red'">
                <span class="dot"></span>{{ (scope.row.totalHours || 0) >= standardHours ? '达标' : '未达标' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="SafetyTrainingHours">
import { listHoursStatistics } from '@/api/safety/trainingAttendee'
import { listDept } from '@/api/system/dept'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_training_hours_index')

const hoursList = ref([])
const deptOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

/** 年度达标学时标准（可配置） */
const standardHours = ref(24)

const queryParams = reactive({ pageNum: 1, pageSize: 10, userName: undefined, deptId: undefined, params: {} })

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.userName) count++
  if (queryParams.deptId) count++
  return count
})

const summaryStats = computed(() => {
  const totalAttendees = hoursList.value.reduce((sum, i) => sum + (i.attendCount || 0), 0)
  const passCount = hoursList.value.reduce((sum, i) => sum + (i.passCount || 0), 0)
  const totalHours = hoursList.value.reduce((sum, i) => sum + parseFloat(i.totalHours || 0), 0).toFixed(2)
  const uniqueUsers = hoursList.value.length
  return { totalAttendees, passCount, totalHours, uniqueUsers }
})

function getList() {
  loading.value = true
  listHoursStatistics(queryParams).then(response => {
    hoursList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryParams.userName = undefined; queryParams.deptId = undefined; handleQuery() }

function calcPassRate(row) {
  if (!row.attendCount || row.attendCount === 0) return 0
  return Math.round(((row.passCount || 0) / row.attendCount) * 100)
}

/** 加载部门选项 */
function loadDeptOptions() {
  listDept({ status: '0' }).then(response => {
    deptOptions.value = response.data || []
  })
}

loadDeptOptions()
getList()
</script>

<style scoped>
.safety-training-hours-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-training-hours-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-training-hours-page .filter-card { padding:14px 20px 16px; }
.safety-training-hours-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-training-hours-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-hours-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-training-hours-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-training-hours-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-training-hours-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-training-hours-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-training-hours-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-training-hours-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-training-hours-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-training-hours-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-training-hours-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-training-hours-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-training-hours-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-training-hours-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-training-hours-page .field .control :deep(.el-select) { width:100%; }
.safety-training-hours-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-training-hours-page .field .control.is-select { padding:0; }
.safety-training-hours-page .field .control.is-select :deep(.el-select) { width:100%; }
.safety-training-hours-page .field .control.is-select :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0 12px; min-height:34px; height:34px; }
.safety-training-hours-page .summary-row { display:grid; grid-template-columns:repeat(4, 1fr); gap:8px; margin-bottom:8px; }
.safety-training-hours-page .summary-card { display:flex; align-items:center; gap:14px; padding:18px 20px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); }
.safety-training-hours-page .summary-icon { display:flex; align-items:center; justify-content:center; width:44px; height:44px; border-radius:var(--r-md); }
.safety-training-hours-page .summary-icon.violet { background:var(--violet-50); color:var(--brand-600); }
.safety-training-hours-page .summary-icon.green { background:var(--green-50); color:var(--green-700); }
.safety-training-hours-page .summary-icon.blue { background:var(--blue-50); color:var(--blue-700); }
.safety-training-hours-page .summary-icon.amber { background:var(--amber-50); color:var(--amber-700); }
.safety-training-hours-page .summary-label { font-size:13px; color:var(--ink-500); margin-bottom:4px; }
.safety-training-hours-page .summary-value { font-size:24px; font-weight:700; color:var(--ink-900); }
.safety-training-hours-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-training-hours-page .toolbar-title { font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-hours-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-training-hours-page .table-wrap { overflow-x:auto; }
.safety-training-hours-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-training-hours-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-training-hours-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-training-hours-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-training-hours-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-training-hours-page .hours-value { font-size:16px; font-weight:700; color:var(--brand-600); }
.safety-training-hours-page .pass-rate { font-size:13px; color:var(--ink-500); }
.safety-training-hours-page .pass-rate-bar { position:relative; width:100%; height:24px; background:var(--ink-100); border-radius:var(--r-sm); overflow:hidden; display:flex; align-items:center; justify-content:center; }
.safety-training-hours-page .pass-rate-fill { position:absolute; left:0; top:0; height:100%; background:var(--green-500); border-radius:var(--r-sm); transition:width .3s var(--ease-out); }
.safety-training-hours-page .pass-rate-text { position:relative; z-index:1; font-size:12px; font-weight:600; color:var(--ink-900); }
.safety-training-hours-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-training-hours-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-training-hours-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-training-hours-page .badge.green .dot { background:var(--green-500); }
.safety-training-hours-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-training-hours-page .badge.red .dot { background:var(--red-500); }
.safety-training-hours-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-training-hours-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } .safety-training-hours-page .summary-row { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-training-hours-page .filter-card .filter-bar { grid-template-columns:1fr; } .safety-training-hours-page .summary-row { grid-template-columns:1fr; } }
</style>
