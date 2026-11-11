<template>
  <div class="app-container sys-connector-grant-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>用户账号</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <span class="toolbar-title">连接器授权管理</span>
          <div class="toolbar-divider"></div>
          <span class="toolbar-hint">持有 grant_token 的用户可通过 WorkBuddy 连接器按本人数据权限查询平台数据</span>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="edop_grant_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="grantList" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="用户账号" prop="userName" min-width="110" align="center" show-overflow-tooltip v-if="columns.userName.visible" />
          <el-table-column label="部门" prop="deptName" min-width="130" align="center" show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="手机号快照" prop="phoneSnapshot" width="130" align="center" show-overflow-tooltip v-if="columns.phoneSnapshot.visible" />
          <el-table-column label="授权来源" prop="clientDesc" min-width="150" align="center" show-overflow-tooltip v-if="columns.clientDesc.visible" />
          <el-table-column label="状态" prop="status" width="90" align="center" v-if="columns.status.visible">
            <template #default="scope">
              <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column label="最近使用" prop="lastUsedTime" width="160" align="center" v-if="columns.lastUsedTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.lastUsedTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="过期时间" prop="expireTime" width="160" align="center" v-if="columns.expireTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.expireTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="160" align="center" v-if="columns.createTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="danger" icon="CircleClose" :disabled="scope.row.status === '1'" @click="handleRevoke(scope.row)" v-hasPermi="['edop:grant:revoke']">吊销</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

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

<script setup name="ConnectorGrant">
import { listGrant, revokeGrant } from "@/api/system/connectorGrant"
import { Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const grantList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const columns = ref({
  userName: { label: '用户账号', visible: true },
  deptName: { label: '部门', visible: true },
  phoneSnapshot: { label: '手机号快照', visible: true },
  clientDesc: { label: '授权来源', visible: true },
  status: { label: '状态', visible: true },
  lastUsedTime: { label: '最近使用', visible: true },
  expireTime: { label: '过期时间', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  return [queryParams.value.userName, queryParams.value.status].filter(v => v !== undefined && v !== null && v !== '').length
})

/** 查询授权列表 */
function getList() {
  loading.value = true
  listGrant(queryParams.value).then(response => {
    grantList.value = response.rows
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
  proxy.resetForm("queryRef")
  queryParams.value.userName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

/** 吊销按钮操作 */
function handleRevoke(row) {
  proxy.$modal.confirm('确认吊销用户「' + row.userName + '」的连接器授权吗？吊销后该用户需重新授权才能继续使用。').then(() => {
    return revokeGrant(row.grantId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("吊销成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-connector-grant-page {
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
.sys-connector-grant-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-connector-grant-page .filter-card { padding:14px 20px 16px; }
.sys-connector-grant-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-connector-grant-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-connector-grant-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-connector-grant-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-connector-grant-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-connector-grant-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-connector-grant-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-connector-grant-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-connector-grant-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-connector-grant-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-connector-grant-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-connector-grant-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-connector-grant-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-connector-grant-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-connector-grant-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-connector-grant-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-connector-grant-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-connector-grant-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-connector-grant-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-connector-grant-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-connector-grant-page .field .control :deep(.el-select) { width:100%; }
.sys-connector-grant-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-connector-grant-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-connector-grant-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.sys-connector-grant-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-connector-grant-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-connector-grant-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-connector-grant-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-connector-grant-page .table-wrap { overflow-x:auto; }
.sys-connector-grant-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-connector-grant-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-connector-grant-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-connector-grant-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-connector-grant-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-connector-grant-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-connector-grant-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-connector-grant-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-connector-grant-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-connector-grant-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-connector-grant-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-connector-grant-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-connector-grant-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-connector-grant-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-connector-grant-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-connector-grant-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-connector-grant-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-connector-grant-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-connector-grant-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-connector-grant-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-connector-grant-page .pagination-container :deep(.el-pagination .btn-prev), .sys-connector-grant-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-connector-grant-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-connector-grant-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-connector-grant-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-connector-grant-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-connector-grant-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-connector-grant-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
