<template>
  <div class="app-container bid-dashboard-page">
    <!-- 顶部操作栏 -->
    <div class="surface dash-toolbar">
      <div class="dash-title">
        <span class="pulse-dot"></span>
        <span class="dash-title-text">投标运营看板</span>
        <span class="dash-sub">数据每 {{ autoRefresh ? '30 秒' : '进入页面时' }}更新</span>
      </div>
      <div class="toolbar-right">
        <el-radio-group v-model="timeRange" @change="loadAll">
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="quarter">本季</el-radio-button>
          <el-radio-button value="year">本年</el-radio-button>
        </el-radio-group>
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
        <el-button icon="Refresh" @click="loadAll">刷新</el-button>
      </div>
    </div>

    <!-- KPI 指标卡 -->
    <el-row :gutter="12" class="kpi-row">
      <el-col :span="4" v-for="(card, i) in kpiCards" :key="card.label">
        <div class="surface kpi-card" :class="card.cls" :style="{ animationDelay: (i * 0.08) + 's' }">
          <div class="kpi-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" v-html="card.icon"></svg>
          </div>
          <div class="kpi-meta">
            <div class="kpi-label">{{ card.label }}</div>
            <div class="kpi-value">{{ card.display }}</div>
            <div class="kpi-foot" v-if="card.foot">{{ card.foot }}</div>
          </div>
          <div class="kpi-spark" :class="card.cls"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 第一行 -->
    <el-row :gutter="12">
      <el-col :span="8">
        <div class="surface chart-card" :style="{ animationDelay: '0.35s' }">
          <div class="chart-title"><span class="bar"></span>投标状态分布</div>
          <div ref="statusChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="surface chart-card" :style="{ animationDelay: '0.45s' }">
          <div class="chart-title"><span class="bar"></span>月度投标趋势（投标数 / 中标数 / 中标率）</div>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="surface chart-card" :style="{ animationDelay: '0.55s' }">
          <div class="chart-title"><span class="bar"></span>中标率仪表盘</div>
          <div ref="gaugeChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 第二行 -->
    <el-row :gutter="12">
      <el-col :span="8">
        <div class="surface chart-card" :style="{ animationDelay: '0.65s' }">
          <div class="chart-title"><span class="bar"></span>投标转化漏斗</div>
          <div ref="funnelChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="surface chart-card" :style="{ animationDelay: '0.75s' }">
          <div class="chart-title"><span class="bar"></span>未中标/废标原因占比</div>
          <div ref="failChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="surface chart-card" :style="{ animationDelay: '0.85s' }">
          <div class="chart-title"><span class="bar"></span>保证金概览</div>
          <div ref="depositChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 第三行：金额维度 -->
    <el-row :gutter="12">
      <el-col :span="24">
        <div class="surface chart-card" :style="{ animationDelay: '0.95s' }">
          <div class="chart-title"><span class="bar"></span>各月投标金额 vs 中标金额对比（万元）</div>
          <div ref="amountChartRef" class="chart-box chart-box--wide"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="BidDashboard">
import * as echarts from 'echarts'
import { getOverview, getStatusDistribution, getTrend, getFailReason } from '@/api/bid/dashboard'

const { proxy } = getCurrentInstance()
const { bid_project_status, bid_fail_reason } = proxy.useDict('bid_project_status', 'bid_fail_reason')

const statusChartRef = ref(null)
const trendChartRef = ref(null)
const failChartRef = ref(null)
const gaugeChartRef = ref(null)
const funnelChartRef = ref(null)
const depositChartRef = ref(null)
const amountChartRef = ref(null)
const timeRange = ref('year')
const autoRefresh = ref(false)
const overview = ref({})
const statusRows = ref([])
let statusChart, trendChart, failChart, gaugeChart, funnelChart, depositChart, amountChart, refreshTimer, spinTimer
// 数字滚动动画状态
const counters = ref({ totalProjects: 0, ongoing: 0, won: 0, winRate: 0, outAmount: 0, outCount: 0 })
const tweenStore = {}

