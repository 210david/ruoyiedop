<template>
  <div class="app-container pms-price-compare-page">
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
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="供应商名称/编号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="物料名称/编号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>询价单号</label>
          <div class="control">
            <el-input v-model="queryParams.inquiryNo" placeholder="请输入询价单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>中标状态</label>
          <div class="control is-select">
            <el-select v-model="awardFilter" placeholder="全部" clearable @change="onAwardFilterChange">
              <el-option label="已中标" value="1" />
              <el-option label="未中标" value="0" />
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
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab" :class="[statusTabClass('1'), { 'is-active': activeStatusTab === '1' }]" @click="handleStatusTabClick('1')">
            <span class="dot"></span>
            <span>已中标</span>
            <span class="count">{{ statusCounts['1'] || 0 }}</span>
          </button>
          <button class="status-tab" :class="[statusTabClass('0'), { 'is-active': activeStatusTab === '0' }]" @click="handleStatusTabClick('0')">
            <span class="dot"></span>
            <span>未中标</span>
            <span class="count">{{ statusCounts['0'] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:priceCompare:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_price_compare_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="priceCompareList" :row-class-name="rowClassName" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="询价单号" prop="inquiryNo" key="inquiryNo" :width="colWidth('inquiryNo', 180)" resizable sortable="custom" v-if="columns.inquiryNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.inquiryNo }}</span></template>
          </el-table-column>
          <el-table-column label="供应商编号" prop="supplierCode" key="supplierCode" :width="colWidth('supplierCode', 120)" resizable show-overflow-tooltip v-if="columns.supplierCode.visible" />
          <el-table-column label="供应商名称" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="物料编号" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 130)" resizable v-if="columns.materialCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 150)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="规格型号" prop="specification" key="specification" :width="colWidth('specification', 130)" resizable show-overflow-tooltip v-if="columns.specification.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" key="quantity" :width="colWidth('quantity', 100)" resizable align="right" class-name="col-num" v-if="columns.quantity.visible" />
          <el-table-column label="单价" prop="price" key="price" :width="colWidth('price', 120)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.price.visible">
            <template #default="scope">
              <span class="rd-amount" :class="{ 'rd-price-best': isLowestPrice(scope.row) }">{{ formatMoney(scope.row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="报价日期" prop="quotationDate" key="quotationDate" :width="colWidth('quotationDate', 120)" resizable align="center" v-if="columns.quotationDate.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.quotationDate, '{y}-{m}-{d}') }}</span></template>
          </el-table-column>
          <el-table-column label="有效期" prop="validDate" key="validDate" :width="colWidth('validDate', 120)" resizable align="center" v-if="columns.validDate.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.validDate, '{y}-{m}-{d}') }}</span></template>
          </el-table-column>
          <el-table-column label="税率(%)" prop="taxRate" key="taxRate" :width="colWidth('taxRate', 90)" resizable align="right" v-if="columns.taxRate.visible" />
          <el-table-column label="交货期(天)" prop="deliveryDays" key="deliveryDays" :width="colWidth('deliveryDays', 100)" resizable align="center" v-if="columns.deliveryDays.visible" />
          <el-table-column label="付款方式" prop="paymentTerms" key="paymentTerms" :width="colWidth('paymentTerms', 120)" resizable align="center" show-overflow-tooltip v-if="columns.paymentTerms.visible">
            <template #default="scope"><dict-tag :options="wms_payment_method" :value="scope.row.paymentTerms" /></template>
          </el-table-column>
          <el-table-column label="中标" prop="isAward" key="isAward" :width="colWidth('isAward', 80)" resizable align="center" fixed="right" v-if="columns.isAward.visible">
            <template #default="scope">
              <span v-if="scope.row.isAward === '1'" class="badge green"><span class="dot"></span>中标</span>
              <span v-else class="badge gray"><span class="dot"></span>未中标</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 状态说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="价格比价说明" width="600px" append-to-body>
      <div class="status-help-content">
        <h4>一、页面说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="价格比价">展示各询价单中供应商的报价信息，便于横向对比价格，辅助采购决策</el-descriptions-item>
          <el-descriptions-item label="最低价高亮">同一询价单同一物料中，最低单价会以红色加粗高亮显示</el-descriptions-item>
          <el-descriptions-item label="中标标记">供应商被确定为中标供应商时，对应行会高亮显示，并标注"中标"徽章</el-descriptions-item>
        </el-descriptions>
        <h4>二、操作说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>筛选查询：</strong>可通过供应商、物料、询价单号、中标状态进行筛选，支持回车快速搜索</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>导出数据：</strong>点击工具栏"导出"按钮，可将当前查询结果导出为Excel文件</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>列定制：</strong>点击工具栏右侧列管理图标，可自定义显示/隐藏表格列，列宽支持拖拽调整并自动保存</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PriceCompare">
import { listPriceCompare, exportPriceCompare } from '@/api/pms/priceCompare'
import { useColumnResize } from '@/composables/useColumnResize'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { wms_unit, wms_payment_method } = proxy.useDict('wms_unit', 'wms_payment_method')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_price_compare_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  inquiryNo: { label: '询价单号', visible: true },
  supplierCode: { label: '供应商编号', visible: true },
  supplierName: { label: '供应商名称', visible: true },
  materialCode: { label: '物料编号', visible: true },
  materialName: { label: '物料名称', visible: true },
  specification: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  quantity: { label: '数量', visible: true },
  price: { label: '单价', visible: true },
  quotationDate: { label: '报价日期', visible: true },
  validDate: { label: '有效期', visible: true },
  taxRate: { label: '税率(%)', visible: true },
  deliveryDays: { label: '交货期(天)', visible: true },
  paymentTerms: { label: '付款方式', visible: true },
  isAward: { label: '中标', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_price_compare_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '1': 0, '0': 0 })
function loadStatusCounts() {
  listPriceCompare({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '1': 0, '0': 0 }
    ;(res.rows || []).forEach(r => {
      if (r.isAward === '1') counts['1']++
      else counts['0']++
    })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '1': 'tab-approved', '0': 'tab-void' }
  return map[value] || ''
}
const awardFilter = ref(undefined)
function onAwardFilterChange(val) {
  activeStatusTab.value = val || 'all'
  handleQuery()
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  awardFilter.value = tab === 'all' ? undefined : tab
  handleQuery()
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierName) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.inquiryNo) count++
  if (awardFilter.value) count++
  return count
})

