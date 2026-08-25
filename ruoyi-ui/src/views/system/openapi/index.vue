<template>
  <div class="app-container sys-openapi-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>应用名称</label>
          <div class="control">
            <el-input v-model="queryParams.appName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
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
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:openapi:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:openapi:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:openapi:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_openapi_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="openapiList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="应用ID" align="center" prop="appId" :width="colWidth('appId', 80)" resizable v-if="columns.appId.visible" />
          <el-table-column label="应用名称" align="center" prop="appName" :show-overflow-tooltip="true" :width="colWidth('appName', 150)" resizable v-if="columns.appName.visible" />
          <el-table-column label="API Key" align="center" prop="appKey" :show-overflow-tooltip="true" :width="colWidth('appKey', 200)" resizable v-if="columns.appKey.visible">
            <template #default="scope">
              <span style="font-family: monospace;">{{ scope.row.appKey }}</span>
            </template>
          </el-table-column>
          <el-table-column label="允许模块" align="center" prop="allowedModules" :width="colWidth('allowedModules', 120)" resizable v-if="columns.allowedModules.visible" />
          <el-table-column label="状态" align="center" prop="status" :width="colWidth('status', 80)" resizable v-if="columns.status.visible">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" :width="colWidth('createTime', 160)" resizable v-if="columns.createTime.visible" />
          <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" :width="colWidth('remark', 200)" resizable v-if="columns.remark.visible" />
          <el-table-column label="操作" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:openapi:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:openapi:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="560px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4" />
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.appId">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.appId }}</span>
          </div>
        </div>
      </template>
      <el-form ref="openapiRef" :model="form" :rules="rules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" /><line x1="9" y1="15" x2="15" y2="15" />
                </svg>
              </span>
              应用信息
            </div>
          </div>
          <div class="rd-card-body">
            <el-form-item label="应用名称" prop="appName">
              <el-input v-model="form.appName" placeholder="请输入对接方应用名称" />
            </el-form-item>
            <el-form-item label="API Key" prop="appKey">
              <el-input v-model="form.appKey" placeholder="留空则自动生成">
                <template #append>
                  <el-button @click="generateKey">生成</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="允许模块" prop="allowedModules">
              <el-input v-model="form.allowedModules" placeholder="* 表示全部，或逗号分隔如 wms,dms" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" />
            </el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listOpenApi, getOpenApi, addOpenApi, updateOpenApi, delOpenApi } from '@/api/system/openapi'
import { Filter } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()

const openapiList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

// ===== 列宽拖拽持久化 =====
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_openapi_index')

// 列显隐配置
const columns = ref({
  appId: { label: '应用ID', visible: true, defaultWidth: 80 },
  appName: { label: '应用名称', visible: true, defaultWidth: 150 },
  appKey: { label: 'API Key', visible: true, defaultWidth: 200 },
  allowedModules: { label: '允许模块', visible: true, defaultWidth: 120 },
  status: { label: '状态', visible: true, defaultWidth: 80 },
  createTime: { label: '创建时间', visible: true, defaultWidth: 160 },
  remark: { label: '备注', visible: true, defaultWidth: 200 }
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    appName: undefined,
    status: undefined
  },
  rules: {
    appName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }],
    appKey: [{ required: true, message: 'API Key不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.appName) count++
  if (queryParams.value.status) count++
  return count
})

function getList() {
  loading.value = true
  listOpenApi(queryParams.value).then(res => {
    openapiList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.appName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.appId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    appId: undefined,
    appName: undefined,
    appKey: undefined,
    allowedModules: '*',
    status: '0',
    remark: undefined
  }
  proxy.resetForm('openapiRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增开放接口应用'
}

function handleUpdate(row) {
  reset()
  const appId = row.appId || ids.value
  getOpenApi(appId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改开放接口应用'
  })
}

function submitForm() {
  proxy.$refs['openapiRef'].validate(valid => {
    if (valid) {
      if (form.value.appId != null) {
        updateOpenApi(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addOpenApi(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const appIds = row.appId || ids.value
  proxy.$modal.confirm('是否确认删除应用编号为"' + appIds + '"的数据项？').then(() => {
    return delOpenApi(appIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function generateKey() {
  form.value.appKey = 'ak-' + Date.now() + '-' + Math.floor(Math.random() * 10000)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-openapi-page {
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
.sys-openapi-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-openapi-page .filter-card { padding:14px 20px 16px; }
.sys-openapi-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-openapi-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-openapi-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-openapi-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-openapi-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-openapi-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-openapi-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-openapi-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-openapi-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-openapi-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-openapi-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-openapi-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-openapi-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-openapi-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-openapi-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-openapi-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-openapi-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-openapi-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-openapi-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-openapi-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-openapi-page .field .control :deep(.el-select) { width:100%; }
.sys-openapi-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-openapi-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-openapi-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-openapi-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-openapi-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-openapi-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-openapi-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-openapi-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-openapi-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-openapi-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-openapi-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-openapi-page .table-wrap { overflow-x:auto; }
.sys-openapi-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-openapi-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-openapi-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-openapi-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-openapi-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-openapi-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-openapi-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-openapi-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-openapi-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-openapi-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-openapi-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-openapi-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-openapi-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-openapi-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-openapi-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-openapi-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-openapi-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-openapi-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-openapi-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-openapi-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-openapi-page .pagination-container :deep(.el-pagination .btn-prev), .sys-openapi-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-openapi-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-openapi-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-openapi-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-openapi-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-openapi-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-openapi-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
