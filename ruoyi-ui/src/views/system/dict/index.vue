<template>
   <div class="app-container tree-sidebar-manage-wrap sys-dict-page">
      <tree-panel title="字典分类" :tree-data="categoryOptions" search-placeholder="请输入分类名称" storage-key="dict-category-sidebar-width" :defaultExpandAll="true" @node-click="handleNodeClick" @refresh="getCategoryTree" ref="categoryTreeRef" />
      <div class="tree-sidebar-content">
         <div class="content-inner">
            <!-- ===== Filter Card ===== -->
            <div class="surface filter-card" v-show="showSearch">
               <div class="filter-head">
                  <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
               </div>
               <div class="filter-bar">
                  <div class="field">
                     <label>字典名称</label>
                     <div class="control">
                        <el-input v-model="queryParams.dictName" placeholder="请输入" clearable @keyup.enter="handleQuery">
                           <template #prefix><el-icon><Search /></el-icon></template>
                        </el-input>
                     </div>
                  </div>
                  <div class="field">
                     <label>字典类型</label>
                     <div class="control">
                        <el-input v-model="queryParams.dictType" placeholder="请输入" clearable @keyup.enter="handleQuery">
                           <template #prefix><el-icon><Key /></el-icon></template>
                        </el-input>
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
                     <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:dict:add']">新增</el-button>
                     <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['system:dict:edit']">
                        <el-icon><Edit /></el-icon> 修改
                     </button>
                     <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:dict:remove']">
                        <el-icon><Delete /></el-icon> 删除
                     </button>
                     <div class="toolbar-divider"></div>
                     <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['system:dict:export']">
                        <el-icon><Download /></el-icon> 导出
                     </button>
                     <button type="button" class="btn-soft is-danger-outline" @click="handleRefreshCache" v-hasPermi="['system:dict:remove']">
                        <el-icon><Refresh /></el-icon> 刷新缓存
                     </button>
                  </div>
                  <div class="right">
                     <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_dict_columns" />
                  </div>
               </div>

               <!-- Table -->
               <div class="table-wrap">
                  <el-table ref="tableRef" v-loading="loading" :data="typeList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