const priceCompareList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const total = ref(0)
const lowestPriceMap = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    supplierName: undefined,
    materialName: undefined,
    inquiryNo: undefined,
    orderByColumn: undefined,
    isAsc: undefined,
    params: {}
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  // 将中标筛选写入 params Map，使用 RuoYi 标准方式传递给后端
  if (awardFilter.value !== undefined && awardFilter.value !== null && awardFilter.value !== '') {
    queryParams.value.params.awardFlag = awardFilter.value
  } else {
    delete queryParams.value.params.awardFlag
  }
  listPriceCompare(queryParams.value).then(res => {
    priceCompareList.value = res.rows
    total.value = res.total
    computeLowestPrices()
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}

/** 计算每个物料+询价单组合的最低价 */
function computeLowestPrices() {
  const map = {}
  priceCompareList.value.forEach(row => {
    const key = row.inquiryNo + '_' + row.materialCode
    if (!map[key] || row.price < map[key]) {
      map[key] = row.price
    }
  })
  lowestPriceMap.value = map
}

/** 判断是否为最低价 */
function isLowestPrice(row) {
  const key = row.inquiryNo + '_' + row.materialCode
  return lowestPriceMap.value[key] !== undefined && row.price === lowestPriceMap.value[key]
}

/** 行样式：中标行高亮 */
function rowClassName({ row }) {
  if (row.isAward === '1') {
    return 'rd-award-row'
  }
  return ''
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.supplierName = undefined
  queryParams.value.materialName = undefined
  queryParams.value.inquiryNo = undefined
  queryParams.value.orderByColumn = undefined
  queryParams.value.isAsc = undefined
  queryParams.value.params = {}
  awardFilter.value = undefined
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.orderByColumn = column.prop
    queryParams.value.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.orderByColumn = undefined
    queryParams.value.isAsc = undefined
  }
  getList()
}