const ICONS = {
  total: '<path d="M3 3h18v18H3z"/><path d="M3 9h18M3 15h18M9 3v18M15 3v18"/>',
  ongoing: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/>',
  won: '<path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M7 6H4a2 2 0 0 0 2 4M17 6h3a2 2 0 0 1-2 4"/>',
  rate: '<path d="M3 3v18h18"/><path d="M7 14l4-4 3 3 5-6"/>',
  out: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="3"/>',
  count: '<path d="M21 8V21H3V8"/><path d="M1 3h22v5H1z"/><path d="M10 12h4"/>'
}

const kpiCards = computed(() => {
  const c = counters.value
  const d = overview.value.deposit || {}
  return [
    { label: '累计投标项目', value: c.totalProjects, display: Math.round(c.totalProjects).toLocaleString(), cls: 'c-blue', icon: ICONS.total, foot: '全量台账' },
    { label: '在投项目', value: c.ongoing, display: Math.round(c.ongoing).toLocaleString(), cls: 'c-violet', icon: ICONS.ongoing, foot: '进行中流转' },
    { label: '累计中标', value: c.won, display: Math.round(c.won).toLocaleString(), cls: 'c-green', icon: ICONS.won, foot: '已签约转化' },
    { label: '中标率', value: c.winRate, display: (c.winRate * 100).toFixed(1) + '%', cls: 'c-brand', icon: ICONS.rate, foot: '中标/已出结果' },
    { label: '在外保证金', value: c.outAmount, display: formatAmountCompact(c.outAmount), cls: 'c-amber', icon: ICONS.out, foot: (d.outCount ?? Math.round(c.outCount)) + ' 笔在外' },
    { label: '保证金占用', value: c.outCount, display: Math.round(c.outCount).toLocaleString() + ' 笔', cls: 'c-cyan', icon: ICONS.count, foot: '待退回跟踪' }
  ]
})

// 数字滚动动画
function tween(key, target, duration = 900, format) {
  const from = tweenStore[key] || 0
  tweenStore[key] = target
  const start = performance.now()
  function step(now) {
    const p = Math.min(1, (now - start) / duration)
    const eased = 1 - Math.pow(1 - p, 3)
    counters.value[key] = from + (target - from) * eased
    if (p < 1) requestAnimationFrame(step)
    else if (format) counters.value[key] = target
  }
  requestAnimationFrame(step)
}

