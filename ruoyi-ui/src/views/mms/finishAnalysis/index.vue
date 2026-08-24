<template>
  <div class="app-container mms-finish-analysis-page">
    <!-- ===== 筛选条件 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>产品编码</label><div class="control"><el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品名称</label><div class="control"><el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>产能单元</label><div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>工单状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_workorder_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>是否准时</label><div class="control is-select"><el-select v-model="queryParams.isOnTime" placeholder="全部" clearable @change="handleQuery"><el-option label="准时完工" value="1" /><el-option label="逾期完工" value="0" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>完工日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stat-cards">
      <div class="stat-card stat-card--blue">
        <div class="stat-card-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
        <div class="stat-card-body">
          <div class="stat-card-label">工单总数</div>
          <div class="stat-card-value">{{ summary.workOrderCount || 0 }}<span class="stat-card-unit">单</span></div>
        </div>
      </div>
      <div class="stat-card stat-card--green">
        <div class="stat-card-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></div>
        <div class="stat-card-body">
          <div class="stat-card-label">平均完工率</div>
          <div class="stat-card-value">{{ formatPct(summary.avgFinishRate) }}<span class="stat-card-unit">%</span></div>
        </div>
      </div>
      <div class="stat-card stat-card--amber">
        <div class="stat-card-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
        <div class="stat-card-body">
          <div class="stat-card-label">平均损耗率</div>
          <div class="stat-card-value">{{ formatPct(summary.avgLossRate) }}<span class="stat-card-unit">%</span></div>
        </div>
      </div>
      <div class="stat-card stat-card--indigo">
        <div class="stat-card-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div>
        <div class="stat-card-body">
          <div class="stat-card-label">平均收率</div>
          <div class="stat-card-value">{{ formatPct(summary.avgYieldRate) }}<span class="stat-card-unit">%</span></div>
        </div>
      </div>
      <div class="stat-card stat-card--teal">
        <div class="stat-card-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
        <div class="stat-card-body">
          <div class="stat-card-label">准时完工率</div>
          <div class="stat-card-value">{{ formatPct(summary.onTimeRate) }}<span class="stat-card-unit">%</span></div>
        </div>
      </div>
    </div>

    <!-- ===== 明细表格 ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab is-active">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ total }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:finishAnalysis:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_finish_analysis_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @header-dragend="onHeaderDragEnd" class="app-table" show-summary :summary-method="getSummaryRow">
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 180)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><span v-if="scope.row.unit" class="badge violet">{{ unitLabel(scope.row.unit) }}</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 120)" resizable show-overflow-tooltip v-if="columns.resourceName.visible" />
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="完工数量" prop="finishQty" key="finishQty" :width="colWidth('finishQty', 100)" resizable align="center" v-if="columns.finishQty.visible" />
          <el-table-column label="合格数量" prop="qualifiedQty" key="qualifiedQty" :width="colWidth('qualifiedQty', 100)" resizable align="center" v-if="columns.qualifiedQty.visible" />
          <el-table-column label="损耗量" prop="lossQty" key="lossQty" :width="colWidth('lossQty', 90)" resizable align="center" v-if="columns.lossQty.visible"><template #default="scope"><span v-if="scope.row.lossQty > 0" class="badge red"><span class="dot"></span>{{ scope.row.lossQty }}</span><span v-else class="text-muted">{{ scope.row.lossQty }}</span></template></el-table-column>
          <el-table-column label="损耗率" prop="lossRate" key="lossRate" :width="colWidth('lossRate', 90)" resizable align="center" v-if="columns.lossRate.visible"><template #default="scope"><span :class="scope.row.lossRate > 5 ? 'text-danger' : ''">{{ scope.row.lossRate }}%</span></template></el-table-column>
          <el-table-column label="收率" prop="yieldRate" key="yieldRate" :width="colWidth('yieldRate', 90)" resizable align="center" v-if="columns.yieldRate.visible"><template #default="scope"><span class="badge" :class="yieldBadge(scope.row.yieldRate)"><span class="dot"></span>{{ scope.row.yieldRate }}%</span></template></el-table-column>
          <el-table-column label="完工率" prop="finishRate" key="finishRate" :width="colWidth('finishRate', 90)" resizable align="center" v-if="columns.finishRate.visible"><template #default="scope"><span class="badge" :class="finishBadge(scope.row.finishRate)"><span class="dot"></span>{{ scope.row.finishRate }}%</span></template></el-table-column>
          <el-table-column label="合格率" prop="qualifiedRate" key="qualifiedRate" :width="colWidth('qualifiedRate', 90)" resizable align="center" v-if="columns.qualifiedRate.visible"><template #default="scope">{{ scope.row.qualifiedRate }}%</template></el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="woStatusBadge(scope.row.status)"><span class="dot"></span>{{ dictLabel(mms_workorder_status, scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="是否准时" prop="isOnTime" key="isOnTime" :width="colWidth('isOnTime', 90)" resizable align="center" v-if="columns.isOnTime.visible"><template #default="scope"><span v-if="scope.row.isOnTime === '1'" class="badge green"><span class="dot"></span>准时</span><span v-else-if="scope.row.isOnTime === '0'" class="badge red"><span class="dot"></span>逾期</span><span v-else class="text-muted">—</span></template></el-table-column>
          <el-table-column label="实际完工" prop="actualFinish" key="actualFinish" :width="colWidth('actualFinish', 150)" resizable align="center" v-if="columns.actualFinish.visible"><template #default="scope"><span>{{ parseTime(scope.row.actualFinish, '{y}-{m}-{d} {h}:{i}') }}</span></template></el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showHelp" title="工单完工分析业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、报表释义 -->
        <h4>一、报表释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工单完工分析？</div>
          <div class="highlight-card-body">
            <p><strong>工单完工分析</strong>是核算工单成本与产出效率的生产管控报表，通过完工率、损耗率、收率等关键指标暴露超耗与低效问题。</p>
            <p>通过对工单完工数据的统计分析，帮助生产管理人员识别低效工单、定位损耗原因、优化生产流程，提升整体生产效率和资源利用率。</p>
          </div>
        </div>

        <!-- 二、核心指标定义 -->
        <h4>二、核心指标定义</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">指标计算公式</div>
          <div class="highlight-card-body">
            <p><strong>完工率</strong> = 完工数量 ÷ 计划数量 × 100% <span style="color: #f56c6c;">*系统自动计算</span></p>
            <p><strong>损耗率</strong> = 损耗量 ÷ 完工数量 × 100%（损耗量 = 完工数量 - 合格数量） <span style="color: #f56c6c;">*系统自动计算</span></p>
            <p><strong>收率/成材率</strong> = 合格数量 ÷ 完工数量 × 100% <span style="color: #f56c6c;">*系统自动计算</span></p>
            <p><strong>合格率</strong> = 合格数量 ÷ 完工数量 × 100% <span style="color: #f56c6c;">*系统自动计算</span></p>
            <p><strong>准时完工率</strong> = 实际完工日期 ≤ 计划完工日期的工单数 ÷ 总工单数 × 100% <span style="color: #f56c6c;">*系统自动计算</span></p>
            <p><strong>是否准时</strong> = 实际完工日期 ≤ 计划完工日期则为“准时”，否则为“逾期” <span style="color: #f56c6c;">*系统自动判定</span></p>
          </div>
        </div>

        <!-- 三、数据来源 -->
        <h4>三、数据来源</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单表（mms_work_order）">提供工单编号、产品信息、计划/完工数量、合格/损耗数量、计划/实际完工日期等基础数据</el-descriptions-item>
          <el-descriptions-item label="报工表（mms_work_report）">提供实际产出数量、合格/不良数量的明细记录</el-descriptions-item>
        </el-descriptions>

        <!-- 四、指标等级参考 -->
        <h4>四、指标等级参考</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">关键指标参考标准</div>
          <div class="highlight-card-body">
            <p><strong>收率 ≥ 95%</strong>：优秀（绿色徽章）— 损耗极小，生产效率高</p>
            <p><strong>收率 85% ~ 95%</strong>：一般（琉珀色徽章）— 存在一定损耗，需排查原因</p>
            <p><strong>收率 &lt; 85%</strong>：较差（红色徽章）— 损耗过大，需重点分析</p>
            <p><strong>完工率 ≥ 90%</strong>：优秀（绿色徽章）— 工单完工进度良好</p>
            <p><strong>完工率 60% ~ 90%</strong>：一般（琉珀色徽章）— 进度偏慢，需关注</p>
            <p><strong>完工率 &lt; 60%</strong>：较差（红色徽章）— 进度严重滞后</p>
          </div>
        </div>

        <!-- 五、统计卡片说明 -->
        <h4>五、统计卡片说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单总数">筛选条件下的工单总数</el-descriptions-item>
          <el-descriptions-item label="平均完工率">所有工单完工率的平均值</el-descriptions-item>
          <el-descriptions-item label="平均损耗率">所有工单损耗率的平均值</el-descriptions-item>
          <el-descriptions-item label="平均收率">所有工单收率的平均值</el-descriptions-item>
          <el-descriptions-item label="准时完工率">实际完工日期 ≤ 计划完工日期的工单数 ÷ 总工单数</el-descriptions-item>
        </el-descriptions>

      </div>
      <template #footer><el-button type="primary" @click="showHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FinishAnalysis">
