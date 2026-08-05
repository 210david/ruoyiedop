<template>
  <div class="app-container dms-category-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>分类名称</label>
          <div class="control">
            <el-input v-model="queryParams.categoryName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:category:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" @click="toggleExpandAll">
            <el-icon><Sort /></el-icon> 展开/折叠
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_category_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-if="refreshTable" v-loading="loading" :data="categoryList" row-key="categoryId" :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column label="分类名称" prop="categoryName" key="categoryName" :width="colWidth('categoryName', 260)" resizable v-if="columns.categoryName.visible" />
          <el-table-column label="分类编码" prop="categoryCode" key="categoryCode" :width="colWidth('categoryCode', 160)" resizable align="center" v-if="columns.categoryCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.categoryCode }}</span></template>
          </el-table-column>
          <el-table-column label="层级" prop="categoryLevel" key="categoryLevel" :width="colWidth('categoryLevel', 80)" resizable align="center" v-if="columns.categoryLevel.visible" />
          <el-table-column label="排序" prop="orderNum" key="orderNum" :width="colWidth('orderNum', 80)" resizable align="center" v-if="columns.orderNum.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['dms:category:add']">新增</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:category:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:category:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

        <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.categoryCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编码：{{ form.categoryCode }}</span>
          </div>
        </div>
      </template>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row>
              <el-col :span="24" v-if="form.parentId !== 0">
                <el-form-item label="上级分类" prop="parentId">
                  <el-tree-select v-model="form.parentId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="选择上级分类" check-strictly />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="分类编码" prop="categoryCode"><el-input v-model="form.categoryCode" placeholder="请输入" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="分类名称" prop="categoryName"><el-input v-model="form.categoryName" placeholder="请输入" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="显示排序" prop="orderNum"><el-input-number v-model="form.orderNum" :min="0" controls-position="right" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsCategory">
import { listCategory, getCategory, addCategory, updateCategory, delCategory } from '@/api/dms/category'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Sort } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["basic","other"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_category_index')

const categoryList = ref([])
const categoryOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const isExpandAll = ref(true)
const refreshTable = ref(true)
const title = ref('')

const defaultColumns = {
  categoryName: { label: '分类名称', visible: true },
  categoryCode: { label: '分类编码', visible: true },
  categoryLevel: { label: '层级', visible: true },
  orderNum: { label: '排序', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_category_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.categoryName) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { categoryName: undefined },
  rules: {
    categoryCode: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }],
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCategory(queryParams.value).then(res => {
    categoryList.value = proxy.handleTree(res.data, 'categoryId')
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { getList() }
function resetQuery() { queryParams.value.categoryName = undefined; proxy.resetForm('queryRef'); handleQuery() }
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}
function reset() {
  form.value = { parentId: 0, categoryCode: undefined, categoryName: undefined, categoryLevel: 1, orderNum: 0, status: '0', remark: undefined }; proxy.resetForm('categoryRef') }
function handleAdd(row) {
  reset()
  listCategory().then(res => { categoryOptions.value = [{ categoryId: 0, categoryName: '顶级分类', children: proxy.handleTree(res.data, 'categoryId') }] })
  if (row != null && row.categoryId) { form.value.parentId = row.categoryId; form.value.categoryLevel = (row.categoryLevel || 1) + 1 } else { form.value.parentId = 0; form.value.categoryLevel = 1 }
  open.value = true; title.value = '添加设备分类'
}
function handleUpdate(row) {
  reset()
  listCategory().then(res => { categoryOptions.value = [{ categoryId: 0, categoryName: '顶级分类', children: proxy.handleTree(res.data, 'categoryId') }] })
  getCategory(row.categoryId).then(res => { form.value = res.data; open.value = true; title.value = '修改设备分类' })
}
function submitForm() {
  proxy.$refs['categoryRef'].validate(valid => {
    if (valid) {
      if (form.value.categoryId != undefined) { updateCategory(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCategory(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { proxy.$modal.confirm('确认删除"' + row.categoryName + '"？').then(() => delCategory(row.categoryId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
.dms-category-page {
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
.dms-category-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-category-page .filter-card { padding:14px 20px 16px; }
.dms-category-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-category-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-category-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-category-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-category-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-category-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-category-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-category-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-category-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.dms-category-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-category-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-category-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-category-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-category-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-category-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-category-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-category-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-category-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-category-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-category-page .btn-soft .el-icon { font-size:14px; }
.dms-category-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-category-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-category-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-category-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-category-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-category-page .table-wrap { overflow-x:auto; }
.dms-category-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-category-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-category-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-category-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-category-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-category-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-category-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-category-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-category-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); }
.dms-category-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-category-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-category-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-category-page .badge.green .dot { background:var(--green-500); }
.dms-category-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-category-page .badge.gray .dot { background:var(--ink-400); }
@media (max-width:1100px) { .dms-category-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-category-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