function formatMoney(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 金额自适应单位：>= 100万 转万元显示（保留2位），避免卡片数值过长显示不全 */
function formatAmountCompact(v) {
  const n = Number(v || 0)
  if (n >= 1000000) return '¥' + (n / 10000).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' 万'
  return '¥' + formatMoney(n)
}

function loadAll() {
  getOverview().then(res => {
    overview.value = res.data
    const p = res.data.project || {}
    const d = res.data.deposit || {}
    tween('totalProjects', Number(p.totalProjects) || 0)
    tween('ongoing', Number(p.ongoing) || 0)
    tween('won', Number(p.won) || 0)
    tween('winRate', Number(p.winRate) || 0, 900)
    tween('outAmount', Number(d.outAmount) || 0, 1100)
    tween('outCount', Number(d.outCount) || 0)
    renderGauge(Number(p.winRate) || 0)
    renderDeposit(d)
  })
  getStatusDistribution().then(res => {
    statusRows.value = res.data || []
    renderStatus(statusRows.value)
    renderFunnel(statusRows.value)
  })
  getTrend().then(res => { renderTrend(res.data || []); renderAmount(res.data || []) })
  getFailReason().then(res => renderFail(res.data || []))
}

const STATUS_COLORS = ['#6366f1', '#3b82f6', '#06b6d4', '#8b5cf6', '#a855f7', '#ec4899', '#f59e0b', '#10b981', '#94a3b8', '#ef4444', '#f97316']

function statusName(v) { const d = bid_project_status.value.find(x => x.value == v); return d ? d.label : v }
function failName(v) { const d = bid_fail_reason.value.find(x => x.value == v); return d ? d.label : v }

function renderStatus(rows) {
  if (!statusChartRef.value) return
  statusChart = statusChart || echarts.init(statusChartRef.value)
  const total = rows.reduce((s, r) => s + Number(r.cnt || 0), 0)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>数量：{c}（{d}%）' },
    legend: { bottom: 0, type: 'scroll', textStyle: { fontSize: 11 }, pageIconSize: 9 },
    graphic: [{ type: 'text', left: 'center', top: '40%', style: { text: String(total), fontSize: 26, fontWeight: 700, fill: '#0f172a' } },
              { type: 'text', left: 'center', top: '52%', style: { text: '项目总数', fontSize: 12, fill: '#64748b' } }],
    animationDuration: 900,
    animationEasing: 'cubicOut',
    series: [{
      type: 'pie', radius: ['48%', '72%'], center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { scale: true, scaleSize: 8, label: { show: true, fontSize: 13, fontWeight: 600, formatter: '{b}\n{c}' } },
      data: rows.map((r, i) => ({ name: statusName(r.status), value: Number(r.cnt || 0), itemStyle: { color: STATUS_COLORS[i % STATUS_COLORS.length] } }))
    }]
  }, true)
  // 自动轮播高亮
  if (spinTimer) clearInterval(spinTimer)
  let idx = 0
  spinTimer = setInterval(() => {
    if (!statusChart || !rows.length) return
    statusChart.dispatchAction({ type: 'downplay', seriesIndex: 0 })
    statusChart.dispatchAction({ type: 'highlight', seriesIndex: 0, dataIndex: idx % rows.length })
    idx++
  }, 2200)
}

