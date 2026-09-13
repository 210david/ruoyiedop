<template>
   <div class="app-container sys-role-auth-page">
      <!-- Filter Card -->
      <div class="surface filter-card" v-show="showSearch">
         <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
         </div>
         <div class="filter-bar">
            <div class="field">
               <label>用户名称</label>
               <div class="control">
                  <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery">
                     <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
               </div>
            </div>
            <div class="field">
               <label>手机号码</label>
               <div class="control">
                  <el-input v-model="queryParams.phonenumber" placeholder="请输入" clearable @keyup.enter="handleQuery">
                     <template #prefix><el-icon><Iphone /></el-icon></template>
                  </el-input>
               </div>
            </div>
         </div>
         <div class="filter-actions">
            <div class="filter-info">
               <el-icon><Filter /></el-icon> 支持回车快速搜索
            </div>
            <div class="filter-buttons">
               <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
               <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </div>
         </div>
      </div>
      <!-- Table Section -->
      <div class="surface">
         <!-- Toolbar -->
         <div class="toolbar">
            <div class="left">
               <el-button type="primary" plain icon="Plus" @click="openSelectUser" v-hasPermi="['system:role:add']">添加用户</el-button>
               <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="cancelAuthUserAll" v-hasPermi="['system:role:remove']">
                  <el-icon><CircleClose /></el-icon> 批量取消授权
               </button>
               <div class="toolbar-divider"></div>
               <button type="button" class="btn-soft is-outline" @click="handleClose">
                  <el-icon><Back /></el-icon> 关闭
               </button>
            </div>
            <div class="right">
               <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
            </div>
         </div>

         <!-- Table -->
         <div class="table-wrap">
            <el-table ref="tableRef" v-loading="loading" :data="userList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
               <el-table-column type="selection" width="55" align="center" />
               <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
               <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true" />
               <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true" />
               <el-table-column label="邮箱" prop="email" :show-overflow-tooltip="true" />
               <el-table-column label="手机" prop="phonenumber" :show-overflow-tooltip="true" />
               <el-table-column label="状态" align="center" prop="status">
                  <template #default="scope">
                     <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
                  </template>
               </el-table-column>
               <el-table-column label="创建时间" align="center" prop="createTime" width="180">
                  <template #default="scope">
                     <span>{{ parseTime(scope.row.createTime) }}</span>
                  </template>
               </el-table-column>
               <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                  <template #default="scope">
                     <div class="action-btn-row">
                        <el-button link type="primary" icon="CircleClose" @click="cancelAuthUser(scope.row)" v-hasPermi="['system:role:remove']">取消授权</el-button>
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
      <select-user ref="selectRef" :roleId="queryParams.roleId" @ok="handleQuery" />
   </div>
</template>

<script setup name="AuthUser">
import selectUser from "./selectUser"
import { allocatedUserList, authUserCancel, authUserCancelAll } from "@/api/system/role"
import { useColumnResize } from '@/composables/useColumnResize'

const route = useRoute()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_role_authUser_index')
const { sys_normal_disable } = useDict("sys_normal_disable")

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const userIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: route.params.roleId,
  userName: undefined,
  phonenumber: undefined,
})

/** 查询授权用户列表 */
function getList() {
  loading.value = true
  allocatedUserList(queryParams).then(response => {
    userList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 返回按钮 */
function handleClose() {
  const obj = { path: "/system/role" }
  proxy.$tab.closeOpenPage(obj)
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.userName = undefined
  queryParams.phonenumber = undefined
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  userIds.value = selection.map(item => item.userId)
  multiple.value = !selection.length
}

/** 打开授权用户表弹窗 */
function openSelectUser() {
  proxy.$refs["selectRef"].show()
}

/** 取消授权按钮操作 */
function cancelAuthUser(row) {
  proxy.$modal.confirm('确认要取消该用户"' + row.userName + '"角色吗？').then(function () {
    return authUserCancel({ userId: row.userId, roleId: queryParams.roleId })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消授权成功")
  }).catch(() => {})
}

/** 批量取消授权按钮操作 */
function cancelAuthUserAll() {
  const roleId = queryParams.roleId
  const uIds = userIds.value.join(",")
  proxy.$modal.confirm("是否取消选中用户授权数据项?").then(function () {
    return authUserCancelAll({ roleId: roleId, userIds: uIds })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消授权成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
/* ===== Design Tokens（与角色管理列表页保持一致）===== */
.sys-role-auth-page {
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.sys-role-auth-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-role-auth-page .filter-card { padding:14px 20px 16px; }
.sys-role-auth-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-role-auth-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-role-auth-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-role-auth-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-role-auth-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-role-auth-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-role-auth-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-role-auth-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-role-auth-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-role-auth-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-role-auth-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.sys-role-auth-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-role-auth-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-role-auth-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-role-auth-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-role-auth-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* ===== Toolbar ===== */
.sys-role-auth-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-role-auth-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-role-auth-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-role-auth-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.sys-role-auth-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.sys-role-auth-page .btn-soft .el-icon { font-size:14px; }
.sys-role-auth-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.sys-role-auth-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.sys-role-auth-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.sys-role-auth-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.sys-role-auth-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.sys-role-auth-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.sys-role-auth-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.sys-role-auth-page .table-wrap { overflow-x:auto; }
.sys-role-auth-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-role-auth-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-role-auth-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-role-auth-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-role-auth-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-role-auth-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-role-auth-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-role-auth-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-role-auth-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-role-auth-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Pagination ===== */
.sys-role-auth-page :deep(.pagination-container) { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-role-auth-page :deep(.pagination-container .el-pagination) { justify-content:flex-end; }
.sys-role-auth-page :deep(.pagination-container .el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-role-auth-page :deep(.pagination-container .el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-role-auth-page :deep(.pagination-container .el-pagination .btn-prev), .sys-role-auth-page :deep(.pagination-container .el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-role-auth-page :deep(.pagination-container .el-pagination .btn-prev:hover), .sys-role-auth-page :deep(.pagination-container .el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-role-auth-page :deep(.pagination-container .el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-role-auth-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-role-auth-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-role-auth-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
