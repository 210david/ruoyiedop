<template>
  <div class="app-container sys-notice-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>公告标题</label>
          <div class="control">
            <el-input v-model="queryParams.noticeTitle" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>操作人员</label>
          <div class="control">
            <el-input v-model="queryParams.createBy" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.noticeType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_notice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:notice:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:notice:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:notice:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_notice_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange" border class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="序号" align="center" prop="noticeId" width="100" v-if="columns.noticeId.visible" />
          <el-table-column label="公告标题" align="center" :show-overflow-tooltip="true" v-if="columns.noticeTitle.visible">
            <template #default="scope">
              <a class="link-type" style="cursor:pointer" @click="handleViewData(scope.row)">{{ scope.row.noticeTitle }}</a>
            </template>
          </el-table-column>
          <el-table-column label="公告类型" align="center" prop="noticeType" width="100" v-if="columns.noticeType.visible">
            <template #default="scope">
              <dict-tag :options="sys_notice_type" :value="scope.row.noticeType" />
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" width="100" v-if="columns.status.visible">
            <template #default="scope">
              <dict-tag :options="sys_notice_status" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column label="创建者" align="center" prop="createBy" width="100" v-if="columns.createBy.visible" />
          <el-table-column label="创建时间" align="center" prop="createTime" width="100" v-if="columns.createTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="User" @click="handleReadUsers(scope.row)" v-hasPermi="['system:notice:list']">阅读用户</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:notice:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:notice:remove']">删除</el-button>
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

    <!-- 添加或修改公告对话框 -->
    <el-dialog v-model="open" width="860px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 11l18-5v12L3 14v-3z" /><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6" />
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.noticeId">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.noticeId }}</span>
          </div>
        </div>
      </template>
      <el-form ref="noticeRef" :model="form" :rules="rules" label-width="80px">
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
              基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="公告标题" prop="noticeTitle">
                  <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="公告类型" prop="noticeType">
                  <el-select v-model="form.noticeType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="dict in sys_notice_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_notice_status" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="4" y1="6" x2="20" y2="6" /><line x1="4" y1="12" x2="20" y2="12" /><line x1="4" y1="18" x2="14" y2="18" />
                </svg>
              </span>
              公告内容
            </div>
          </div>
          <div class="rd-card-body">
            <el-form-item label="内容">
              <editor v-model="form.noticeContent" :min-height="192"/>
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
    <notice-detail-view ref="noticeViewRef" />
    <read-users-dialog ref="readUsersRef" />
  </div>
</template>

<script setup name="Notice">
import NoticeDetailView from "@/layout/components/HeaderNotice/DetailView"
import ReadUsersDialog from "./ReadUsers"
import { listNotice, getNotice, delNotice, addNotice, updateNotice } from "@/api/system/notice"
import { Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { sys_notice_status, sys_notice_type } = useDict("sys_notice_status", "sys_notice_type")

const noticeList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

// 列显隐配置
const columns = ref({
  noticeId: { label: '序号', visible: true },
  noticeTitle: { label: '公告标题', visible: true },
  noticeType: { label: '公告类型', visible: true },
  status: { label: '状态', visible: true },
  createBy: { label: '创建者', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    noticeTitle: undefined,
    createBy: undefined,
    status: undefined
  },
  rules: {
    noticeTitle: [{ required: true, message: "公告标题不能为空", trigger: "blur" }],
    noticeType: [{ required: true, message: "公告类型不能为空", trigger: "change" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.noticeTitle) count++
  if (queryParams.value.createBy) count++
  if (queryParams.value.noticeType) count++
  return count
})

/** 查询公告列表 */
function getList() {
  loading.value = true
  listNotice(queryParams.value).then(response => {
    noticeList.value = response.rows
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
    noticeId: undefined,
    noticeTitle: undefined,
    noticeType: undefined,
    noticeContent: undefined,
    status: "0"
  }
  proxy.resetForm("noticeRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value.noticeTitle = undefined
  queryParams.value.createBy = undefined
  queryParams.value.noticeType = undefined
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.noticeId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加公告"
}

/**修改按钮操作 */
function handleUpdate(row) {
  reset()
  const noticeId = row.noticeId || ids.value
  getNotice(noticeId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改公告"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["noticeRef"].validate(valid => {
    if (valid) {
      if (form.value.noticeId != undefined) {
        updateNotice(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addNotice(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 查看公告详情 */
function handleViewData(row) {
  proxy.$refs["noticeViewRef"].open(row)
}

/** 查看已读用户 */
function handleReadUsers(row) {
  proxy.$refs["readUsersRef"].open(row)
}

/** 删除按钮操作 */
function handleDelete(row) {
  const noticeIds = row.noticeId || ids.value
  proxy.$modal.confirm('是否确认删除公告编号为"' + noticeIds + '"的数据项？').then(function() {
    return delNotice(noticeIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-notice-page {
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
.sys-notice-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-notice-page .filter-card { padding:14px 20px 16px; }
.sys-notice-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-notice-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-notice-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-notice-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.sys-notice-page .filter-card .adv-link:hover { color:var(--brand-600); }
.sys-notice-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.sys-notice-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.sys-notice-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-notice-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-notice-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-notice-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-notice-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-notice-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-notice-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-notice-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-notice-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-notice-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-notice-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-notice-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-notice-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-notice-page .field .control :deep(.el-select) { width:100%; }
.sys-notice-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-notice-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-notice-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-notice-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-notice-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-notice-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-notice-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-notice-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-notice-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-notice-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-notice-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-notice-page .table-wrap { overflow-x:auto; }
.sys-notice-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-notice-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-notice-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-notice-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-notice-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-notice-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-notice-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-notice-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-notice-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-notice-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-notice-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-notice-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-notice-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-notice-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-notice-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-notice-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-notice-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-notice-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-notice-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-notice-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-notice-page .pagination-container :deep(.el-pagination .btn-prev), .sys-notice-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-notice-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-notice-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-notice-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-notice-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-notice-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-notice-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