<el-table-column label="字典编号" align="center" prop="dictId" :width="colWidth('dictId', 120)" resizable v-if="columns.dictId.visible" />
                     <el-table-column label="字典名称" align="center" prop="dictName" :show-overflow-tooltip="true" :min-width="colWidth('dictName', 120)" resizable v-if="columns.dictName.visible" />
                     <el-table-column label="字典分类" align="center" key="dictCategory" prop="dictCategory" :width="colWidth('dictCategory', 120)" resizable v-if="columns.dictCategory.visible">
                        <template #default="scope">
                           <el-tag :type="getCategoryTagType(scope.row.dictCategory)" size="small">{{ getCategoryLabel(scope.row.dictCategory) }}</el-tag>
                        </template>
                     </el-table-column>
                     <el-table-column label="字典类型" align="center" prop="dictType" :show-overflow-tooltip="true" :width="colWidth('dictType', 140)" resizable v-if="columns.dictType.visible">
                        <template #default="scope">
                           <a class="link-type" style="cursor:pointer" @click="handleViewData(scope.row)">{{ scope.row.dictType }}</a>
                        </template>
                     </el-table-column>
                     <el-table-column label="状态" align="center" prop="status" :width="colWidth('status', 100)" resizable v-if="columns.status.visible">
                        <template #default="scope">
                           <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
                        </template>
                     </el-table-column>
                     <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" :min-width="colWidth('remark', 200)" resizable v-if="columns.remark.visible" />
                     <el-table-column label="创建时间" align="center" prop="createTime" :width="colWidth('createTime', 180)" resizable v-if="columns.createTime.visible">
                        <template #default="scope">
                           <span>{{ parseTime(scope.row.createTime) }}</span>
                        </template>
                     </el-table-column>
                     <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                        <template #default="scope">
                           <div class="action-btn-row">
                              <el-tooltip content="修改" placement="top">
                                 <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:dict:edit']"></el-button>
                              </el-tooltip>
                              <el-tooltip content="字典数据" placement="top">
                                 <el-button link type="primary" icon="Operation" @click="handleDataList(scope.row)" v-hasPermi="['system:dict:edit']"></el-button>
                              </el-tooltip>
                              <el-tooltip content="删除" placement="top">
                                 <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:dict:remove']"></el-button>
                              </el-tooltip>
                           </div>
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

            <!-- 添加或修改字典类型对话框 -->
            <el-dialog v-model="open" width="600px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
               <template #header>
                  <div class="rd-detail-header">
                     <div class="rd-detail-header-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                           <path d="M4 5h16" /><path d="M4 12h16" /><path d="M4 19h16" />
                           <circle cx="8" cy="5" r="1.5" fill="currentColor" /><circle cx="16" cy="12" r="1.5" fill="currentColor" /><circle cx="8" cy="19" r="1.5" fill="currentColor" />
                        </svg>
                     </div>
                     <span class="rd-detail-header-title">{{ title }}</span>
                     <div class="rd-detail-header-sub" v-if="form.dictId">
                        <div class="rd-detail-header-divider"></div>
                        <span class="rd-detail-header-no">编号：{{ form.dictId }}</span>
                     </div>
                  </div>
               </template>
               <el-form ref="dictRef" :model="form" :rules="rules" label-width="100px">
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
                           字典信息
                        </div>
                     </div>
                     <div class="rd-card-body">
                        <el-form-item label="字典名称" prop="dictName">
                           <el-input v-model="form.dictName" placeholder="请输入字典名称" />
                        </el-form-item>
                        <el-form-item label="字典分类" prop="dictCategory">
                           <el-select v-model="form.dictCategory" placeholder="请选择字典分类" style="width: 100%">
                              <el-option v-for="dict in sys_dict_category" :key="dict.value" :label="dict.label" :value="dict.value" />
                           </el-select>
                        </el-form-item>
                        <el-form-item prop="dictType">
                           <el-input v-model="form.dictType" placeholder="请输入字典类型" />
                           <template #label>
                              <span>
                                 <el-tooltip content='数据存储中的Key值，如：sys_user_sex' placement="top">
                                    <el-icon><question-filled /></el-icon>
                                 </el-tooltip>
                                 字典类型
                              </span>
                           </template>
                        </el-form-item>
                        <el-form-item label="状态" prop="status">
                           <el-radio-group v-model="form.status">
                              <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                           </el-radio-group>
                        </el-form-item>
                        <el-form-item label="备注" prop="remark">
                           <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="3"></el-input>
                        </el-form-item>
                     </div>
                  </section>
               </el-form>
               <template #footer>
                  <el-button type="primary" @click="submitForm">确 定</el-button>
                  <el-button @click="cancel">取 消</el-button>
               </template>
            </el-dialog>

            <dict-data-drawer v-model:visible="drawerVisible" :row="drawerRow" />
         </div>
      </div>
   </div>
</template>

<script setup name="Dict">
import TreePanel from "@/components/TreePanel"
import DictDataDrawer from './detail'
import useDictStore from '@/store/modules/dict'
import { listType, getType, delType, addType, updateType, refreshCache } from "@/api/system/dict/type"
import { Search, Key, Filter, Edit, Delete, Download, Refresh, RefreshLeft } from "@element-plus/icons-vue"
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { sys_normal_disable, sys_dict_category } = useDict("sys_normal_disable", "sys_dict_category")

const typeList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const categoryOptions = ref([])

// 列宽拖拽持久化
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_dict_index')

// 列显隐配置
const columns = ref({
  dictId: { label: '字典编号', visible: true, defaultWidth: 120 },
  dictName: { label: '字典名称', visible: true, defaultWidth: 120 },
  dictCategory: { label: '字典分类', visible: true, defaultWidth: 120 },
  dictType: { label: '字典类型', visible: true, defaultWidth: 140 },
  status: { label: '状态', visible: true, defaultWidth: 100 },
  remark: { label: '备注', visible: true, defaultWidth: 200 },
  createTime: { label: '创建时间', visible: true, defaultWidth: 180 }
})