function renderTrend(rows) {
  if (!trendChartRef.value) return
  trendChart = trendChart || echarts.init(trendChartRef.value)
  const months = rows.map(r => r.month)
  const totals = rows.map(r => Number(r.total || 0))
  const wons = rows.map(r => Number(r.won || 0))
  const rates = rows.map((r, i) => totals[i] ? +(wons[i] / totals[i] * 100).toFixed(1) : 0)
  trendChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    grid: { left: 46, right: 46, top: 34, bottom: 46 },
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    xAxis: { type: 'category', data: months, axisLine: { lineStyle: { color: '#cbd5e1' } }, axisLabel: { fontSize: 11 } },
    yAxis: [
      { type: 'value', name: '数量', splitLine: { lineStyle: { color: '#f1f5f9' } } },
      { type: 'value', name: '中标率%', max: 100, axisLabel: { formatter: '{value}%' }, splitLine: { show: false } }
    ],
    series: [
      { name: '投标数', type: 'bar', barMaxWidth: 22, data: totals,
        itemStyle: { borderRadius: [5, 5, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#818cf8' }, { offset: 1, color: '#6366f1' }]) } },
      { name: '中标数', type: 'bar', barMaxWidth: 22, data: wons,
        itemStyle: { borderRadius: [5, 5, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#34d399' }, { offset: 1, color: '#10b981' }]) } },
      { name: '中标率', type: 'line', yAxisIndex: 1, smooth: true, data: rates, symbolSize: 7,
        lineStyle: { width: 3, color: '#f59e0b' }, itemStyle: { color: '#f59e0b', borderWidth: 2, borderColor: '#fff' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245,158,11,.22)' }, { offset: 1, color: 'rgba(245,158,11,0)' }]) } }
    ]
  }, true)
}

function renderFail(rows) {
  if (!failChartRef.value) return
  failChart = failChart || echarts.init(failChartRef.value)
  const palette = ['#ef4444', '#f97316', '#f59e0b', '#eab308', '#fb7185', '#f87171']
  failChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>数量：{c}（{d}%）' },
    legend: { bottom: 0, type: 'scroll', textStyle: { fontSize: 11 } },
    animationDuration: 900,
    animationEasing: 'elasticOut',
    series: [{
      type: 'pie', roseType: 'area', radius: ['18%', '68%'], center: ['50%', '44%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, fontSize: 11, formatter: '{b}' },
      data: rows.map((r, i) => ({ name: failName(r.failReason), value: Number(r.cnt || 0), itemStyle: { color: palette[i % palette.length] } }))
    }]
  }, true)
}

function renderFunnel(rows) {
  if (!funnelChartRef.value) return
  funnelChart = funnelChart || echarts.init(funnelChartRef.value)
  // 按状态码聚合：跟进中(0-4) → 已递交/开标(5-6) → 中标(7)；与 statusDistribution 返回的状态码一致
  const sumCodes = codes => rows.filter(r => codes.includes(String(r.status))).reduce((s, r) => s + Number(r.cnt || 0), 0)
  const stages = [
    { name: '全部项目', value: rows.reduce((s, r) => s + Number(r.cnt || 0), 0) },
    { name: '跟进中', value: sumCodes(['0', '1', '2', '3', '4']) },
    { name: '已递交/开标', value: sumCodes(['5', '6']) },
    { name: '中标', value: sumCodes(['7']) }
  ].map(s => ({ ...s, value: Math.max(s.value, 0) }))
  funnelChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>数量：{c}' },
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    series: [{
      type: 'funnel', left: '12%', right: '12%', top: 14, bottom: 8,
      minSize: '22%', sort: 'descending', gap: 4,
      label: { show: true, position: 'inside', fontSize: 12, formatter: '{b}: {c}' },
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      emphasis: { label: { fontSize: 14 } },
      data: stages.map((s, i) => ({ ...s, itemStyle: { color: STATUS_COLORS[[0, 2, 5, 6][i]] } }))
    }]
  }, true)
}

function renderGauge(winRate) {
  if (!gaugeChartRef.value) return
  gaugeChart = gaugeChart || echarts.init(gaugeChartRef.value)
  const pct = +(winRate * 100).toFixed(1)
  gaugeChart.setOption({
    series: [{
      type: 'gauge', startAngle: 210, endAngle: -30, min: 0, max: 100, radius: '92%',
      progress: { show: true, width: 14, roundCap: true,
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#6366f1' }, { offset: 1, color: '#10b981' }]) } },
      pointer: { show: false },
      axisLine: { roundCap: true, lineStyle: { width: 14, color: [[1, '#eef2f7']] } },
      axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
      title: { show: true, offsetCenter: [0, '32%'], fontSize: 12, color: '#64748b' },
      detail: { valueAnimation: true, offsetCenter: [0, '-4%'], fontSize: 30, fontWeight: 700, color: '#0f172a', formatter: '{value}%' },
      data: [{ value: pct, name: '历史中标率' }]
    }]
  }, true)
}

function renderDeposit(d) {
  if (!depositChartRef.value) return
  depositChart = depositChart || echarts.init(depositChartRef.value)
  const outAmount = Number(d.outAmount || 0)
  const outCount = Number(d.outCount || 0)
  depositChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>金额：￥{c}' },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    animationDuration: 900,
    animationEasing: 'cubicOut',
    series: [{
      type: 'pie', radius: ['52%', '74%'], center: ['50%', '44%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, fontSize: 11, formatter: '{b}\n￥{c}' },
      emphasis: { scale: true, scaleSize: 6 },
      data: [
        { name: '在外（未退回）', value: outAmount, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#fbbf24' }, { offset: 1, color: '#f59e0b' }]) } },
        { name: '已释放', value: Math.max(outAmount * 1.6 - outAmount, outAmount * 0.1), itemStyle: { color: '#e2e8f0' }, tooltip: { show: false } }
      ]
    }],
    graphic: [{ type: 'text', left: 'center', top: '38%', style: { text: String(outCount) + ' 笔', fontSize: 18, fontWeight: 700, fill: '#0f172a' } },
              { type: 'text', left: 'center', top: '48%', style: { text: '在外占用', fontSize: 11, fill: '#64748b' } }]
  }, true)
}

function renderAmount(rows) {
  if (!amountChartRef.value) return
  amountChart = amountChart || echarts.init(amountChartRef.value)
  const toWan = v => +(Number(v || 0) / 10000).toFixed(2)
  const months = rows.map(r => r.month)
  const bidAmounts = rows.map(r => toWan(r.bidAmount))
  const wonAmounts = rows.map(r => toWan(r.wonAmount))
  const rates = rows.map((r, i) => bidAmounts[i] ? +(wonAmounts[i] / bidAmounts[i] * 100).toFixed(1) : 0)
  amountChart.setOption({
    tooltip: {
      trigger: 'axis', axisPointer: { type: 'shadow' },
      valueFormatter: v => v == null ? '—' : '￥' + Number(v).toLocaleString('zh-CN') + ' 万'
    },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    grid: { left: 70, right: 60, top: 40, bottom: 46 },
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    xAxis: { type: 'category', data: months, axisLine: { lineStyle: { color: '#cbd5e1' } }, axisLabel: { fontSize: 11 } },
    yAxis: [
      { type: 'value', name: '金额（万元）', axisLabel: { formatter: v => Number(v).toLocaleString('zh-CN') }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
      { type: 'value', name: '金额中标率%', max: 100, axisLabel: { formatter: '{value}%' }, splitLine: { show: false } }
    ],
    series: [
      { name: '投标金额', type: 'bar', barMaxWidth: 26, data: bidAmounts,
        itemStyle: { borderRadius: [5, 5, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#818cf8' }, { offset: 1, color: '#4f46e5' }]) },
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(79,70,229,.35)' } } },
      { name: '中标金额', type: 'bar', barMaxWidth: 26, data: wonAmounts,
        itemStyle: { borderRadius: [5, 5, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#34d399' }, { offset: 1, color: '#059669' }]) },
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(5,150,105,.35)' } } },
      { name: '金额中标率', type: 'line', yAxisIndex: 1, smooth: true, data: rates, symbolSize: 7,
        lineStyle: { width: 3, color: '#f59e0b', type: 'dashed' }, itemStyle: { color: '#f59e0b', borderWidth: 2, borderColor: '#fff' } }
    ]
  }, true)
}

function handleResize() {
  [statusChart, trendChart, failChart, gaugeChart, funnelChart, depositChart, amountChart].forEach(c => c && c.resize())
}

watch(autoRefresh, v => {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (v) { refreshTimer = setInterval(loadAll, 30000) }
})

onMounted(() => { loadAll(); window.addEventListener('resize', handleResize) })
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) { clearInterval(refreshTimer) }
  if (spinTimer) { clearInterval(spinTimer) }
  ;[statusChart, trendChart, failChart, gaugeChart, funnelChart, depositChart, amountChart].forEach(c => c && c.dispose())
})
</script>

<style scoped>
.bid-dashboard-page {
  padding-top:10px;
  --brand-50:#eef2ff; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-700:#b45309; --blue-700:#1d4ed8; --green-700:#047857; --violet-700:#7c3aed; --cyan-700:#0e7490;
  --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);
  font-feature-settings:"tnum" 1; color:var(--ink-900);
}
.bid-dashboard-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:12px; }
.bid-dashboard-page .dash-toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; }
.bid-dashboard-page .dash-title { display:flex; align-items:center; gap:10px; }
.bid-dashboard-page .dash-title-text { font-size:16px; font-weight:700; }
.bid-dashboard-page .dash-sub { font-size:12px; color:var(--ink-400); }
.bid-dashboard-page .pulse-dot { width:9px; height:9px; border-radius:50%; background:#10b981; position:relative; }
.bid-dashboard-page .pulse-dot::after { content:''; position:absolute; inset:-4px; border-radius:50%; background:rgba(16,185,129,.35); animation:pulse 1.8s ease-out infinite; }
@keyframes pulse { 0% { transform:scale(.5); opacity:1; } 100% { transform:scale(1.6); opacity:0; } }
.bid-dashboard-page .toolbar-right { display:flex; align-items:center; gap:12px; }

/* KPI 卡入场动画 + 悬浮特效 */
@keyframes cardIn { from { opacity:0; transform:translateY(14px); } to { opacity:1; transform:translateY(0); } }
@keyframes chartIn { from { opacity:0; transform:translateY(18px); } to { opacity:1; transform:translateY(0); } }
.bid-dashboard-page .kpi-card { padding:16px; display:flex; align-items:center; gap:12px; position:relative; transition:transform .25s ease, box-shadow .25s ease; border-left:4px solid transparent; animation:cardIn .5s cubic-bezier(.22,.9,.35,1) both; }
.bid-dashboard-page .kpi-card:hover { transform:translateY(-4px); box-shadow:0 10px 22px rgba(15,23,42,.10); }
.bid-dashboard-page .kpi-icon { width:42px; height:42px; border-radius:12px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.bid-dashboard-page .kpi-value { font-size:20px; font-weight:700; margin-top:3px; letter-spacing:-.3px; }
.bid-dashboard-page .kpi-foot { font-size:11px; color:var(--ink-400); margin-top:2px; }
.bid-dashboard-page .kpi-row { margin-bottom:0; }
.bid-dashboard-page .c-blue .kpi-icon { background:#eff6ff; color:#3b82f6; }
.bid-dashboard-page .c-blue { border-left-color:#3b82f6; } .bid-dashboard-page .c-blue .kpi-value { color:var(--blue-700); }
.bid-dashboard-page .c-violet .kpi-icon { background:#f5f3ff; color:#8b5cf6; }
.bid-dashboard-page .c-violet { border-left-color:#8b5cf6; } .bid-dashboard-page .c-violet .kpi-value { color:var(--violet-700); }
.bid-dashboard-page .c-green .kpi-icon { background:#ecfdf5; color:#10b981; }
.bid-dashboard-page .c-green { border-left-color:#10b981; } .bid-dashboard-page .c-green .kpi-value { color:var(--green-700); }
.bid-dashboard-page .c-brand .kpi-icon { background:#eef2ff; color:#6366f1; }
.bid-dashboard-page .c-brand { border-left-color:#6366f1; } .bid-dashboard-page .c-brand .kpi-value { color:var(--brand-700); }
.bid-dashboard-page .c-amber .kpi-icon { background:#fffbeb; color:#f59e0b; }
.bid-dashboard-page .c-amber { border-left-color:#f59e0b; } .bid-dashboard-page .c-amber .kpi-value { color:var(--amber-700); }
.bid-dashboard-page .c-cyan .kpi-icon { background:#ecfeff; color:#06b6d4; }
.bid-dashboard-page .c-cyan { border-left-color:#06b6d4; } .bid-dashboard-page .c-cyan .kpi-value { color:var(--cyan-700); }

/* 图表卡入场动画 */
.bid-dashboard-page .chart-card { padding:14px 16px; animation:chartIn .55s cubic-bezier(.22,.9,.35,1) both; transition:box-shadow .25s ease; }
.bid-dashboard-page .chart-card:hover { box-shadow:0 8px 20px rgba(15,23,42,.08); }
.bid-dashboard-page .chart-title { font-size:14px; font-weight:600; margin-bottom:8px; display:flex; align-items:center; gap:8px; }
.bid-dashboard-page .chart-title .bar { width:4px; height:14px; border-radius:2px; background:linear-gradient(180deg,#6366f1,#10b981); }
.bid-dashboard-page .chart-box { height:290px; width:100%; }
.bid-dashboard-page .chart-box--wide { height:320px; }
</style>
