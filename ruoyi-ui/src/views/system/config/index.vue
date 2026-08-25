<template>
  <div class="app-container sys-config-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>参数名称</label>
          <div class="control">
            <el-input v-model="queryParams.configName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>参数键名</label>
          <div class="control">
            <el-input v-model="queryParams.configKey" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>系统内置</label>
          <div class="control is-select">
            <el-select v-model="queryParams.configType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:config:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:config:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:config:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:config:export']">导出</el-button>
          <el-button type="danger" plain icon="Refresh" @click="handleRefreshCache" v-hasPermi="['system:config:remove']">刷新缓存</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_config_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange" border class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="参数主键" align="center" prop="configId" v-if="columns.configId.visible" />
          <el-table-column label="参数名称" align="center" prop="configName" :show-overflow-tooltip="true" v-if="columns.configName.visible" />
          <el-table-column label="参数键名" align="center" prop="configKey" :show-overflow-tooltip="true" v-if="columns.configKey.visible" />
          <el-table-column label="参数键值" align="center" prop="configValue" :show-overflow-tooltip="true" v-if="columns.configValue.visible" />
          <el-table-column label="系统内置" align="center" prop="configType" v-if="columns.configType.visible">
            <template #default="scope">
              <dict-tag :options="sys_yes_no" :value="scope.row.configType" />
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" v-if="columns.remark.visible" />
          <el-table-column label="创建时间" align="center" prop="createTime" width="180" v-if="columns.createTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:config:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:config:remove']">删除</el-button>
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

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog v-model="open" width="560px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.configId">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.configId }}</span>
          </div>
        </div>
      </template>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="80px">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
                </svg>
              </span>
              参数信息
            </div>
          </div>
          <div class="rd-card-body">
            <el-form-item label="参数名称" prop="configName">
              <el-input v-model="form.configName" placeholder="请输入参数名称" />
            </el-form-item>
            <el-form-item label="参数键名" prop="configKey">
              <el-input v-model="form.configKey" placeholder="请输入参数键名" />
            </el-form-item>
            <el-form-item label="参数键值" prop="configValue">
              <el-input v-model="form.configValue" type="textarea" placeholder="请输入参数键值" :rows="3" />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="系统内置" prop="configType">
                  <el-radio-group v-model="form.configType">
                    <el-radio v-for="dict in sys_yes_no" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="3" />
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

<script setup name="Config">
import { listConfig, getConfig, delConfig, addConfig, updateConfig, refreshCache } from "@/api/system/config"
import { Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { sys_yes_no } = useDict("sys_yes_no")

const configList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

// 列显隐配置
const columns = ref({
  configId: { label: '参数主键', visible: true },
  configName: { label: '参数名称', visible: true },
  configKey: { label: '参数键名', visible: true },
  configValue: { label: '参数键值', visible: true },
  configType: { label: '系统内置', visible: true },
  remark: { label: '备注', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configName: undefined,
    configKey: undefined,
    configType: undefined
  },
  rules: {
    configName: [{ required: true, message: "参数名称不能为空", trigger: "blur" }],
    configKey: [{ required: true, message: "参数键名不能为空", trigger: "blur" }],
    configValue: [{ required: true, message: "参数键值不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.configName) count++
  if (queryParams.value.configKey) count++
  if (queryParams.value.configType) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 查询参数列表 */
function getList() {
  loading.value = true
  listConfig(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    configList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    configId: undefined,
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: "Y",
    remark: undefined
  }
  proxy.resetForm("configRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.configName = undefined
  queryParams.value.configKey = undefined
  queryParams.value.configType = undefined
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加参数"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const configId = row.configId || ids.value
  getConfig(configId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改参数"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["configRef"].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const configIds = row.configId || ids.value
  proxy.$modal.confirm('是否确认删除参数编号为"' + configIds + '"的数据项？').then(function () {
    return delConfig(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/config/export", {
    ...proxy.addDateRange(queryParams.value, dateRange.value)
  }, `config_${new Date().getTime()}.xlsx`)
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess("刷新缓存成功")
  })
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-config-page {
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
.sys-config-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-config-page .filter-card { padding:14px 20px 16px; }
.sys-config-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-config-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-config-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-config-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-config-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-config-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-config-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-config-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-config-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-config-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-config-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-config-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-config-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-config-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-config-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-config-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-config-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-config-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-config-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-config-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-config-page .field .control :deep(.el-select) { width:100%; }
.sys-config-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-config-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-config-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-config-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-config-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-config-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-config-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-config-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-config-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-config-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-config-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-config-page .table-wrap { overflow-x:auto; }
.sys-config-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-config-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-config-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-config-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-config-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-config-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-config-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-config-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-config-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-config-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-config-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-config-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-config-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-config-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-config-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-config-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-config-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-config-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-config-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-config-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-config-page .pagination-container :deep(.el-pagination .btn-prev), .sys-config-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-config-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-config-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-config-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-config-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-config-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-config-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
