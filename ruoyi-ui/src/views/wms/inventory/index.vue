<template>
  <div class="app-container wms-inventory-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>仓库/库区/库位</label>
          <div class="control">
            <el-input v-model="queryParams.locationKeyword" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>规格型号</label>
          <div class="control">
            <el-input v-model="queryParams.specModel" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>更新时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:inventory:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['wms:inventory:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_inventory_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 180)" resizable sortable="custom" v-if="columns.materialCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 240)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 200)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge blue">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="仓库/库区/库位" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 240)" resizable show-overflow-tooltip v-if="columns.warehouseName.visible">
            <template #default="scope">
              {{ scope.row.warehouseName || '' }}<span v-if="scope.row.areaName">/{{ scope.row.areaName }}</span><span v-if="scope.row.locationName">/{{ scope.row.locationName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 120)" resizable v-if="columns.batchNo.visible" />
          <el-table-column label="生产日期" prop="productionDate" key="productionDate" :width="colWidth('productionDate', 130)" resizable align="center" v-if="columns.productionDate.visible" />
          <el-table-column label="有效期" prop="expiryDate" key="expiryDate" :width="colWidth('expiryDate', 130)" resizable align="center" v-if="columns.expiryDate.visible" />
          <el-table-column label="可用数量" prop="qty" key="qty" :width="colWidth('qty', 110)" resizable align="center" class-name="col-num" v-if="columns.qty.visible" />
          <el-table-column label="锁定数量" prop="lockQty" key="lockQty" :width="colWidth('lockQty', 110)" resizable align="center" class-name="col-num" v-if="columns.lockQty.visible" />
          <el-table-column label="更新时间" prop="updateTime" key="updateTime" :width="colWidth('updateTime', 180)" resizable align="center" sortable="custom" v-if="columns.updateTime.visible" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:inventory:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="WmsInventory">
import { listInventory, delInventory } from '@/api/wms/inventory'
import { fetchAllPages, downloadCsv } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown, Delete, Download, RefreshLeft, Search, Filter } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_inventory_index')
const { wms_unit } = proxy.useDict('wms_unit')
const list = ref([]); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const total = ref(0); const ids = ref([]); const multiple = ref(true)
const dateRange = ref([])

const defaultColumns = {
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  warehouseName: { label: '仓库/库区/库位', visible: true },
  batchNo: { label: '批次号', visible: true },
  productionDate: { label: '生产日期', visible: true },
  expiryDate: { label: '有效期', visible: true },
  qty: { label: '可用数量', visible: true },
  lockQty: { label: '锁定数量', visible: true },
  updateTime: { label: '更新时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('wms_inventory_columns')
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

const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, locationKeyword: undefined, batchNo: undefined, specModel: undefined, params: {} } })
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.locationKeyword) count++
  if (queryParams.value.batchNo) count++
  if (queryParams.value.specModel) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listInventory(proxy.addDateRange(queryParams.value, dateRange.value, 'UpdateTime')).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.locationKeyword = undefined; queryParams.value.batchNo = undefined; queryParams.value.specModel = undefined; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}
function unitLabel(unit) { const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '-' }
/** 导出库存（与列表口径一致：含筛选条件，导出全部页数据；仓库/库区/库位为合并列） */
async function handleExport() {
  const rows = await fetchAllPages(listInventory, proxy.addDateRange(queryParams.value, dateRange.value, 'UpdateTime'))
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['物料编码', '物料名称', '规格型号', '单位', '仓库/库区/库位', '批次号', '生产日期', '有效期', '可用数量', '锁定数量', '更新时间']
  downloadCsv(`inventory_${new Date().getTime()}`, headers, rows.map(i => [
    i.materialCode || '', i.materialName || '', i.specModel || '', unitLabel(i.unit),
    [i.warehouseName, i.areaName, i.locationName].filter(Boolean).join('/'),
    i.batchNo || '', i.productionDate || '', i.expiryDate || '',
    i.qty != null ? i.qty : '', i.lockQty != null ? i.lockQty : '', i.updateTime || ''
  ]))
}
function handleSelectionChange(sel) { ids.value = sel.map(i => i.inventoryId); multiple.value = !sel.length }
function handleDelete(row) { const inventoryIds = row.inventoryId || ids.value; proxy.$modal.confirm('确认删除选中的库存记录？').then(() => delInventory(inventoryIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.wms-inventory-page {
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
.wms-inventory-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.wms-inventory-page .filter-card { padding:14px 20px 16px; }
.wms-inventory-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.wms-inventory-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.wms-inventory-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.wms-inventory-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.wms-inventory-page .filter-card .adv-link:hover { color:var(--brand-600); }
.wms-inventory-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.wms-inventory-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.wms-inventory-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.wms-inventory-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.wms-inventory-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.wms-inventory-page .filter-card .filter-buttons { display:flex; gap:8px; }
.wms-inventory-page .field { display:flex; flex-direction:column; gap:6px; }
.wms-inventory-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.wms-inventory-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.wms-inventory-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.wms-inventory-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.wms-inventory-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.wms-inventory-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.wms-inventory-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.wms-inventory-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.wms-inventory-page .field .control :deep(.el-select) { width:100%; }
.wms-inventory-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.wms-inventory-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.wms-inventory-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.wms-inventory-page .field .control :deep(.el-date-editor) { width:100%; }
.wms-inventory-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.wms-inventory-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.wms-inventory-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.wms-inventory-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.wms-inventory-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.wms-inventory-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.wms-inventory-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.wms-inventory-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.wms-inventory-page .btn-soft .el-icon { font-size:14px; }
.wms-inventory-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.wms-inventory-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.wms-inventory-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.wms-inventory-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.wms-inventory-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.wms-inventory-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.wms-inventory-page .table-wrap { overflow-x:auto; }
.wms-inventory-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.wms-inventory-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.wms-inventory-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.wms-inventory-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.wms-inventory-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.wms-inventory-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.wms-inventory-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.wms-inventory-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.wms-inventory-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.wms-inventory-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.wms-inventory-page .app-table :deep(.col-num) { text-align:center; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.wms-inventory-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.wms-inventory-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.wms-inventory-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.wms-inventory-page .badge.amber .dot { background:var(--amber-500); }
.wms-inventory-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.wms-inventory-page .badge.blue .dot { background:var(--blue-500); }
.wms-inventory-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.wms-inventory-page .badge.green .dot { background:var(--green-500); }
.wms-inventory-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.wms-inventory-page .badge.red .dot { background:var(--red-500); }
.wms-inventory-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.wms-inventory-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.wms-inventory-page .badge.gray .dot { background:var(--ink-400); }
.wms-inventory-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.wms-inventory-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.wms-inventory-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.wms-inventory-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; }
.wms-inventory-page .pagination-container :deep(.el-pagination .btn-prev), .wms-inventory-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.wms-inventory-page .pagination-container :deep(.el-pagination .btn-prev:hover), .wms-inventory-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.wms-inventory-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .wms-inventory-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .wms-inventory-page .filter-card .filter-bar { grid-template-columns:1fr; } .wms-inventory-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
