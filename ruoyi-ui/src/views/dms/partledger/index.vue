<template>
  <div class="app-container dms-partledger-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #409eff"><Box /></el-icon>
            <div>
              <div class="stat-num">{{ statTotal }}</div>
              <div class="stat-label">备件种类</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #67c23a"><CircleCheckFilled /></el-icon>
            <div>
              <div class="stat-num">{{ statNormal }}</div>
              <div class="stat-label">库存正常</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #e6a23c"><WarningFilled /></el-icon>
            <div>
              <div class="stat-num">{{ statLow }}</div>
              <div class="stat-label">库存不足</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #f56c6c"><CircleCloseFilled /></el-icon>
            <div>
              <div class="stat-num">{{ statOver }}</div>
              <div class="stat-label">库存积压</div>
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
        <div class="field">
          <label>备件类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.partType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in dms_part_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>库存状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.stockStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="normal" />
              <el-option label="库存不足" value="low" />
              <el-option label="库存积压" value="over" />
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
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:sparepart:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:partledger:export']">
            <el-icon><Download /></el-icon> 导出台账
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_partledger_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="filteredList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="备件编号" prop="partCode" key="partCode" :width="colWidth('partCode', 130)" resizable v-if="columns.partCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件名称" prop="partName" key="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip v-if="columns.partName.visible" />
          <el-table-column label="备件类别" prop="partType" key="partType" :width="colWidth('partType', 90)" resizable align="center" v-if="columns.partType.visible">
            <template #default="scope"><span class="badge violet">{{ partTypeLabel(scope.row.partType) }}</span></template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 130)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 60)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge amber">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="当前库存" prop="currentStock" key="currentStock" :width="colWidth('currentStock', 90)" resizable align="center" v-if="columns.currentStock.visible">
            <template #default="scope">
              <span :class="getStockClass(scope.row)">{{ scope.row.currentStock != null ? scope.row.currentStock : 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存下限" prop="stockMin" key="stockMin" :width="colWidth('stockMin', 80)" resizable align="center" v-if="columns.stockMin.visible" />
          <el-table-column label="库存上限" prop="stockMax" key="stockMax" :width="colWidth('stockMax', 80)" resizable align="center" v-if="columns.stockMax.visible" />
          <el-table-column label="库存状态" prop="stockStatus" key="stockStatus" :width="colWidth('stockStatus', 90)" resizable align="center" v-if="columns.stockStatus.visible">
            <template #default="scope">
              <span class="badge" :class="stockBadgeClass(scope.row)">
                <span class="dot"></span>{{ getStockText(scope.row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 100)" resizable show-overflow-tooltip v-if="columns.warehouseName.visible" />
          <el-table-column label="存放位置" prop="storageLocation" key="storageLocation" :width="colWidth('storageLocation', 110)" resizable show-overflow-tooltip v-if="columns.storageLocation.visible" />
          <el-table-column label="供应商" prop="supplier" key="supplier" :width="colWidth('supplier', 110)" resizable show-overflow-tooltip v-if="columns.supplier.visible" />
          <el-table-column label="更新时间" prop="updateTime" key="updateTime" :width="colWidth('updateTime', 160)" resizable align="center" sortable="custom" v-if="columns.updateTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:sparepart:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">备件详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.partCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ viewData.partCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">备件编号</span><div class="rd-value">{{ viewData.partCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">备件名称</span><div class="rd-value">{{ viewData.partName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">备件类别</span><div class="rd-value"><dict-tag :options="dms_part_type" :value="viewData.partType" /></div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计量单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplier || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 库存信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="16 18 23 18 23 11"/></svg></span>库存信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">当前库存</span><div class="rd-value">{{ viewData.currentStock != null ? viewData.currentStock : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">库存状态</span><div class="rd-value"><el-tag :type="getStockType(viewData)" effect="dark" size="small">{{ getStockText(viewData) }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">库存下限</span><div class="rd-value">{{ viewData.stockMin != null ? viewData.stockMin : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">库存上限</span><div class="rd-value">{{ viewData.stockMax != null ? viewData.stockMax : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">存放位置</span><div class="rd-value">{{ viewData.storageLocation || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">仓库名称</span><div class="rd-value">{{ viewData.warehouseName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">备件状态</span><div class="rd-value"><el-tag :type="viewData.status === '0' ? 'success' : 'danger'">{{ viewData.status === '0' ? '正常' : '停用' }}</el-tag></div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPartLedger">
import { listPartLedger, delPartLedger } from '@/api/dms/partledger'
import { fetchAllPages, downloadCsv } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Delete, Download } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partledger_index')
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')
const { collapsedCards, toggleCard } = useDetailCard(['v3', 'v2', 'v1', 'v0'])

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const viewOpen = ref(false)
const viewData = ref({})

const statTotal = ref(0)
const statNormal = ref(0)
const statLow = ref(0)
const statOver = ref(0)

const defaultColumns = {
  partCode: { label: '备件编号', visible: true },
  partName: { label: '备件名称', visible: true },
  partType: { label: '备件类别', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  currentStock: { label: '当前库存', visible: true },
  stockMin: { label: '库存下限', visible: true },
  stockMax: { label: '库存上限', visible: true },
  stockStatus: { label: '库存状态', visible: true },
  warehouseName: { label: '仓库', visible: true },
  storageLocation: { label: '存放位置', visible: true },
  supplier: { label: '供应商', visible: true },
  updateTime: { label: '更新时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_partledger_columns')
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
  if (queryParams.value.partType) count++
  if (queryParams.value.stockStatus) count++
  return count
})

function partTypeLabel(val) { const item = dms_part_type.value.find(d => d.value == val); return item ? item.label : '-' }
function unitLabel(val) { const item = wms_unit.value.find(d => d.value == val); return item ? item.label : '-' }

/** 库存状态判断：库存不足(小于下限)、库存积压(大于上限)、正常 */
function getStockKey(row) {
  const stock = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (min > 0 && stock < min) return 'low'
  if (max > 0 && stock > max) return 'over'
  return 'normal'
}
function getStockType(row) {
  const k = getStockKey(row)
  if (k === 'low') return 'warning'
  if (k === 'over') return 'danger'
  return 'success'
}
function getStockText(row) {
  const k = getStockKey(row)
  if (k === 'low') return '库存不足'
  if (k === 'over') return '库存积压'
  return '正常'
}
function getStockClass(row) {
  const k = getStockKey(row)
  if (k === 'low') return 'stock-warning'
  if (k === 'over') return 'stock-danger'
  return ''
}
function stockBadgeClass(row) {
  const k = getStockKey(row)
  if (k === 'low') return 'amber'
  if (k === 'over') return 'red'
  return 'green'
}

/** 前端过滤库存状态 */
const filteredList = computed(() => {
  const status = queryParams.value.stockStatus
  if (!status) return list.value
  return list.value.filter(row => getStockKey(row) === status)
})

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, partCode: undefined, partName: undefined, partType: undefined, stockStatus: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listPartLedger(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    // 统计
    statTotal.value = res.total
    statLow.value = res.rows.filter(i => getStockKey(i) === 'low').length
    statOver.value = res.rows.filter(i => getStockKey(i) === 'over').length
    statNormal.value = res.rows.filter(i => getStockKey(i) === 'normal').length
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.partCode = undefined; queryParams.value.partName = undefined; queryParams.value.partType = undefined; queryParams.value.stockStatus = undefined; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.stockId); multiple.value = !selection.length }
function handleDelete(row) { const stockIds = row.stockId || ids.value; proxy.$modal.confirm('确认删除选中的库存记录？（不会删除备件主数据）').then(() => delPartLedger(stockIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
/** 导出台账（与列表口径一致：含筛选条件、库存状态前端过滤，导出全部页数据） */
async function handleExport() {
  const rows = await fetchAllPages(listPartLedger, queryParams.value)
  const filtered = queryParams.value.stockStatus ? rows.filter(i => getStockKey(i) === queryParams.value.stockStatus) : rows
  if (!filtered.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['备件编号', '备件名称', '备件类别', '规格型号', '单位', '当前库存', '库存下限', '库存上限', '库存状态', '仓库', '存放位置', '供应商', '更新时间']
  downloadCsv(`partledger_${new Date().getTime()}`, headers, filtered.map(i => [
    i.partCode, i.partName, partTypeLabel(i.partType), i.specModel, unitLabel(i.unit),
    i.currentStock != null ? i.currentStock : 0, i.stockMin != null ? i.stockMin : '', i.stockMax != null ? i.stockMax : '',
    getStockText(i), i.warehouseName || '', i.storageLocation || '', i.supplier || '', i.updateTime || ''
  ]))
}

/** 查看详情（行数据来自库存台账关联查询，已含库存、仓库、存放位置字段） */
function handleView(row) {
  // 关联查询中 status 为库存记录状态，备件状态映射在 partStatus
  viewData.value = { ...row, status: row.partStatus || row.status }
  viewOpen.value = true
}

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-partledger-page {
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
.dms-partledger-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-partledger-page .filter-card { padding:14px 20px 16px; }
.dms-partledger-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-partledger-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-partledger-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-partledger-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-partledger-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-partledger-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-partledger-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-partledger-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-partledger-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-partledger-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-partledger-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-partledger-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-partledger-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-partledger-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-partledger-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-partledger-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-partledger-page .field .control :deep(.el-select) { width:100%; }
.dms-partledger-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-partledger-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-partledger-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-partledger-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-partledger-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-partledger-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-partledger-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-partledger-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-partledger-page .btn-soft .el-icon { font-size:14px; }
.dms-partledger-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-partledger-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-partledger-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-partledger-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-partledger-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-partledger-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-partledger-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-partledger-page .table-wrap { overflow-x:auto; }
.dms-partledger-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-partledger-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-partledger-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-partledger-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-partledger-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-partledger-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-partledger-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-partledger-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-partledger-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-partledger-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-partledger-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-partledger-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-partledger-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-partledger-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-partledger-page .badge.amber .dot { background:var(--amber-500); }
.dms-partledger-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-partledger-page .badge.blue .dot { background:var(--blue-500); }
.dms-partledger-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-partledger-page .badge.green .dot { background:var(--green-500); }
.dms-partledger-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-partledger-page .badge.red .dot { background:var(--red-500); }
.dms-partledger-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-partledger-page .badge.violet .dot { background:var(--brand-500); }
.dms-partledger-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-partledger-page .badge.gray .dot { background:var(--ink-400); }
.dms-partledger-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-partledger-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-partledger-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-partledger-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-partledger-page .pagination-container :deep(.el-pagination .btn-prev), .dms-partledger-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-partledger-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-partledger-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-partledger-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-partledger-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-partledger-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-partledger-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-partledger-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-partledger-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-partledger-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-partledger-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-partledger-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== Stat Cards ===== */
.dms-partledger-page .stat-card { border-radius: var(--r-lg); }
.dms-partledger-page .stat-body { display: flex; align-items: center; gap: 12px; }
.dms-partledger-page .stat-icon { font-size: 36px; }
.dms-partledger-page .stat-num { font-size: 24px; font-weight: bold; }
.dms-partledger-page .stat-label { font-size: 13px; color: var(--ink-500); }
.dms-partledger-page .stock-danger { color: var(--red-500); font-weight: bold; }
.dms-partledger-page .stock-warning { color: var(--amber-500); font-weight: bold; }
</style>