function handleExport() {
  if (awardFilter.value !== undefined && awardFilter.value !== null && awardFilter.value !== '') {
    queryParams.value.params.awardFlag = awardFilter.value
  } else {
    delete queryParams.value.params.awardFlag
  }
  proxy.download('pms/priceCompare/export', { ...queryParams.value }, `price_compare_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-price-compare-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff; --violet-500:#8b5cf6;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.pms-price-compare-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.pms-price-compare-page .filter-card { padding:14px 20px 16px; }
.pms-price-compare-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-price-compare-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-price-compare-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-price-compare-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-price-compare-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-price-compare-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-price-compare-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-price-compare-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-price-compare-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-price-compare-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-price-compare-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.pms-price-compare-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-price-compare-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-price-compare-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-price-compare-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-price-compare-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-price-compare-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-price-compare-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-price-compare-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-price-compare-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-price-compare-page .field .control :deep(.el-select) { width:100%; }
.pms-price-compare-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-price-compare-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-price-compare-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-price-compare-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-price-compare-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-price-compare-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-price-compare-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Status Tabs ===== */
.pms-price-compare-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-price-compare-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-price-compare-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-price-compare-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-price-compare-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-price-compare-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-price-compare-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-price-compare-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-price-compare-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-price-compare-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-price-compare-page .status-tab.tab-approved .dot { background:var(--green-500); }
.pms-price-compare-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.pms-price-compare-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.pms-price-compare-page .status-tab.tab-void .dot { background:var(--ink-400); }

/* ===== Tip Pill ===== */
.pms-price-compare-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; flex-shrink:0; }
.pms-price-compare-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-price-compare-page .tip-pill .el-icon { font-size:14px; }

/* ===== Toolbar ===== */
.pms-price-compare-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-price-compare-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-price-compare-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-price-compare-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.pms-price-compare-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-price-compare-page .btn-soft .el-icon { font-size:14px; }
.pms-price-compare-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-price-compare-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-price-compare-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-price-compare-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-price-compare-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-price-compare-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }

/* ===== Table ===== */
.pms-price-compare-page .table-wrap { overflow-x:auto; }
.pms-price-compare-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-price-compare-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-price-compare-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-price-compare-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-price-compare-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-price-compare-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-price-compare-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-price-compare-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-price-compare-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-price-compare-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-price-compare-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pms-price-compare-page .app-table :deep(.col-num) { text-align:right; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.pms-price-compare-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
.pms-price-compare-page .app-table .rd-price-best { color:var(--red-500); font-weight:700; }
.pms-price-compare-page .app-table :deep(.rd-award-row) { background-color:#f0f9eb; }
.pms-price-compare-page .app-table :deep(.rd-award-row:hover > td) { background-color:#e8f5e0 !important; }

/* ===== Badges ===== */
.pms-price-compare-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-price-compare-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-price-compare-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-price-compare-page .badge.green .dot { background:var(--green-500); }
.pms-price-compare-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-price-compare-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.pms-price-compare-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-price-compare-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-price-compare-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-price-compare-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-price-compare-page .pagination-container :deep(.el-pagination .btn-prev), .pms-price-compare-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-price-compare-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-price-compare-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-price-compare-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .pms-price-compare-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-price-compare-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-price-compare-page .toolbar { flex-wrap:wrap; gap:10px; } .pms-price-compare-page .status-tabs { padding:6px 8px; } }

/* ===== Status Help Dialog ===== */
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
</style>
