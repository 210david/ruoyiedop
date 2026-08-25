<template>
  <div class="app-container sys-online-page">
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
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleBatchForceLogout" v-hasPermi="['monitor:online:forceLogout']">批量强退</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_online_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="onlineList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
          @selection-change="handleSelectionChange"
          @header-dragend="onHeaderDragEnd"
          border
          class="app-table"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center">
            <template #default="scope">
              <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="会话编号" align="center" prop="tokenId" key="tokenId" :show-overflow-tooltip="true" :width="colWidth('tokenId', 220)" resizable v-if="columns.tokenId.visible" />
          <el-table-column label="登录名称" align="center" prop="userName" key="userName" :show-overflow-tooltip="true" :width="colWidth('userName', 120)" resizable v-if="columns.userName.visible" />
          <el-table-column label="所属部门" align="center" prop="deptName" key="deptName" :show-overflow-tooltip="true" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="主机" align="center" prop="ipaddr" key="ipaddr" :show-overflow-tooltip="true" :width="colWidth('ipaddr', 130)" resizable v-if="columns.ipaddr.visible" />
          <el-table-column label="登录地点" align="center" prop="loginLocation" key="loginLocation" :show-overflow-tooltip="true" :width="colWidth('loginLocation', 130)" resizable v-if="columns.loginLocation.visible" />
          <el-table-column label="操作系统" align="center" prop="os" key="os" :show-overflow-tooltip="true" :width="colWidth('os', 130)" resizable v-if="columns.os.visible" />
          <el-table-column label="浏览器" align="center" prop="browser" key="browser" :show-overflow-tooltip="true" :width="colWidth('browser', 130)" resizable v-if="columns.browser.visible" />
          <el-table-column label="登录时间" align="center" prop="loginTime" key="loginTime" :width="colWidth('loginTime', 180)" resizable v-if="columns.loginTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.loginTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Delete" @click="handleForceLogout(scope.row)" v-hasPermi="['monitor:online:forceLogout']">强退</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="pageNum"
        v-model:limit="pageSize"
      />
    </div>
  </div>
</template>

<script setup name="Online">
import { forceLogout, list as initData } from "@/api/monitor/online"
import { useColumnResize } from '@/composables/useColumnResize'
import { Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const onlineList = ref([])
const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const dateRange = ref([])

// 列宽拖拽持久化
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_online_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  tokenId: { label: '会话编号', visible: true },
  userName: { label: '登录名称', visible: true },
  deptName: { label: '所属部门', visible: true },
  ipaddr: { label: '主机', visible: true },
  loginLocation: { label: '登录地点', visible: true },
  os: { label: '操作系统', visible: true },
  browser: { label: '浏览器', visible: true },
  loginTime: { label: '登录时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('sys_online_columns')
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

const queryParams = ref({
  ipaddr: undefined,
  userName: undefined
})

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ipaddr) count++
  if (queryParams.value.userName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 查询在线用户列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginLoginTime = dateRange.value[0]
    params.endLoginTime = dateRange.value[1]
  }
  initData(params).then(response => {
    onlineList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  pageNum.value = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.ipaddr = undefined
  queryParams.value.userName = undefined
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.tokenId)
  multiple.value = !selection.length
}

/** 强退按钮操作 */
function handleForceLogout(row) {
  proxy.$modal.confirm('是否确认强退名称为"' + row.userName + '"的用户?').then(function () {
    return forceLogout(row.tokenId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 批量强退 */
function handleBatchForceLogout() {
  proxy.$modal.confirm('是否确认强退选中的' + ids.value.length + '个用户?').then(function () {
    const promises = ids.value.map(tokenId => forceLogout(tokenId))
    return Promise.all(promises)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("批量强退成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-online-page {
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
.sys-online-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-online-page .filter-card { padding:14px 20px 16px; }
.sys-online-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-online-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-online-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-online-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-online-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-online-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-online-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-online-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-online-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-online-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-online-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-online-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-online-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-online-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-online-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-online-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-date-picker transparent inside .control */
.sys-online-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-online-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-online-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-online-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-online-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-online-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-online-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-online-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-online-page .table-wrap { overflow-x:auto; }
.sys-online-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-online-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-online-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-online-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-online-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-online-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-online-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-online-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-online-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-online-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Pagination ===== */
.sys-online-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-online-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-online-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-online-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-online-page .pagination-container :deep(.el-pagination .btn-prev), .sys-online-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-online-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-online-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-online-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-online-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-online-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-online-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
