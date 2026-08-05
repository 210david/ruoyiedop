<template>
  <div class="app-container sys-operlog-page">
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
          <label>操作地址</label>
          <div class="control">
            <el-input v-model="queryParams.operIp" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>系统模块</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>操作人员</label>
          <div class="control">
            <el-input v-model="queryParams.operName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.businessType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_oper_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>操作时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD HH:mm:ss" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]" style="width: 100%" />
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
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:operlog:remove']">删除</el-button>
          <el-button type="danger" plain icon="Delete" @click="handleClean" v-hasPermi="['monitor:operlog:remove']">清空</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['monitor:operlog:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_operlog_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="operlogRef" v-loading="loading" :data="operlogList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange" border class="app-table">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="日志编号" align="center" prop="operId" v-if="columns.operId.visible" />
          <el-table-column label="系统模块" align="center" prop="title" :show-overflow-tooltip="true" v-if="columns.title.visible" />
          <el-table-column label="操作类型" align="center" prop="businessType" v-if="columns.businessType.visible">
            <template #default="scope">
              <dict-tag :options="sys_oper_type" :value="scope.row.businessType" />
            </template>
          </el-table-column>
          <el-table-column label="操作人员" align="center" width="110" prop="operName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" v-if="columns.operName.visible" />
          <el-table-column label="操作地址" align="center" prop="operIp" width="130" :show-overflow-tooltip="true" v-if="columns.operIp.visible" />
          <el-table-column label="操作状态" align="center" prop="status" v-if="columns.status.visible">
            <template #default="scope">
              <dict-tag :options="sys_common_status" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column label="操作日期" align="center" prop="operTime" width="180" sortable="custom" :sort-orders="['descending', 'ascending']" v-if="columns.operTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.operTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="消耗时间" align="center" prop="costTime" width="110" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" v-if="columns.costTime.visible">
            <template #default="scope">
              <span>{{ scope.row.costTime }}毫秒</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row, scope.index)" v-hasPermi="['monitor:operlog:query']">详细</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <operlog-detail v-model:visible="detailVisible" :row="detailRow" />
  </div>
</template>

<script setup name="Operlog">
import OperlogDetail from './detail'
import { list, delOperlog, cleanOperlog } from "@/api/monitor/operlog"
import { Filter, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { sys_oper_type, sys_common_status } = useDict("sys_oper_type", "sys_common_status")

const operlogList = ref([])
const detailVisible = ref(false)
const loading = ref(true)
const detailRow = ref({})
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const defaultSort = ref({ prop: "operTime", order: "descending" })

// 列显隐配置
const columns = ref({
  operId: { label: '日志编号', visible: true },
  title: { label: '系统模块', visible: true },
  businessType: { label: '操作类型', visible: true },
  operName: { label: '操作人员', visible: true },
  operIp: { label: '操作地址', visible: true },
  status: { label: '操作状态', visible: true },
  operTime: { label: '操作日期', visible: true },
  costTime: { label: '消耗时间', visible: true }
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    operIp: undefined,
    title: undefined,
    operName: undefined,
    businessType: undefined,
    status: undefined
  }
})

const { queryParams, form } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.operIp) count++
  if (queryParams.value.title) count++
  if (queryParams.value.operName) count++
  if (queryParams.value.businessType) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 查询登录日志 */
function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    operlogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.operIp = undefined
  queryParams.value.title = undefined
  queryParams.value.operName = undefined
  queryParams.value.businessType = undefined
  queryParams.value.status = undefined
  queryParams.value.pageNum = 1
  proxy.$refs["operlogRef"].sort(defaultSort.value.prop, defaultSort.value.order)
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.operId)
  multiple.value = !selection.length
}

/** 排序触发事件 */
function handleSortChange(column, prop, order) {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order
  getList()
}

/** 详细按钮操作 */
function handleDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

/** 删除按钮操作 */
function handleDelete(row) {
  const operIds = row.operId || ids.value
  proxy.$modal.confirm('是否确认删除日志编号为"' + operIds + '"的数据项?').then(function () {
    return delOperlog(operIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有操作日志数据项?").then(function () {
    return cleanOperlog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/operlog/export",{
    ...proxy.addDateRange(queryParams.value, dateRange.value),
  }, `config_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-operlog-page {
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
.sys-operlog-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-operlog-page .filter-card { padding:14px 20px 16px; }
.sys-operlog-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-operlog-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-operlog-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-operlog-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-operlog-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-operlog-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-operlog-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-operlog-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-operlog-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-operlog-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-operlog-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-operlog-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-operlog-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-operlog-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-operlog-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-operlog-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-operlog-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-operlog-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-operlog-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-operlog-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-operlog-page .field .control :deep(.el-select) { width:100%; }
.sys-operlog-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-operlog-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-operlog-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-operlog-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-operlog-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-operlog-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-operlog-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-operlog-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-operlog-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-operlog-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-operlog-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-operlog-page .table-wrap { overflow-x:auto; }
.sys-operlog-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-operlog-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-operlog-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-operlog-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-operlog-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-operlog-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-operlog-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-operlog-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-operlog-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-operlog-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-operlog-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-operlog-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-operlog-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-operlog-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-operlog-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-operlog-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-operlog-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-operlog-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-operlog-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-operlog-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-operlog-page .pagination-container :deep(.el-pagination .btn-prev), .sys-operlog-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-operlog-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-operlog-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-operlog-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-operlog-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-operlog-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-operlog-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
