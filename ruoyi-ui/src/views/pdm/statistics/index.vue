<template>
  <div class="app-container pdm-statistics-page">
    <!-- ===== KPI 行 ===== -->
    <el-row :gutter="12" class="kpi-row">
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <div class="kpi-card kpi-card--blue">
          <div class="kpi-card__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <div class="kpi-card__body"><div class="kpi-card__label">产品档案</div><div class="kpi-card__value">{{ summary.productCount || 0 }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <div class="kpi-card kpi-card--green">
          <div class="kpi-card__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <div class="kpi-card__body"><div class="kpi-card__label">设计方案</div><div class="kpi-card__value">{{ summary.schemeCount || 0 }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <div class="kpi-card kpi-card--amber">
          <div class="kpi-card__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="kpi-card__body"><div class="kpi-card__label">图纸文件</div><div class="kpi-card__value">{{ summary.fileCount || 0 }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <div class="kpi-card kpi-card--indigo">
          <div class="kpi-card__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg></div>
          <div class="kpi-card__body"><div class="kpi-card__label">设计BOM</div><div class="kpi-card__value">{{ summary.bomCount || 0 }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <div class="kpi-card kpi-card--teal">
          <div class="kpi-card__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg></div>
          <div class="kpi-card__body"><div class="kpi-card__label">变更单</div><div class="kpi-card__value">{{ summary.changeCount || 0 }}</div></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <div class="kpi-card kpi-card--rose">
          <div class="kpi-card__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 2v6h-6"/><path d="M3 12a9 9 0 0 1 15-6.7L21 8"/><path d="M3 22v-6h6"/><path d="M21 12a9 9 0 0 1-15 6.7L3 16"/></svg></div>
          <div class="kpi-card__body"><div class="kpi-card__label">BOM下发</div><div class="kpi-card__value">{{ summary.releaseCount || 0 }}</div></div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 图表区 行1：分类分布 + 状态分布 ===== -->
    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="chart-panel">
          <div class="chart-panel__head"><span class="chart-panel__title">产品分类分布</span><span class="chart-panel__desc">各分类下产品档案数量</span></div>
          <div ref="categoryChart" class="chart-panel__body"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="chart-panel">
          <div class="chart-panel__head"><span class="chart-panel__title">产品状态分布</span><span class="chart-panel__desc">全生命周期状态占比</span></div>
          <div ref="statusChart" class="chart-panel__body"></div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 图表区 行2：月度趋势 + 变更状态 ===== -->
    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="chart-panel">
          <div class="chart-panel__head"><span class="chart-panel__title">设计文档月度新增趋势</span><span class="chart-panel__desc">近 12 个月方案/图纸/BOM/变更单</span></div>
          <div ref="trendChart" class="chart-panel__body"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="chart-panel">
          <div class="chart-panel__head"><span class="chart-panel__title">变更单状态分布</span><span class="chart-panel__desc">各状态变更单数量</span></div>
          <div ref="changeChart" class="chart-panel__body"></div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 图表区 行3：产品产出TOP + 单据总量对比 ===== -->
    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :lg="14">
        <div class="chart-panel">
          <div class="chart-panel__head"><span class="chart-panel__title">产品设计产出 TOP10</span><span class="chart-panel__desc">按方案+图纸+BOM 产出量排名</span></div>
          <div ref="topChart" class="chart-panel__body"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="chart-panel">
          <div class="chart-panel__head"><span class="chart-panel__title">各类单据总量对比</span><span class="chart-panel__desc">设计文档资产结构</span></div>
          <div ref="totalChart" class="chart-panel__body"></div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 明细表格 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <span class="surface-title">产品统计明细</span>
          <span class="surface-desc">共 {{ total }} 条</span>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pdm_statistics_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @header-dragend="onHeaderDragEnd" class="app-table" show-summary :summary-method="getSummaryRow">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" align="center" resizable />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 170)" resizable v-if="columns.productCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.productCode }}</span></template>
          </el-table-column>
          <el-table-column label="产品名称" prop="productName" key="productName" :min-width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="产品分类" prop="categoryName" key="categoryName" :width="colWidth('categoryName', 140)" resizable show-overflow-tooltip v-if="columns.categoryName.visible">
            <template #default="scope"><span class="badge violet">{{ scope.row.categoryName || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="产品状态" prop="productStatus" key="productStatus" :width="colWidth('productStatus', 110)" resizable align="center" v-if="columns.productStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.productStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.productStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="定型日期" prop="finalDate" key="finalDate" :width="colWidth('finalDate', 110)" resizable align="center" v-if="columns.finalDate.visible">
            <template #default="scope">{{ scope.row.finalDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="设计方案数" prop="schemeCount" key="schemeCount" :width="colWidth('schemeCount', 110)" resizable align="center" sortable v-if="columns.schemeCount.visible">
            <template #default="scope"><span class="num-strong">{{ scope.row.schemeCount }}</span></template>
          </el-table-column>
          <el-table-column label="图纸文件数" prop="fileCount" key="fileCount" :width="colWidth('fileCount', 110)" resizable align="center" sortable v-if="columns.fileCount.visible">
            <template #default="scope"><span class="num-strong">{{ scope.row.fileCount }}</span></template>
          </el-table-column>
          <el-table-column label="设计BOM数" prop="bomCount" key="bomCount" :width="colWidth('bomCount', 110)" resizable align="center" sortable v-if="columns.bomCount.visible">
            <template #default="scope"><span class="num-strong">{{ scope.row.bomCount }}</span></template>
          </el-table-column>
          <el-table-column label="变更单数" prop="changeCount" key="changeCount" :width="colWidth('changeCount', 100)" resizable align="center" sortable v-if="columns.changeCount.visible">
            <template #default="scope">
              <span class="badge red" v-if="scope.row.changeCount > 0"><span class="dot"></span>{{ scope.row.changeCount }}</span>
              <span v-else class="text-muted">{{ scope.row.changeCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最近变更时间" prop="lastChangeTime" key="lastChangeTime" :width="colWidth('lastChangeTime', 170)" resizable align="center" v-if="columns.lastChangeTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.lastChangeTime) || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" resizable align="center" v-if="columns.createTime.visible" />
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="PdmStatistics">
import * as echarts from 'echarts'
import { listStatistics, getStatisticsSummary, getStatisticsCharts } from '@/api/pdm/statistics'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { pdm_product_status, pdm_change_status } = proxy.useDict('pdm_product_status', 'pdm_change_status')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_statistics')

const defaultColumns = {
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  categoryName: { label: '产品分类', visible: true },
  productStatus: { label: '产品状态', visible: true },
  finalDate: { label: '定型日期', visible: true },
  schemeCount: { label: '设计方案数', visible: true },
  fileCount: { label: '图纸文件数', visible: true },
  bomCount: { label: '设计BOM数', visible: true },
  changeCount: { label: '变更单数', visible: true },
  lastChangeTime: { label: '最近变更时间', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pdm_statistics_columns')
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

const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const summary = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productCode: undefined,
    productName: undefined,
    categoryId: undefined,
    productStatus: undefined,
    params: {}
  }
})
const { queryParams } = toRefs(data)

function statusLabel(status) {
  if (status === null || status === undefined || status === '') return '-'
  const item = pdm_product_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function statusBadgeClass(status) {
  const map = { '0': 'gray', '1': 'blue', '2': 'green', '3': 'amber', '4': 'red' }
  return map[status] || 'gray'
}

/** 汇总行：各数量列求和 */
function getSummaryRow({ columns: cols, data }) {
  const sums = []
  cols.forEach((col, index) => {
    if (index === 0) { sums[index] = '合计'; return }
    if (['schemeCount', 'fileCount', 'bomCount', 'changeCount'].includes(col.property)) {
      const values = data.map(item => Number(item[col.property]))
      sums[index] = values.reduce((prev, cur) => {
        const value = Number(cur)
        if (!Number.isNaN(value)) return prev + cur
        return prev
      }, 0)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

function getList() {
  loading.value = true
  listStatistics(queryParams.value).then(res => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

// ===== ECharts =====
const palette = ['#4f7cff', '#10b981', '#f59e0b', '#8b5cf6', '#14b8a6', '#ef4444', '#0ea5e9', '#eab308', '#f97316', '#6366f1']
const chartTextColor = computed(() => '#606266')
const chartSplitColor = computed(() => '#eee')
const categoryChart = ref(null); const statusChart = ref(null); const trendChart = ref(null)
const changeChart = ref(null); const topChart = ref(null); const totalChart = ref(null)
let instances = []
let chartData = { categoryDist: [], statusDist: [], monthTrend: [], changeStatusDist: [], productTop: [] }

function dictLabel(list, value) {
  const item = (list || []).find(d => d.value == value)
  return item ? item.label : value
}
const emptyTipOption = { title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#94a3b8', fontSize: 14, fontWeight: 500 } } }

function renderCategoryChart() {
  const raw = chartData.categoryDist || []
  instances[0].setOption(raw.length ? {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 个 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '46%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { color: chartTextColor.value, formatter: '{b}\n{c} 个' },
      data: raw.map((d, i) => ({ name: d.name, value: Number(d.value), itemStyle: { color: palette[i % palette.length] } }))
    }]
  } : emptyTipOption)
}
function renderStatusChart() {
  const raw = (chartData.statusDist || []).map(d => ({ name: dictLabel(pdm_product_status.value, d.name), value: Number(d.value) }))
  instances[1].setOption(raw.length ? {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 个 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    series: [{
      type: 'pie', radius: ['52%', '74%'], center: ['50%', '46%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false }, emphasis: { label: { show: true, formatter: '{b}\n{c} 个' } },
      data: raw.map((d, i) => ({ ...d, itemStyle: { color: palette[i % palette.length] } }))
    }]
  } : emptyTipOption)
}
function renderTrendChart() {
  const raw = chartData.monthTrend || []
  instances[2].setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: 10, right: 16, top: 30, bottom: 44, containLabel: true },
    xAxis: { type: 'category', data: raw.map(d => d.month), boundaryGap: false, axisLabel: { color: chartTextColor.value }, axisLine: { lineStyle: { color: chartSplitColor.value } } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: chartTextColor.value }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    series: [
      { name: '设计方案', type: 'line', smooth: true, data: raw.map(d => Number(d.schemeCount)), itemStyle: { color: '#4f7cff' }, lineStyle: { width: 2.5 }, symbolSize: 6 },
      { name: '图纸文件', type: 'line', smooth: true, data: raw.map(d => Number(d.fileCount)), itemStyle: { color: '#f59e0b' }, lineStyle: { width: 2.5 }, symbolSize: 6 },
      { name: '设计BOM', type: 'line', smooth: true, data: raw.map(d => Number(d.bomCount)), itemStyle: { color: '#10b981' }, lineStyle: { width: 2.5 }, symbolSize: 6 },
      { name: '变更单', type: 'line', smooth: true, data: raw.map(d => Number(d.changeCount)), itemStyle: { color: '#ef4444' }, lineStyle: { width: 2.5 }, symbolSize: 6 }
    ]
  })
}
function renderChangeChart() {
  const raw = (chartData.changeStatusDist || []).map(d => ({ name: dictLabel(pdm_change_status.value, d.name), value: Number(d.value) }))
  instances[3].setOption(raw.length ? {
    tooltip: { trigger: 'axis' },
    grid: { left: 10, right: 16, top: 30, bottom: 10, containLabel: true },
    xAxis: { type: 'category', data: raw.map(d => d.name), axisLabel: { color: chartTextColor.value, interval: 0, rotate: raw.length > 5 ? 24 : 0 }, axisLine: { lineStyle: { color: chartSplitColor.value } }, axisTick: { show: false } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: chartTextColor.value }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    series: [{
      type: 'bar', barMaxWidth: 32, data: raw.map((d, i) => ({ ...d, itemStyle: { color: palette[i % palette.length], borderRadius: [6, 6, 0, 0] } })),
      label: { show: true, position: 'top', color: chartTextColor.value }
    }]
  } : emptyTipOption)
}
function renderTopChart() {
  const raw = chartData.productTop || []
  instances[4].setOption(raw.length ? {
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: 10, right: 26, top: 30, bottom: 44, containLabel: true },
    xAxis: { type: 'value', minInterval: 1, axisLabel: { color: chartTextColor.value }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    yAxis: { type: 'category', data: raw.map(d => d.name).reverse(), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value, width: 110, overflow: 'truncate' } },
    series: [
      { name: '设计方案', type: 'bar', stack: 'total', barMaxWidth: 18, data: raw.map(d => Number(d.schemeCount)).reverse(), itemStyle: { color: '#4f7cff' } },
      { name: '图纸文件', type: 'bar', stack: 'total', barMaxWidth: 18, data: raw.map(d => Number(d.fileCount)).reverse(), itemStyle: { color: '#f59e0b' } },
      { name: '设计BOM', type: 'bar', stack: 'total', barMaxWidth: 18, data: raw.map(d => Number(d.bomCount)).reverse(), itemStyle: { color: '#10b981', borderRadius: [0, 6, 6, 0] } }
    ]
  } : emptyTipOption)
}
function renderTotalChart() {
  const s = summary.value
  instances[5].setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 10, right: 16, top: 30, bottom: 10, containLabel: true },
    xAxis: { type: 'category', data: ['设计方案', '图纸文件', '设计BOM', '变更单', 'BOM下发'], axisLabel: { color: chartTextColor.value, interval: 0 }, axisLine: { lineStyle: { color: chartSplitColor.value } }, axisTick: { show: false } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: chartTextColor.value }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    series: [{
      type: 'bar', barMaxWidth: 36,
      data: [
        { value: Number(s.schemeCount || 0), itemStyle: { color: '#4f7cff' } },
        { value: Number(s.fileCount || 0), itemStyle: { color: '#f59e0b' } },
        { value: Number(s.bomCount || 0), itemStyle: { color: '#10b981' } },
        { value: Number(s.changeCount || 0), itemStyle: { color: '#ef4444' } },
        { value: Number(s.releaseCount || 0), itemStyle: { color: '#8b5cf6' } }
      ].map(d => ({ ...d, itemStyle: { ...d.itemStyle, borderRadius: [6, 6, 0, 0] } })),
      label: { show: true, position: 'top', color: chartTextColor.value }
    }]
  })
}

function initCharts() {
  instances.forEach(i => i.dispose())
  instances = [categoryChart, statusChart, trendChart, changeChart, topChart, totalChart].map(refEl => echarts.init(refEl.value))
}

function renderAll() {
  if (!instances.length) return
  renderCategoryChart()
  renderStatusChart()
  renderTrendChart()
  renderChangeChart()
  renderTopChart()
  renderTotalChart()
}

function loadCharts() {
  Promise.all([
    getStatisticsCharts().then(res => { chartData = res.data || {} }),
    getStatisticsSummary({ pageNum: 1, pageSize: 1, params: {} }).then(res => { summary.value = res.data || {} })
  ]).then(() => {
    nextTick(() => { initCharts(); renderAll() })
  })
}

const onResize = () => { instances.forEach(i => i.resize()) }
onMounted(() => { window.addEventListener('resize', onResize) })
onBeforeUnmount(() => { window.removeEventListener('resize', onResize); instances.forEach(i => i.dispose()); instances = [] })

watch([pdm_product_status, pdm_change_status], () => { if (instances.length) nextTick(renderAll) })

function resetQuery() {
  queryParams.value = {
    pageNum: 1,
    pageSize: queryParams.value.pageSize,
    productCode: undefined,
    productName: undefined,
    categoryId: undefined,
    productStatus: undefined,
    params: {}
  }
  getList()
}

function handleExport() {
  proxy.download('pdm/statistics/export', { ...queryParams.value }, `pdm_statistics_${new Date().getTime()}.xlsx`)
}

getList()
loadCharts()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pdm-statistics-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== KPI 卡片 ===== */
.pdm-statistics-page .kpi-row { margin-bottom: 4px; }
.pdm-statistics-page .kpi-card { display:flex; align-items:center; gap:10px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); padding:14px 16px; margin-bottom:8px; }
.pdm-statistics-page .kpi-card__icon { display:inline-flex; align-items:center; justify-content:center; width:40px; height:40px; border-radius:11px; flex-shrink:0; }
.pdm-statistics-page .kpi-card--blue .kpi-card__icon { background:#eff6ff; color:#3b82f6; }
.pdm-statistics-page .kpi-card--green .kpi-card__icon { background:#ecfdf5; color:#10b981; }
.pdm-statistics-page .kpi-card--amber .kpi-card__icon { background:#fffbeb; color:#f59e0b; }
.pdm-statistics-page .kpi-card--indigo .kpi-card__icon { background:var(--brand-50); color:var(--brand-500); }
.pdm-statistics-page .kpi-card--teal .kpi-card__icon { background:#f0fdfa; color:#14b8a6; }
.pdm-statistics-page .kpi-card--rose .kpi-card__icon { background:#fff1f2; color:#f43f5e; }
.pdm-statistics-page .kpi-card__label { font-size:12px; color:var(--ink-500); margin-bottom:2px; white-space:nowrap; }
.pdm-statistics-page .kpi-card__value { font-size:22px; font-weight:700; color:var(--ink-900); line-height:1.1; }

/* ===== 图表面板 ===== */
.pdm-statistics-page .chart-row { margin-bottom: 4px; }
.pdm-statistics-page .chart-panel { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); margin-bottom:8px; padding:14px 16px; }
.pdm-statistics-page .chart-panel__head { display:flex; align-items:baseline; gap:10px; margin-bottom:8px; }
.pdm-statistics-page .chart-panel__title { font-size:14px; font-weight:600; color:var(--ink-900); }
.pdm-statistics-page .chart-panel__title::before { content:''; display:inline-block; width:4px; height:12px; border-radius:2px; background:var(--brand-600); margin-right:8px; vertical-align:-1px; }
.pdm-statistics-page .chart-panel__desc { font-size:12px; color:var(--ink-400); }
.pdm-statistics-page .chart-panel__body { height:300px; width:100%; }

/* ===== Surface / Toolbar / Table ===== */
.pdm-statistics-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pdm-statistics-page .surface-title { font-size:14px; font-weight:600; color:var(--ink-900); }
.pdm-statistics-page .surface-title::before { content:''; display:inline-block; width:4px; height:12px; border-radius:2px; background:var(--brand-600); margin-right:8px; }
.pdm-statistics-page .surface-desc { font-size:12px; color:var(--ink-400); margin-left:8px; }
.pdm-statistics-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-statistics-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-statistics-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pdm-statistics-page .table-wrap { overflow-x:auto; }
.pdm-statistics-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-statistics-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-statistics-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-statistics-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-statistics-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-statistics-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-statistics-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-statistics-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-statistics-page .app-table :deep(.el-table__footer td) { background:var(--ink-50) !important; font-weight:600; color:var(--ink-900); border-top:1px solid var(--ink-200); }
.pdm-statistics-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); }
.pdm-statistics-page .app-table .num-strong { font-weight:600; color:var(--ink-900); }
.pdm-statistics-page .text-muted { color:var(--ink-400); }

/* ===== Badges ===== */
.pdm-statistics-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-statistics-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-statistics-page .badge.green { background:#ecfdf5; color:#047857; border-color:#a7f3d0; }
.pdm-statistics-page .badge.green .dot { background:#10b981; }
.pdm-statistics-page .badge.blue { background:#eff6ff; color:#1d4ed8; border-color:#bfdbfe; }
.pdm-statistics-page .badge.blue .dot { background:#3b82f6; }
.pdm-statistics-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.pdm-statistics-page .badge.amber .dot { background:#f59e0b; }
.pdm-statistics-page .badge.red { background:#fef2f2; color:#b91c1c; border-color:#fecaca; }
.pdm-statistics-page .badge.red .dot { background:#ef4444; }
.pdm-statistics-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-statistics-page .badge.gray .dot { background:var(--ink-400); }
.pdm-statistics-page .badge.violet { background:#f5f3ff; color:var(--brand-700); border-color:var(--brand-200); }

@media (max-width:1280px) { .pdm-statistics-page .kpi-row .el-col { margin-bottom: 0; } }
</style>
