<template>
  <div class="app-container dms-partalert-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-danger">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><WarningFilled /></el-icon>
            <div>
              <div class="alert-num">{{ alertCount }}</div>
              <div class="alert-label">预警备件数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-warning">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><CircleCloseFilled /></el-icon>
            <div>
              <div class="alert-num">{{ shortageCount }}</div>
              <div class="alert-label">库存不足（=0）</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-info">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><InfoFilled /></el-icon>
            <div>
              <div class="alert-num">{{ lowStockCount }}</div>
              <div class="alert-label">低于下限</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-success">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><Goods /></el-icon>
            <div>
              <div class="alert-num">{{ overStockCount }}</div>
              <div class="alert-label">高于上限</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>备件编号</label>
          <div class="control">
            <el-input v-model="queryParams.partCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>备件名称</label>
          <div class="control">
            <el-input v-model="queryParams.partName" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:partalert:export']">
            <el-icon><Download /></el-icon> 导出预警
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_partalert_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="备件编号" prop="partCode" key="partCode" :width="colWidth('partCode', 130)" resizable v-if="columns.partCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件名称" prop="partName" key="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip v-if="columns.partName.visible" />
          <el-table-column label="备件类别" prop="partType" key="partType" :width="colWidth('partType', 100)" resizable align="center" v-if="columns.partType.visible">
            <template #default="scope"><span class="badge violet">{{ partTypeLabel(scope.row.partType) }}</span></template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 70)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge amber">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="当前库存" prop="currentStock" key="currentStock" :width="colWidth('currentStock', 90)" resizable align="center" v-if="columns.currentStock.visible">
            <template #default="scope">
              <span :class="getStockClass(scope.row)">{{ scope.row.currentStock != null ? scope.row.currentStock : 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存下限" prop="stockMin" key="stockMin" :width="colWidth('stockMin', 90)" resizable align="center" v-if="columns.stockMin.visible" />
          <el-table-column label="库存上限" prop="stockMax" key="stockMax" :width="colWidth('stockMax', 90)" resizable align="center" v-if="columns.stockMax.visible" />
          <el-table-column label="预警状态" prop="alertStatus" key="alertStatus" :width="colWidth('alertStatus', 120)" resizable align="center" v-if="columns.alertStatus.visible">
            <template #default="scope">
              <span class="badge" :class="alertBadgeClass(scope.row)">
                <span class="dot"></span>{{ getAlertText(scope.row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="存放位置" prop="storageLocation" key="storageLocation" :width="colWidth('storageLocation', 120)" resizable show-overflow-tooltip v-if="columns.storageLocation.visible" />
          <el-table-column label="供应商" prop="supplier" key="supplier" :width="colWidth('supplier', 120)" resizable show-overflow-tooltip v-if="columns.supplier.visible" />
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="DmsPartAlert">
import { listPartAlert, delPartAlert } from '@/api/dms/partalert'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, Delete, Download } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partalert_index')
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const multiple = ref(true)

const alertCount = ref(0)
const shortageCount = ref(0)
const lowStockCount = ref(0)
const overStockCount = ref(0)

const defaultColumns = {
  partCode: { label: '备件编号', visible: true },
  partName: { label: '备件名称', visible: true },
  partType: { label: '备件类别', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  currentStock: { label: '当前库存', visible: true },
  stockMin: { label: '库存下限', visible: true },
  stockMax: { label: '库存上限', visible: true },
  alertStatus: { label: '预警状态', visible: true },
  storageLocation: { label: '存放位置', visible: true },
  supplier: { label: '供应商', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_partalert_columns')
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
  if (queryParams.value.partCode) count++
  if (queryParams.value.partName) count++
  return count
})

function partTypeLabel(val) { const item = dms_part_type.value.find(d => d.value == val); return item ? item.label : '-' }
function unitLabel(val) { const item = wms_unit.value.find(d => d.value == val); return item ? item.label : '-' }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, partCode: undefined, partName: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listPartAlert(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    // 统计
    alertCount.value = res.total
    shortageCount.value = res.rows.filter(i => (i.currentStock != null ? Number(i.currentStock) : 0) <= 0).length
    lowStockCount.value = res.rows.filter(i => {
      const s = i.currentStock != null ? Number(i.currentStock) : 0
      const min = i.stockMin != null ? Number(i.stockMin) : 0
      return s > 0 && s <= min
    }).length
    overStockCount.value = res.rows.filter(i => {
      const s = i.currentStock != null ? Number(i.currentStock) : 0
      const max = i.stockMax != null ? Number(i.stockMax) : 0
      return max > 0 && s >= max
    }).length
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.partCode = undefined; queryParams.value.partName = undefined; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.partId); multiple.value = !selection.length }

function getAlertType(row) {
  const s = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (s <= 0) return 'danger'
  if (min > 0 && s <= min) return 'warning'
  if (max > 0 && s >= max) return 'success'
  return 'info'
}
function alertBadgeClass(row) {
  const t = getAlertType(row)
  if (t === 'danger') return 'red'
  if (t === 'warning') return 'amber'
  if (t === 'success') return 'green'
  return 'gray'
}
function getAlertText(row) {
  const s = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (s <= 0) return '库存不足'
  if (min > 0 && s <= min) return '低于下限'
  if (max > 0 && s >= max) return '高于上限'
  return '正常'
}
function getStockClass(row) {
  const s = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (s <= 0) return 'stock-danger'
  if (min > 0 && s <= min) return 'stock-warning'
  if (max > 0 && s >= max) return 'stock-over'
  return ''
}

function handleExport() { proxy.download('dms/sparepart/alert/export', { ...queryParams.value }, `partalert_${new Date().getTime()}.xlsx`) }
function handleDelete(row) {
  const partIds = row.partId || ids.value
  proxy.$modal.confirm('确认删除选中的备件库存预警？\n删除后将清除该备件的安全库存上下限设置。').then(() => delPartAlert(partIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-partalert-page {
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
.dms-partalert-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-partalert-page .filter-card { padding:14px 20px 16px; }
.dms-partalert-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-partalert-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-partalert-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-partalert-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-partalert-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-partalert-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-partalert-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-partalert-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-partalert-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-partalert-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-partalert-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-partalert-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-partalert-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-partalert-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-partalert-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-partalert-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-partalert-page .field .control :deep(.el-select) { width:100%; }
.dms-partalert-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-partalert-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-partalert-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-partalert-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-partalert-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-partalert-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-partalert-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-partalert-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-partalert-page .btn-soft .el-icon { font-size:14px; }
.dms-partalert-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-partalert-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-partalert-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-partalert-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-partalert-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-partalert-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-partalert-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-partalert-page .table-wrap { overflow-x:auto; }
.dms-partalert-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-partalert-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-partalert-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-partalert-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-partalert-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-partalert-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-partalert-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-partalert-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-partalert-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-partalert-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-partalert-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-partalert-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-partalert-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-partalert-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-partalert-page .badge.amber .dot { background:var(--amber-500); }
.dms-partalert-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-partalert-page .badge.blue .dot { background:var(--blue-500); }
.dms-partalert-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-partalert-page .badge.green .dot { background:var(--green-500); }
.dms-partalert-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-partalert-page .badge.red .dot { background:var(--red-500); }
.dms-partalert-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-partalert-page .badge.violet .dot { background:var(--brand-500); }
.dms-partalert-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-partalert-page .badge.gray .dot { background:var(--ink-400); }
.dms-partalert-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-partalert-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-partalert-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-partalert-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-partalert-page .pagination-container :deep(.el-pagination .btn-prev), .dms-partalert-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-partalert-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-partalert-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-partalert-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .dms-partalert-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-partalert-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-partalert-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== Alert Stat Cards ===== */
.dms-partalert-page .alert-card { border-radius: var(--r-lg); }
.dms-partalert-page .alert-card-body { display: flex; align-items: center; gap: 12px; }
.dms-partalert-page .alert-icon { font-size: 36px; }
.dms-partalert-page .alert-num { font-size: 24px; font-weight: bold; }
.dms-partalert-page .alert-label { font-size: 13px; color: var(--ink-500); }
.dms-partalert-page .alert-danger .alert-icon { color: var(--red-500); }
.dms-partalert-page .alert-warning .alert-icon { color: var(--amber-500); }
.dms-partalert-page .alert-info .alert-icon { color: var(--blue-500); }
.dms-partalert-page .alert-success .alert-icon { color: var(--green-500); }
.dms-partalert-page .stock-danger { color: var(--red-500); font-weight: bold; }
.dms-partalert-page .stock-warning { color: var(--amber-500); font-weight: bold; }
.dms-partalert-page .stock-over { color: var(--green-500); font-weight: bold; }
</style>