import { listFinishAnalysis, getFinishAnalysisSummary } from "@/api/mms/finishAnalysis"
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { mms_workorder_status, wms_unit } = proxy.useDict("mms_workorder_status", "wms_unit")
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_finish_analysis')
const columns = reactive({
  workOrderNo: { label: '工单号', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  unit: { label: '单位', visible: true },
  resourceName: { label: '产能单元', visible: true },
  planQty: { label: '计划数量', visible: true },
  finishQty: { label: '完工数量', visible: true },
  qualifiedQty: { label: '合格数量', visible: true },
  lossQty: { label: '损耗量', visible: true },
  lossRate: { label: '损耗率', visible: true },
  yieldRate: { label: '收率', visible: true },
  finishRate: { label: '完工率', visible: true },
  qualifiedRate: { label: '合格率', visible: true },
  status: { label: '状态', visible: true },
  isOnTime: { label: '是否准时', visible: true },
  actualFinish: { label: '实际完工', visible: true }
})

const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const dateRange = ref([])
const showHelp = ref(false)
const summary = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    workOrderNo: undefined,
    productCode: undefined,
    productName: undefined,
    resourceName: undefined,
    status: undefined,
    isOnTime: undefined,
    params: {}
  }
})
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let c = 0
  if (queryParams.value.workOrderNo) c++
  if (queryParams.value.productCode) c++
  if (queryParams.value.productName) c++
  if (queryParams.value.resourceName) c++
  if (queryParams.value.status) c++
  if (queryParams.value.isOnTime) c++
  if (dateRange.value && dateRange.value.length === 2) c++
  return c
})

