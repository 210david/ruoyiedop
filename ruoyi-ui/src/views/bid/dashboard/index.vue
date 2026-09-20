<template>
  <div class="app-container bid-dashboard-page">
    <!-- 顶部操作栏 -->
    <div class="surface dash-toolbar">
      <el-radio-group v-model="timeRange" @change="loadAll">
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="quarter">本季</el-radio-button>
        <el-radio-button value="year">本年</el-radio-button>
      </el-radio-group>
      <div class="toolbar-right">
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
        <el-button icon="Refresh" @click="loadAll">刷新</el-button>
      </div>
    </div>

    <!-- KPI 指标卡 -->
    <el-row :gutter="12" class="kpi-row">
      <el-col :span="4" v-for="card in kpiCards" :key="card.label">
        <div class="surface kpi-card" :class="card.cls">
          <div class="kpi-label">{{ card.label }}</div>
          <div class="kpi-value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="12">
      <el-col :span="8">
        <div class="surface chart-card">
          <div class="chart-title">投标状态分布</div>
          <div ref="statusChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="surface chart-card">
          <div class="chart-title">月度投标趋势（投标数/中标数）</div>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="surface chart-card">
          <div class="chart-title">未中标/废标原因占比</div>
          <div ref="failChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="BidDashboard">
import * as echarts from 'echarts'
import { getOverview, getStatusDistribution, getTrend, getFailReason } from '@/api/bid/dashboard'

const statusChartRef = ref(null)
const trendChartRef = ref(null)
const failChartRef = ref(null)
const timeRange = ref('year')
const autoRefresh = ref(false)
const overview = ref({})
let statusChart, trendChart, failChart, refreshTimer

const kpiCards = computed(() => {
  const p = overview.value.project || {}
  const d = overview.value.deposit || {}
  return [
    { label: '累计投标项目', value: p.totalProjects ?? 0, cls: 'c-blue' },
    { label: '在投项目', value: p.ongoing ?? 0, cls: 'c-violet' },
    { label: '累计中标', value: p.won ?? 0, cls: 'c-green' },
    { label: '中标率', value: p.winRate ? (p.winRate * 100).toFixed(1) + '%' : '0.0%', cls: 'c-brand' },
    { label: '在外保证金', value: '￥' + formatMoney(d.outAmount || 0), cls: 'c-amber' }
  ]
})

function formatMoney(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function loadAll() {
  getOverview().then(res => { overview.value = res.data })
  getStatusDistribution().then(res => renderStatus(res.data || []))
  getTrend().then(res => renderTrend(res.data || []))
  getFailReason().then(res => renderFail(res.data || []))
}

function renderStatus(rows) {
  if (!statusChartRef.value) return
  statusChart = statusChart || echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
      label: { show: true, formatter: '{b}: {c}' },
      data: rows.map(r => ({ name: r.status, value: r.cnt }))
    }]
  }, true)
}

function renderTrend(rows) {
  if (!trendChartRef.value) return
  trendChart = trendChart || echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 40, right: 40, top: 30, bottom: 50 },
    xAxis: { type: 'category', data: rows.map(r => r.month) },
    yAxis: [{ type: 'value', name: '投标数' }, { type: 'value', name: '中标数' }],
    series: [
      { name: '投标数', type: 'bar', data: rows.map(r => r.total), itemStyle: { borderRadius: [4, 4, 0, 0] } },
      { name: '中标数', type: 'line', yAxisIndex: 1, smooth: true, data: rows.map(r => r.won) }
    ]
  }, true)
}

function renderFail(rows) {
  if (!failChartRef.value) return
  failChart = failChart || echarts.init(failChartRef.value)
  failChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', roseType: 'radius', radius: ['20%', '65%'], center: ['50%', '50%'],
      label: { show: true, formatter: '{b}: {c}' },
      data: rows.map(r => ({ name: r.failReason, value: r.cnt }))
    }]
  }, true)
}

function handleResize() {
  statusChart && statusChart.resize()
  trendChart && trendChart.resize()
  failChart && failChart.resize()
}

watch(autoRefresh, v => {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (v) { refreshTimer = setInterval(loadAll, 30000) }
})

onMounted(() => { loadAll(); window.addEventListener('resize', handleResize) })
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) { clearInterval(refreshTimer) }
  statusChart && statusChart.dispose()
  trendChart && trendChart.dispose()
  failChart && failChart.dispose()
})
</script>

<style scoped>
.bid-dashboard-page { padding-top:10px; --brand-50:#eef2ff; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-700:#b45309; --blue-700:#1d4ed8; --green-700:#047857; --violet-700:#7c3aed; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.bid-dashboard-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:12px; }
.bid-dashboard-page .dash-toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; }
.bid-dashboard-page .toolbar-right { display:flex; align-items:center; gap:12px; }
.bid-dashboard-page .kpi-row { margin-bottom: 0; }
.bid-dashboard-page .kpi-card { padding:14px 16px; transition: transform .2s ease, box-shadow .2s ease; border-left: 4px solid transparent; }
.bid-dashboard-page .kpi-card:hover { transform: translateY(-3px); box-shadow: 0 6px 14px rgba(15,23,42,.08); }
.bid-dashboard-page .kpi-label { font-size:12px; color:var(--ink-500); }
.bid-dashboard-page .kpi-value { font-size:22px; font-weight:700; margin-top:6px; }
.bid-dashboard-page .c-blue { border-left-color:#3b82f6; } .bid-dashboard-page .c-blue .kpi-value { color:var(--blue-700); }
.bid-dashboard-page .c-violet { border-left-color:#8b5cf6; } .bid-dashboard-page .c-violet .kpi-value { color:var(--violet-700); }
.bid-dashboard-page .c-green { border-left-color:#10b981; } .bid-dashboard-page .c-green .kpi-value { color:var(--green-700); }
.bid-dashboard-page .c-brand { border-left-color:#6366f1; } .bid-dashboard-page .c-brand .kpi-value { color:var(--brand-700); }
.bid-dashboard-page .c-amber { border-left-color:#f59e0b; } .bid-dashboard-page .c-amber .kpi-value { color:var(--amber-700); }
.bid-dashboard-page .chart-card { padding:14px 16px; }
.bid-dashboard-page .chart-title { font-size:14px; font-weight:600; margin-bottom:8px; }
.bid-dashboard-page .chart-box { height:300px; width:100%; }
</style>
