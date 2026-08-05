<template>
  <div class="app-container dms-partstock-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>单据号</label>
          <div class="control">
            <el-input v-model="queryParams.documentCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.moveType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="入库" value="0" />
              <el-option label="出库" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>备件编号</label>
          <div class="control">
            <el-input v-model="queryParams.partCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商/部门</label>
          <div class="control">
            <el-input v-model="queryParams.supplierOrDept" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partstock:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:partstock:export']">
            <el-icon><Download /></el-icon> 导出流水账
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_partstock_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" :default-sort="{ prop: 'createTime', order: 'descending' }" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="单据号" prop="documentCode" key="documentCode" :width="colWidth('documentCode', 140)" resizable v-if="columns.documentCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.documentCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件编号" prop="partCode" key="partCode" :width="colWidth('partCode', 120)" resizable v-if="columns.partCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件名称" prop="partName" key="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip v-if="columns.partName.visible" />
          <el-table-column label="类型" prop="moveType" key="moveType" :width="colWidth('moveType', 80)" resizable align="center" v-if="columns.moveType.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.moveType === '0' ? 'green' : 'amber'">
                <span class="dot"></span>{{ scope.row.moveType === '0' ? '入库' : '出库' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出入库类型" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 100)" resizable align="center" v-if="columns.sourceType.visible">
            <template #default="scope">
              <span class="badge violet">{{ moveTypeLabel(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="供应商/部门" prop="supplierOrDept" key="supplierOrDept" :width="colWidth('supplierOrDept', 150)" resizable show-overflow-tooltip v-if="columns.supplierOrDept.visible" />
          <el-table-column label="数量" prop="quantity" key="quantity" :width="colWidth('quantity', 80)" resizable align="center" v-if="columns.quantity.visible" />
          <el-table-column label="变更前" prop="beforeStock" key="beforeStock" :width="colWidth('beforeStock', 80)" resizable align="center" v-if="columns.beforeStock.visible" />
          <el-table-column label="变更后" prop="afterStock" key="afterStock" :width="colWidth('afterStock', 80)" resizable align="center" v-if="columns.afterStock.visible" />
          <el-table-column label="操作人" prop="operatorName" key="operatorName" :width="colWidth('operatorName', 80)" resizable v-if="columns.operatorName.visible" />
          <el-table-column label="操作时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable v-if="columns.createTime.visible" />
          <el-table-column label="备注" prop="remark" key="remark" :width="colWidth('remark', 150)" resizable show-overflow-tooltip v-if="columns.remark.visible" />
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partstock:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="DmsPartStock">
import { listPartStock, delPartStock } from '@/api/dms/partstock'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, Delete, Download } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partstock_index')
const { dms_partin_type, dms_partout_type } = proxy.useDict('dms_partin_type', 'dms_partout_type')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const defaultColumns = {
  documentCode: { label: '单据号', visible: true },
  partCode: { label: '备件编号', visible: true },
  partName: { label: '备件名称', visible: true },
  moveType: { label: '类型', visible: true },
  sourceType: { label: '出入库类型', visible: true },
  supplierOrDept: { label: '供应商/部门', visible: true },
  quantity: { label: '数量', visible: true },
  beforeStock: { label: '变更前', visible: true },
  afterStock: { label: '变更后', visible: true },
  operatorName: { label: '操作人', visible: true },
  createTime: { label: '操作时间', visible: true },
  remark: { label: '备注', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_partstock_columns')
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
  if (queryParams.value.documentCode) count++
  if (queryParams.value.moveType) count++
  if (queryParams.value.partCode) count++
  if (queryParams.value.supplierOrDept) count++
  return count
})

function moveTypeLabel(row) {
  if (row.moveType === '0') {
    const item = dms_partin_type.value.find(d => d.value == row.sourceType)
    return item ? item.label : '-'
  } else {
    const item = dms_partout_type.value.find(d => d.value == row.targetType)
    return item ? item.label : '-'
  }
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    documentCode: undefined,
    moveType: undefined,
    partCode: undefined,
    supplierOrDept: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listPartStock(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.documentCode = undefined; queryParams.value.moveType = undefined; queryParams.value.partCode = undefined; queryParams.value.supplierOrDept = undefined; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); multiple.value = !selection.length }
function handleExport() { proxy.download('dms/sparepart/partstock/export', { ...queryParams.value }, `partstock_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的流水记录？').then(() => delPartStock(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-partstock-page {
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
.dms-partstock-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-partstock-page .filter-card { padding:14px 20px 16px; }
.dms-partstock-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-partstock-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-partstock-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-partstock-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-partstock-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-partstock-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-partstock-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-partstock-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-partstock-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-partstock-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-partstock-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-partstock-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-partstock-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-partstock-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-partstock-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-partstock-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-partstock-page .field .control :deep(.el-select) { width:100%; }
.dms-partstock-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-partstock-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-partstock-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-partstock-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-partstock-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-partstock-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-partstock-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-partstock-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-partstock-page .btn-soft .el-icon { font-size:14px; }
.dms-partstock-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-partstock-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-partstock-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-partstock-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-partstock-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-partstock-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-partstock-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-partstock-page .table-wrap { overflow-x:auto; }
.dms-partstock-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-partstock-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-partstock-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-partstock-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-partstock-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-partstock-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-partstock-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-partstock-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-partstock-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-partstock-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-partstock-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-partstock-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-partstock-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-partstock-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-partstock-page .badge.amber .dot { background:var(--amber-500); }
.dms-partstock-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-partstock-page .badge.blue .dot { background:var(--blue-500); }
.dms-partstock-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-partstock-page .badge.green .dot { background:var(--green-500); }
.dms-partstock-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-partstock-page .badge.red .dot { background:var(--red-500); }
.dms-partstock-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-partstock-page .badge.violet .dot { background:var(--brand-500); }
.dms-partstock-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-partstock-page .badge.gray .dot { background:var(--ink-400); }
.dms-partstock-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-partstock-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-partstock-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-partstock-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-partstock-page .pagination-container :deep(.el-pagination .btn-prev), .dms-partstock-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-partstock-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-partstock-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-partstock-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .dms-partstock-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-partstock-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-partstock-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