const drawerVisible = ref(false)
const drawerRow = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: undefined,
    status: undefined,
    dictCategory: undefined
  },
  rules: {
    dictName: [{ required: true, message: "字典名称不能为空", trigger: "blur" }],
    dictType: [{ required: true, message: "字典类型不能为空", trigger: "blur" }],
    dictCategory: [{ required: true, message: "字典分类不能为空", trigger: "change" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.dictName) count++
  if (queryParams.value.dictType) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 分类标签类型映射 */
const categoryTagTypeMap = {
  system: 'primary',
  dms: 'success',
  pms: 'warning',
  mk: 'danger',
  mms: 'primary',
  wms: 'info',
  safety: 'warning',
  qms: 'success',
  hr: 'info'
}

/** 获取分类标签类型 */
function getCategoryTagType(category) {
  const dict = sys_dict_category.value?.find(d => d.value === category)
  return dict?.listClass || categoryTagTypeMap[category] || 'info'
}

/** 获取分类标签文本 */
function getCategoryLabel(category) {
  const dict = sys_dict_category.value?.find(d => d.value === category)
  return dict ? dict.label : (category || '未分类')
}

/** 构建字典分类树（从字典数据生成） */
function getCategoryTree() {
  categoryOptions.value = (sys_dict_category.value || []).map(d => ({
    id: d.value,
    label: d.label
  }))
}

/** 查询字典类型列表 */
function getList() {
  loading.value = true
  listType(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    typeList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
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
    dictId: undefined,
    dictName: undefined,
    dictType: undefined,
    dictCategory: "system",
    status: "0",
    remark: undefined
  }
  proxy.resetForm("dictRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
   dateRange.value = []
   queryParams.value.dictName = undefined
   queryParams.value.dictType = undefined
   queryParams.value.status = undefined
   queryParams.value.dictCategory = undefined
   proxy.$refs.categoryTreeRef.setCurrentKey(null)
   handleQuery()
}

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.dictCategory = data.id
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  // 如果当前选中了分类节点，自动填充分类
  if (queryParams.value.dictCategory) {
    form.value.dictCategory = queryParams.value.dictCategory
  }
  open.value = true
  title.value = "添加字典类型"
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 字典数据抽屉 */
function handleViewData(row) {
  drawerRow.value = row
  drawerVisible.value = true
}

/** 字典数据列表页面 */
function handleDataList(row) {
  proxy.$tab.openPage("字典数据", '/system/dict-data/index/' + row.dictId)
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const dictId = row.dictId || ids.value
  getType(dictId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改字典类型"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dictRef"].validate(valid => {
    if (valid) {
      if (form.value.dictId != undefined) {
        updateType(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addType(form.value).then(response => {
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
  const dictIds = row.dictId || ids.value
  proxy.$modal.confirm('是否确认删除选中的字典类型？').then(function() {
    return delType(dictIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/dict/type/export", proxy.withExportIds({
    ...proxy.addDateRange(queryParams.value, dateRange.value)
  }, ids.value, 'dictId'), `dict_${new Date().getTime()}.xlsx`)
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess("刷新成功")
    useDictStore().cleanDict()
  })
}

getList()

// 字典数据加载完成后构建分类树
watch(() => sys_dict_category.value, (val) => {
  if (val && val.length) {
    getCategoryTree()
  }
}, { immediate: true })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-dict-page {
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
.sys-dict-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-dict-page .filter-card { padding:14px 20px 16px; }
.sys-dict-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-dict-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-dict-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-dict-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-dict-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-dict-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-dict-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-dict-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-dict-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-dict-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-dict-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-dict-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-dict-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-dict-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-dict-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-dict-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-dict-page .field .control :deep(.el-select) { width:100%; }
.sys-dict-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-dict-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-dict-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-dict-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-dict-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-dict-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-dict-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-dict-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-dict-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-dict-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-dict-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons (btn-soft) ===== */
.sys-dict-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.sys-dict-page .btn-soft .el-icon { font-size:14px; }
.sys-dict-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.sys-dict-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.sys-dict-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.sys-dict-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.sys-dict-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.sys-dict-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.sys-dict-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.sys-dict-page .table-wrap { overflow-x:auto; }
.sys-dict-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-dict-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-dict-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-dict-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-dict-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-dict-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-dict-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-dict-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-dict-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-dict-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Dict Tag Badge Style ===== */
.sys-dict-page .app-table :deep(.el-tag) { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; height:auto; }
.sys-dict-page .app-table :deep(.el-tag.el-tag--primary) { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.sys-dict-page .app-table :deep(.el-tag.el-tag--success) { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.sys-dict-page .app-table :deep(.el-tag.el-tag--warning) { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.sys-dict-page .app-table :deep(.el-tag.el-tag--danger) { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.sys-dict-page .app-table :deep(.el-tag.el-tag--info) { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.sys-dict-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-dict-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-dict-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-dict-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-dict-page .pagination-container :deep(.el-pagination .btn-prev), .sys-dict-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-dict-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-dict-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-dict-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* rd-card header cursor default for dict page (no collapse) */
.sys-dict-page .rd-card .rd-card-header { cursor: default; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-dict-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-dict-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-dict-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 操作列按钮对齐 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
