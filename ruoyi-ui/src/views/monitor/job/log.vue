<template>
  <div class="app-container monitor-joblog-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>任务名称</label>
          <div class="control">
            <el-input v-model="queryParams.jobName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>任务组名</label>
          <div class="control is-select">
            <el-select v-model="queryParams.jobGroup" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sys_job_group" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>执行状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sys_common_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>执行时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:job:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <button type="button" class="btn-soft is-danger-outline" @click="handleClean" v-hasPermi="['monitor:job:remove']">
            <el-icon><Delete /></el-icon> 清空
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['monitor:job:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="monitor_joblog_columns"></right-toolbar>
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="jobLogList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="日志编号" prop="jobLogId" key="jobLogId" :width="colWidth('jobLogId', 100)" resizable align="center" v-if="columns.jobLogId.visible" />
          <el-table-column label="任务名称" prop="jobName" key="jobName" :width="colWidth('jobName', 180)" resizable align="center" show-overflow-tooltip v-if="columns.jobName.visible" />
          <el-table-column label="任务组名" prop="jobGroup" key="jobGroup" :width="colWidth('jobGroup', 120)" resizable align="center" v-if="columns.jobGroup.visible">
            <template #default="scope">
              <span class="badge violet">{{ jobGroupLabel(scope.row.jobGroup) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调用目标字符串" prop="invokeTarget" key="invokeTarget" :width="colWidth('invokeTarget', 220)" resizable align="center" show-overflow-tooltip v-if="columns.invokeTarget.visible" />
          <el-table-column label="日志信息" prop="jobMessage" key="jobMessage" :width="colWidth('jobMessage', 200)" resizable align="center" show-overflow-tooltip v-if="columns.jobMessage.visible" />
          <el-table-column label="执行状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status == 0 ? 'green' : 'red'">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="执行时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['monitor:job:query']">详细</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 调度日志详细 -->
    <job-detail v-model:visible="open" :row="form" type="log" />
  </div>
</template>

<script setup name="JobLog">
import JobDetail from './detail'
import { getJob } from "@/api/monitor/job"
import { listJobLog, delJobLog, cleanJobLog } from "@/api/monitor/jobLog"
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { sys_common_status, sys_job_group } = useDict("sys_common_status", "sys_job_group")

const jobLogList = ref([])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('monitor_jobLog_index')
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])
const route = useRoute()

/** 列显隐配置 */
const defaultColumns = {
  jobLogId: { label: '日志编号', visible: true },
  jobName: { label: '任务名称', visible: true },
  jobGroup: { label: '任务组名', visible: true },
  invokeTarget: { label: '调用目标字符串', visible: true },
  jobMessage: { label: '日志信息', visible: true },
  status: { label: '执行状态', visible: true },
  createTime: { label: '执行时间', visible: true },
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('monitor_joblog_columns')
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

/** 已选筛选条件数量 */
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.jobName) count++
  if (queryParams.value.jobGroup) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 任务组名字典标签 */
function jobGroupLabel(value) {
  const item = sys_job_group.value.find(d => d.value == value)
  return item ? item.label : '-'
}

/** 执行状态标签 */
function statusLabel(value) {
  const item = sys_common_status.value.find(d => d.value == value)
  return item ? item.label : '-'
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    jobName: undefined,
    jobGroup: undefined,
    status: undefined
  }
})

const { queryParams, form } = toRefs(data)

/** 查询调度日志列表 */
function getList() {
  loading.value = true
  listJobLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    jobLogList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.jobLogId)
  multiple.value = !selection.length
}

/** 详细按钮操作 */
function handleView(row) {
  open.value = true
  form.value = row
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除选中的调度日志？').then(function () {
    return delJobLog(ids.value)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有调度日志数据项?").then(function () {
    return cleanJobLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/jobLog/export", proxy.withExportIds({
    ...proxy.addDateRange(queryParams.value, dateRange.value),
  }, ids.value, 'jobLogId'), `job_log_${new Date().getTime()}.xlsx`)
}

(() => {
  const jobId = route.params && route.params.jobId
  if (jobId !== undefined && jobId != 0) {
    getJob(jobId).then(response => {
      queryParams.value.jobName = response.data.jobName
      queryParams.value.jobGroup = response.data.jobGroup
      getList()
    })
  } else {
    getList()
  }
})()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.monitor-joblog-page {
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
.monitor-joblog-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.monitor-joblog-page .filter-card { padding:14px 20px 16px; }
.monitor-joblog-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.monitor-joblog-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.monitor-joblog-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.monitor-joblog-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.monitor-joblog-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.monitor-joblog-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.monitor-joblog-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.monitor-joblog-page .field { display:flex; flex-direction:column; gap:6px; }
.monitor-joblog-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.monitor-joblog-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.monitor-joblog-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.monitor-joblog-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.monitor-joblog-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.monitor-joblog-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.monitor-joblog-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.monitor-joblog-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.monitor-joblog-page .field .control :deep(.el-select) { width:100%; }
.monitor-joblog-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.monitor-joblog-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.monitor-joblog-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.monitor-joblog-page .field .control :deep(.el-date-editor) { width:100%; }
.monitor-joblog-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.monitor-joblog-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.monitor-joblog-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.monitor-joblog-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.monitor-joblog-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.monitor-joblog-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.monitor-joblog-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.monitor-joblog-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.monitor-joblog-page .btn-soft .el-icon { font-size:14px; }
.monitor-joblog-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.monitor-joblog-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.monitor-joblog-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.monitor-joblog-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.monitor-joblog-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.monitor-joblog-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.monitor-joblog-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.monitor-joblog-page .table-wrap { overflow-x:auto; }
.monitor-joblog-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.monitor-joblog-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.monitor-joblog-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.monitor-joblog-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.monitor-joblog-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.monitor-joblog-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.monitor-joblog-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.monitor-joblog-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.monitor-joblog-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Badges ===== */
.monitor-joblog-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.monitor-joblog-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.monitor-joblog-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.monitor-joblog-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.monitor-joblog-page .badge.green .dot { background:var(--green-500); }
.monitor-joblog-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.monitor-joblog-page .badge.red .dot { background:var(--red-500); }

/* ===== Pagination ===== */
.monitor-joblog-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.monitor-joblog-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.monitor-joblog-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.monitor-joblog-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.monitor-joblog-page .pagination-container :deep(.el-pagination .btn-prev), .monitor-joblog-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.monitor-joblog-page .pagination-container :deep(.el-pagination .btn-prev:hover), .monitor-joblog-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.monitor-joblog-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .monitor-joblog-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .monitor-joblog-page .filter-card .filter-bar { grid-template-columns:1fr; } .monitor-joblog-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 操作列按钮对齐 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
