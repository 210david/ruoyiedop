<template>
  <div class="app-container qms-pushlog-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>评价周期</label>
          <div class="control">
            <el-input v-model="queryParams.evalPeriod" placeholder="如2026-08" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>推送状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.pushStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option label="待推送" value="0" />
              <el-option label="成功" value="1" />
              <el-option label="失败" value="2" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.grade" placeholder="全部" clearable @change="handleQuery">
              <el-option label="A" value="A" />
              <el-option label="B" value="B" />
              <el-option label="C" value="C" />
              <el-option label="D" value="D" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:sqmPushLog:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:sqmPushLog:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_sqmPushLog_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 180)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />          <el-table-column label="评价周期" prop="evalPeriod" key="evalPeriod" :width="colWidth('evalPeriod', 110)" resizable align="center" v-if="columns.evalPeriod.visible" />
          <el-table-column label="质量评分" prop="qualityScore" key="qualityScore" :width="colWidth('qualityScore', 100)" resizable align="center" v-if="columns.qualityScore.visible" />
          <el-table-column label="等级" prop="grade" key="grade" :width="colWidth('grade', 80)" resizable align="center" v-if="columns.grade.visible">
            <template #default="scope">
              <span class="badge" :class="gradeClass(scope.row.grade)"><span class="dot"></span>{{ scope.row.grade }}</span>
            </template>
          </el-table-column>
          <el-table-column label="合格率(%)" prop="passRate" key="passRate" :width="colWidth('passRate', 100)" resizable align="center" v-if="columns.passRate.visible" />
          <el-table-column label="PPM" prop="ppm" key="ppm" :width="colWidth('ppm', 100)" resizable align="center" v-if="columns.ppm.visible" />
          <el-table-column label="推送状态" prop="pushStatus" key="pushStatus" :width="colWidth('pushStatus', 100)" resizable align="center" v-if="columns.pushStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusClass(scope.row.pushStatus)"><span class="dot"></span>{{ statusLabel(scope.row.pushStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="推送时间" prop="pushTime" key="pushTime" :width="colWidth('pushTime', 160)" resizable align="center" v-if="columns.pushTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.pushTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span></template>
          </el-table-column>
          <el-table-column label="重试次数" prop="retryCount" key="retryCount" :width="colWidth('retryCount', 90)" resizable align="center" v-if="columns.retryCount.visible" />
          <el-table-column label="失败原因" prop="errorMsg" key="errorMsg" :width="colWidth('errorMsg', 200)" resizable show-overflow-tooltip v-if="columns.errorMsg.visible" />
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="QmsSqmPushLog">
import { listSqmPushLog, delSqmPushLog } from '@/api/qms/sqmPushLog'
import { useColumnResize } from '@/composables/useColumnResize'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_sqmPushLog_index')
const { proxy } = getCurrentInstance()

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const selectedIds = ref([])
const defaultColumns = {
  supplierName: { label: '供应商', visible: true },
  evalPeriod: { label: '评价周期', visible: true },
  qualityScore: { label: '质量评分', visible: true },
  grade: { label: '等级', visible: true },
  passRate: { label: '合格率(%)', visible: true },
  ppm: { label: 'PPM', visible: true },
  pushStatus: { label: '推送状态', visible: true },
  pushTime: { label: '推送时间', visible: true },
  retryCount: { label: '重试次数', visible: true },
  errorMsg: { label: '失败原因', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('qms_sqmPushLog_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, params: {} } })
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierName) count++
  if (queryParams.value.evalPeriod) count++
  if (queryParams.value.pushStatus) count++
  if (queryParams.value.grade) count++
  return count
})

function getList() {
  loading.value = true
  listSqmPushLog(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.supplierName = undefined
  queryParams.value.evalPeriod = undefined
  queryParams.value.pushStatus = undefined
  queryParams.value.grade = undefined
  queryParams.value.params = {}
  handleQuery()
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.logId) }
function handleDelete() {
  proxy.$modal.confirm('确认删除选中的推送日志？').then(() => delSqmPushLog(selectedIds.value.join(','))).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}
function handleExport() {
  proxy.download('qms/sqmPushLog/export', { ...queryParams.value }, `sqmPushLog_${new Date().getTime()}.xlsx`)
}
function statusLabel(val) { return { '0': '待推送', '1': '成功', '2': '失败' }[val] || '-' }
function statusClass(val) { return { '0': 'warning', '1': 'success', '2': 'danger' }[val] || '' }
function gradeClass(val) { return { 'A': 'success', 'B': 'info', 'C': 'warning', 'D': 'danger' }[val] || '' }
getList()
</script>

<style scoped>
.qms-pushlog-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46f5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-pushlog-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-pushlog-page .filter-card { padding:14px 20px 16px; }
.qms-pushlog-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-pushlog-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-pushlog-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-pushlog-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-pushlog-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-pushlog-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-pushlog-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-pushlog-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-pushlog-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-pushlog-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-pushlog-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-pushlog-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-pushlog-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-pushlog-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-pushlog-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-pushlog-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-pushlog-page .field .control :deep(.el-select) { width:100%; }
.qms-pushlog-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-pushlog-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-pushlog-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-pushlog-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-pushlog-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-pushlog-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-pushlog-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-pushlog-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-pushlog-page .btn-soft .el-icon { font-size:14px; }
.qms-pushlog-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-pushlog-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-pushlog-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-pushlog-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-pushlog-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-pushlog-page .table-wrap { overflow-x:auto; }
.qms-pushlog-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-pushlog-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-pushlog-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-pushlog-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-pushlog-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-pushlog-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-pushlog-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-pushlog-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-pushlog-page .badge.success { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-pushlog-page .badge.success .dot { background:var(--green-500); }
.qms-pushlog-page .badge.warning { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-pushlog-page .badge.warning .dot { background:#f59e0b; }
.qms-pushlog-page .badge.danger { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-pushlog-page .badge.danger .dot { background:var(--red-500); }
.qms-pushlog-page .badge.info { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-pushlog-page .badge.info .dot { background:var(--brand-500); }
@media (max-width:1100px) { .qms-pushlog-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-pushlog-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-pushlog-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
