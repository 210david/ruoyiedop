<template>
  <div class="app-container wms-log-page">
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
          <label>变动类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.changeType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_change_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>关联单据</label>
          <div class="control">
            <el-input v-model="queryParams.refOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>仓库</label>
          <div class="control is-select">
            <el-select v-model="queryParams.warehouseId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>操作时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" style="width: 100%" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
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
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:log:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['wms:log:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_log_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 180)" resizable v-if="columns.materialCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 280)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="仓库/库区/库位" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 240)" resizable show-overflow-tooltip v-if="columns.warehouseName.visible">
            <template #default="scope">
              {{ scope.row.warehouseName || '' }}<span v-if="scope.row.areaName">/{{ scope.row.areaName }}</span><span v-if="scope.row.locationName">/{{ scope.row.locationName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 120)" resizable v-if="columns.batchNo.visible" />
          <el-table-column label="变动类型" prop="changeType" key="changeType" :width="colWidth('changeType', 120)" resizable align="center" v-if="columns.changeType.visible">
            <template #default="scope"><span class="badge violet">{{ changeTypeLabel(scope.row.changeType) }}</span></template>
          </el-table-column>
          <el-table-column label="变动数量" prop="changeQty" key="changeQty" :width="colWidth('changeQty', 110)" resizable align="center" class-name="col-num" v-if="columns.changeQty.visible" />
          <el-table-column label="变动前" prop="beforeQty" key="beforeQty" :width="colWidth('beforeQty', 110)" resizable align="center" class-name="col-num" v-if="columns.beforeQty.visible" />
          <el-table-column label="变动后" prop="afterQty" key="afterQty" :width="colWidth('afterQty', 110)" resizable align="center" class-name="col-num" v-if="columns.afterQty.visible" />
          <el-table-column label="关联单据" prop="refOrderNo" key="refOrderNo" :width="colWidth('refOrderNo', 180)" resizable v-if="columns.refOrderNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.refOrderNo }}</span></template>
          </el-table-column>
          <el-table-column label="操作人" prop="createBy" key="createBy" :width="colWidth('createBy', 120)" resizable v-if="columns.createBy.visible" />
          <el-table-column label="操作时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:log:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="WmsInventoryLog">
import { listInventoryLog, delInventoryLog } from '@/api/wms/inventory'
import { listWarehouse } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown, Delete, Download, RefreshLeft, Search, Filter } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_log_index')
const { wms_change_type } = proxy.useDict('wms_change_type')
const list = ref([]); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const total = ref(0); const dateRange = ref([]); const ids = ref([]); const multiple = ref(true); const warehouseOptions = ref([])

const defaultColumns = {
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  warehouseName: { label: '仓库/库区/库位', visible: true },
  batchNo: { label: '批次号', visible: true },
  changeType: { label: '变动类型', visible: true },
  changeQty: { label: '变动数量', visible: true },
  beforeQty: { label: '变动前', visible: true },
  afterQty: { label: '变动后', visible: true },
  refOrderNo: { label: '关联单据', visible: true },
  createBy: { label: '操作人', visible: true },
  createTime: { label: '操作时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('wms_log_columns')
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

const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, changeType: undefined, refOrderNo: undefined, warehouseId: undefined, batchNo: undefined } })
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.changeType) count++
  if (queryParams.value.refOrderNo) count++
  if (queryParams.value.warehouseId) count++
  if (queryParams.value.batchNo) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listInventoryLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.changeType = undefined; queryParams.value.refOrderNo = undefined; queryParams.value.warehouseId = undefined; queryParams.value.batchNo = undefined; handleQuery() }
function changeTypeLabel(type) { const item = wms_change_type.value.find(d => d.value == type); return item ? item.label : '-' }
function handleExport() { proxy.download('wms/log/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `inventory_log_${new Date().getTime()}.xlsx`) }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.logId); multiple.value = !sel.length }
function handleDelete(row) { const logIds = row.logId || ids.value; proxy.$modal.confirm('确认删除选中的流水记录？').then(() => delInventoryLog(logIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
getList()
listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
</script>

<style scoped>
.wms-log-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color: var(--ink-900); }
.wms-log-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.wms-log-page .filter-card { padding:14px 20px 16px; }
.wms-log-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.wms-log-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.wms-log-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.wms-log-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.wms-log-page .filter-card .adv-link:hover { color:var(--brand-600); }
.wms-log-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.wms-log-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.wms-log-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.wms-log-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.wms-log-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.wms-log-page .filter-card .filter-buttons { display:flex; gap:8px; }
.wms-log-page .field { display:flex; flex-direction:column; gap:6px; }
.wms-log-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.wms-log-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.wms-log-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.wms-log-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.wms-log-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.wms-log-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.wms-log-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.wms-log-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.wms-log-page .field .control :deep(.el-select) { width:100%; }
.wms-log-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.wms-log-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.wms-log-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.wms-log-page .field .control :deep(.el-date-editor) { width:100%; }
.wms-log-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.wms-log-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.wms-log-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.wms-log-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.wms-log-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.wms-log-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.wms-log-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.wms-log-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.wms-log-page .btn-soft .el-icon { font-size:14px; }
.wms-log-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.wms-log-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.wms-log-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.wms-log-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.wms-log-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.wms-log-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.wms-log-page .table-wrap { overflow-x:auto; }
.wms-log-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.wms-log-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.wms-log-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.wms-log-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.wms-log-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.wms-log-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.wms-log-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.wms-log-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.wms-log-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.wms-log-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.wms-log-page .app-table :deep(.col-num) { text-align:center; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.wms-log-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.wms-log-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.wms-log-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.wms-log-page .badge.amber .dot { background:var(--amber-500); }
.wms-log-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.wms-log-page .badge.blue .dot { background:var(--blue-500); }
.wms-log-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.wms-log-page .badge.green .dot { background:var(--green-500); }
.wms-log-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.wms-log-page .badge.red .dot { background:var(--red-500); }
.wms-log-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.wms-log-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.wms-log-page .badge.gray .dot { background:var(--ink-400); }
.wms-log-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.wms-log-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.wms-log-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.wms-log-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; }
.wms-log-page .pagination-container :deep(.el-pagination .btn-prev), .wms-log-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.wms-log-page .pagination-container :deep(.el-pagination .btn-prev:hover), .wms-log-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.wms-log-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .wms-log-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .wms-log-page .filter-card .filter-bar { grid-template-columns:1fr; } .wms-log-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