function getList() {
  loading.value = true
  listFinishAnalysis(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    loadSummary()
  }).catch(() => {
    loading.value = false
  })
}

function loadSummary() {
  getFinishAnalysisSummary(proxy.addDateRange({ ...queryParams.value, pageNum: 1, pageSize: 999 }, dateRange.value)).then(res => {
    summary.value = res.data || {}
  }).catch(() => {})
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.workOrderNo = undefined
  queryParams.value.productCode = undefined
  queryParams.value.productName = undefined
  queryParams.value.resourceName = undefined
  queryParams.value.status = undefined
  queryParams.value.isOnTime = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleExport() {
  proxy.download("mms/finishAnalysis/export", { ...queryParams.value }, `finish_analysis_${new Date().getTime()}.xlsx`)
}

function formatPct(val) {
  if (val == null || val === '') return '0.00'
  return Number(val).toFixed(2)
}

function dictLabel(dictRef, value) {
  if (value === null || value === undefined || value === '') return '—'
  const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef
  if (!arr || !Array.isArray(arr)) return '—'
  const item = arr.find(d => d.value == value)
  return item ? item.label : '—'
}

function unitLabel(unit) {
  if (unit === null || unit === undefined || unit === '') return '—'
  const item = wms_unit.value.find(d => d.value == unit)
  return item ? item.label : '—'
}

function yieldBadge(val) {
  if (val >= 95) return 'green'
  if (val >= 85) return 'amber'
  return 'red'
}

function finishBadge(val) {
  if (val >= 90) return 'green'
  if (val >= 60) return 'amber'
  return 'red'
}

function woStatusBadge(status) {
  const map = { '0': 'gray', '1': 'blue', '2': 'amber', '3': 'green', '4': 'gray', '5': 'amber', '6': 'red' }
  return map[status] || 'gray'
}

// 表尾汇总行
function getSummaryRow({ columns, data }) {
  const sums = []
  columns.forEach((col, index) => {
    if (index === 0) { sums[index] = '合计'; return }
    const prop = col.property
    if (['planQty', 'finishQty', 'qualifiedQty', 'lossQty'].includes(prop)) {
      const total = data.reduce((prev, row) => {
        const val = Number(row[prop] || 0)
        return isNaN(val) ? prev : prev + val
      }, 0)
      sums[index] = total.toFixed(2)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

getList()
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-finish-analysis-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --teal-50:#f0fdfa; --teal-500:#14b8a6; --teal-700:#0f766e;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);
  color:var(--ink-900);
}

/* 统计卡片 */
.stat-cards { display:grid; grid-template-columns:repeat(5, minmax(0,1fr)); gap:12px; margin-bottom:10px; }
.stat-card {
  display:flex; align-items:center; gap:14px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg);
  padding:16px 18px; box-shadow:var(--shadow-card); position:relative; overflow:hidden;
}
.stat-card::before { content:''; position:absolute; left:0; top:0; bottom:0; width:4px; }
.stat-card--blue::before { background:var(--blue-500); }
.stat-card--green::before { background:var(--green-500); }
.stat-card--amber::before { background:var(--amber-500); }
.stat-card--indigo::before { background:var(--brand-500); }
.stat-card--teal::before { background:var(--teal-500); }
.stat-card-icon {
  width:44px; height:44px; border-radius:var(--r-md); display:flex; align-items:center; justify-content:center; flex-shrink:0;
}
.stat-card--blue .stat-card-icon { background:var(--blue-50); color:var(--blue-700); }
.stat-card--green .stat-card-icon { background:var(--green-50); color:var(--green-700); }
.stat-card--amber .stat-card-icon { background:var(--amber-50); color:var(--amber-700); }
.stat-card--indigo .stat-card-icon { background:var(--brand-50); color:var(--brand-700); }
.stat-card--teal .stat-card-icon { background:var(--teal-50); color:var(--teal-700); }
.stat-card-body { flex:1; min-width:0; }
.stat-card-label { font-size:13px; color:var(--ink-500); font-weight:500; margin-bottom:4px; }
.stat-card-value { font-size:24px; font-weight:700; color:var(--ink-900); line-height:1; font-feature-settings:"tnum" 1; }
.stat-card-unit { font-size:13px; font-weight:500; color:var(--ink-400); margin-left:3px; }

/* Badge */
.badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.badge .dot { width:6px; height:6px; border-radius:50%; }
.badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .badge.green .dot { background:var(--green-500); }
.badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .badge.amber .dot { background:var(--amber-500); }
.badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .badge.red .dot { background:var(--red-500); }
.badge.gray { background:var(--ink-50); color:var(--ink-700); border-color:var(--ink-200); } .badge.gray .dot { background:var(--ink-400); }
.badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .badge.blue .dot { background:var(--blue-500); }
.badge.violet { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.text-muted { color:var(--ink-400); font-size:13px; }
.text-danger { color:var(--red-700); font-weight:600; }

/* Status Tabs */
.mms-finish-analysis-page .status-tabs { display: flex; align-items: center; gap: 12px; padding: 6px 10px 6px 12px; border-bottom: 1px solid var(--ink-200); background: #fff; }
.mms-finish-analysis-page .tabs-track { display: flex; align-items: center; gap: 4px; flex: 1; min-width: 0; overflow-x: auto; scrollbar-width: none; }
.mms-finish-analysis-page .tabs-track::-webkit-scrollbar { display: none; }
.mms-finish-analysis-page .status-tab { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: var(--r-sm); font-size: 14px; color: var(--ink-500); cursor: pointer; user-select: none; transition: all .15s cubic-bezier(.16,.84,.44,1); white-space: nowrap; border: 1px solid transparent; background: transparent; }
.mms-finish-analysis-page .status-tab .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--ink-300); }
.mms-finish-analysis-page .status-tab .count { font-size: 12px; font-weight: 600; padding: 1px 6px; border-radius: 999px; background: var(--ink-100); color: var(--ink-500); min-width: 18px; text-align: center; line-height: 1.4; font-feature-settings: "tnum" 1; }
.mms-finish-analysis-page .status-tab:hover { background: var(--ink-50); color: var(--ink-700); }
.mms-finish-analysis-page .status-tab.is-active { background: var(--brand-50); color: var(--brand-700); font-weight: 600; border-color: var(--brand-200); }
.mms-finish-analysis-page .status-tab.is-active .count { background: var(--brand-600); color: #fff; }
.mms-finish-analysis-page .status-tab.is-active .dot { background: var(--brand-500); }

/* Tip Pill */
.mms-finish-analysis-page .tip-pill { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; font-size: 13px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid #fde68a; background: #fffaf0; color: #92400e; cursor: pointer; white-space: nowrap; flex-shrink: 0; transition: all 0.15s ease; }
.mms-finish-analysis-page .tip-pill:hover { background: #fffbeb; border-color: #f59e0b; color: #78350f; }
.mms-finish-analysis-page .tip-pill .el-icon { font-size: 14px; }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }

@media (max-width: 1280px) { .stat-cards { grid-template-columns:repeat(3, minmax(0,1fr)); } }
@media (max-width: 768px) { .stat-cards { grid-template-columns:repeat(2, minmax(0,1fr)); } }
</style>
