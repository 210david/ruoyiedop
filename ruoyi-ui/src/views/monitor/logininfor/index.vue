<template>
  <div class="app-container sys-logininfor-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>登录地址</label>
          <div class="control">
            <el-input v-model="queryParams.ipaddr" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>用户名称</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>登录时间</label>
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
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:logininfor:remove']">删除</el-button>
          <el-button type="danger" plain icon="Delete" @click="handleClean" v-hasPermi="['monitor:logininfor:remove']">清空</el-button>
          <el-button type="primary" plain icon="Unlock" :disabled="single" @click="handleUnlock" v-hasPermi="['monitor:logininfor:unlock']">解锁</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['monitor:logininfor:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_logininfor_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="logininforRef" v-loading="loading" :data="logininforList" @selection-change="handleSelectionChange" :default-sort="defaultSort" @sort-change="handleSortChange" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="访问编号" align="center" prop="infoId" v-if="columns.infoId.visible" />
          <el-table-column label="用户名称" align="center" prop="userName" :show-overflow-tooltip="true" sortable="custom" :sort-orders="['descending', 'ascending']" v-if="columns.userName.visible" />
          <el-table-column label="地址" align="center" prop="ipaddr" :show-overflow-tooltip="true" v-if="columns.ipaddr.visible" />
          <el-table-column label="登录地点" align="center" prop="loginLocation" :show-overflow-tooltip="true" v-if="columns.loginLocation.visible" />
          <el-table-column label="操作系统" align="center" prop="os" :show-overflow-tooltip="true" v-if="columns.os.visible" />
          <el-table-column label="浏览器" align="center" prop="browser" :show-overflow-tooltip="true" v-if="columns.browser.visible" />
          <el-table-column label="登录状态" align="center" prop="status" v-if="columns.status.visible">
            <template #default="scope">
              <dict-tag :options="sys_common_status" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column label="描述" align="center" prop="msg" :show-overflow-tooltip="true" v-if="columns.msg.visible" />
          <el-table-column label="访问时间" align="center" prop="loginTime" sortable="custom" :sort-orders="['descending', 'ascending']" width="180" v-if="columns.loginTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.loginTime) }}</span>
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
  </div>
</template>

<script setup name="Logininfor">
import { list, delLogininfor, cleanLogininfor, unlockLogininfor } from "@/api/monitor/logininfor"

const { proxy } = getCurrentInstance()
const { sys_common_status } = useDict("sys_common_status")

const logininforList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const selectName = ref("")
const total = ref(0)
const dateRange = ref([])
const defaultSort = ref({ prop: "loginTime", order: "descending" })

// 列显隐配置
const columns = ref({
  infoId: { label: '访问编号', visible: true },
  userName: { label: '用户名称', visible: true },
  ipaddr: { label: '地址', visible: true },
  loginLocation: { label: '登录地点', visible: true },
  os: { label: '操作系统', visible: true },
  browser: { label: '浏览器', visible: true },
  status: { label: '登录状态', visible: true },
  msg: { label: '描述', visible: true },
  loginTime: { label: '访问时间', visible: true }
})

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  ipaddr: undefined,
  userName: undefined,
  status: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ipaddr) count++
  if (queryParams.value.userName) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 查询登录日志列表 */
function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    logininforList.value = response.rows
    total.value = response.total
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
  queryParams.value.ipaddr = undefined
  queryParams.value.userName = undefined
  queryParams.value.status = undefined
  queryParams.value.pageNum = 1
  proxy.$refs["logininforRef"].sort(defaultSort.value.prop, defaultSort.value.order)
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.infoId)
  multiple.value = !selection.length
  single.value = selection.length != 1
  selectName.value = selection.map(item => item.userName)
}

/** 排序触发事件 */
function handleSortChange(column, prop, order) {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order
  getList()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const infoIds = row.infoId || ids.value
  proxy.$modal.confirm('是否确认删除访问编号为"' + infoIds + '"的数据项?').then(function () {
    return delLogininfor(infoIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有登录日志数据项?").then(function () {
    return cleanLogininfor()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 解锁按钮操作 */
function handleUnlock() {
  const username = selectName.value
  proxy.$modal.confirm('是否确认解锁用户"' + username + '"数据项?').then(function () {
    return unlockLogininfor(username)
  }).then(() => {
    proxy.$modal.msgSuccess("用户" + username + "解锁成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/logininfor/export", {
    ...proxy.addDateRange(queryParams.value, dateRange.value),
  }, `logininfor_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-logininfor-page {
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
.sys-logininfor-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-logininfor-page .filter-card { padding:14px 20px 16px; }
.sys-logininfor-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-logininfor-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-logininfor-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-logininfor-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-logininfor-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-logininfor-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-logininfor-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-logininfor-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-logininfor-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-logininfor-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-logininfor-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-logininfor-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-logininfor-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-logininfor-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-logininfor-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-logininfor-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-logininfor-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-logininfor-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-logininfor-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-logininfor-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-logininfor-page .field .control :deep(.el-select) { width:100%; }
.sys-logininfor-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-logininfor-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-logininfor-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-logininfor-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-logininfor-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-logininfor-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-logininfor-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-logininfor-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-logininfor-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-logininfor-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-logininfor-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-logininfor-page .table-wrap { overflow-x:auto; }
.sys-logininfor-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-logininfor-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-logininfor-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-logininfor-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-logininfor-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-logininfor-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-logininfor-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-logininfor-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-logininfor-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-logininfor-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-logininfor-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-logininfor-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-logininfor-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-logininfor-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-logininfor-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-logininfor-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-logininfor-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-logininfor-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-logininfor-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-logininfor-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-logininfor-page .pagination-container :deep(.el-pagination .btn-prev), .sys-logininfor-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-logininfor-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-logininfor-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-logininfor-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-logininfor-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-logininfor-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-logininfor-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
