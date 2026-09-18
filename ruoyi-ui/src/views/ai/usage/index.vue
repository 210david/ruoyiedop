<template>
  <div class="app-container ai-usage" v-loading="loading">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon">📊</span>
        <span>AI 用量统计看板</span>
        <el-tag size="small" effect="plain" class="range-tag">近 {{ days }} 天</el-tag>
      </div>
      <div class="header-actions">
        <el-switch v-model="autoRefresh" active-text="自动刷新" size="small" class="auto-switch" />
        <el-button :icon="Refresh" circle size="small" @click="load" title="刷新" />
        <el-radio-group v-model="days" size="small" @change="load">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
          <el-radio-button :value="90">近90天</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 异常预警横幅 -->
    <div v-if="alerts.length" class="alert-area">
      <transition-group name="alert-slide">
        <div v-for="(a, i) in alerts" :key="i" class="alert-item" :class="'alert-' + a.level">
          <span class="alert-dot"></span>
          <span class="alert-title">【{{ a.title }}】</span>
          <span class="alert-msg">{{ a.message }}</span>
        </div>
      </transition-group>
    </div>
    <div v-else class="alert-area alert-ok">
      <span class="alert-dot ok"></span> 服务运行正常，近 {{ days }} 天未检测到异常
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi-blue">
          <div class="kpi-icon">📞</div>
          <div class="kpi-body">
            <div class="kpi-label">本月已调用</div>
            <div class="kpi-value">{{ animated.monthlyUsed }}</div>
            <div class="kpi-sub">限额 {{ monthlyLimit > 0 ? monthlyLimit + ' 次/月' : '不限' }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi-cyan">
          <div class="kpi-icon">📈</div>
          <div class="kpi-body">
            <div class="kpi-label">近{{ days }}天总调用</div>
            <div class="kpi-value">{{ animated.total }}</div>
            <div class="kpi-sub">平均耗时 {{ total.avgDuration || 0 }}ms</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi-green">
          <div class="kpi-icon">✅</div>
          <div class="kpi-body">
            <div class="kpi-label">成功率</div>
            <div class="kpi-value ok">{{ animated.successRate }}%</div>
            <div class="kpi-sub">成功 {{ total.success || 0 }} / 降级 {{ total.degraded || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="kpi-card kpi-red">
          <div class="kpi-icon">⚠️</div>
          <div class="kpi-body">
            <div class="kpi-label">失败 / 限额拦截</div>
            <div class="kpi-value err">{{ animated.failed }} <span class="sep">/</span> {{ animated.limited }}</div>
            <div class="kpi-sub">失败较多请检查AI配置</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 调用趋势 -->
    <el-card shadow="never" class="chart-card">
      <template #header><span class="card-title">📅 调用趋势（按日期 / 分模块）</span></template>
      <div ref="trendChart" class="chart chart-lg" />
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">🧩 模块用量占比</span></template>
          <div ref="moduleChart" class="chart chart-md" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">🚦 调用状态分布</span></template>
          <div ref="statusChart" class="chart chart-md" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">⏰ 使用时段分布（24小时）</span></template>
          <div ref="hourChart" class="chart chart-md" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">🏆 功能调用排行（TOP 20）</span></template>
          <div ref="featureChart" class="chart chart-md" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">👥 人员使用排行（TOP 15）</span></template>
          <div ref="userChart" class="chart chart-md" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="AiUsage">
import { getAiUsage } from '@/api/ai/center'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'

const loading = ref(true)
const days = ref(30)
const autoRefresh = ref(false)
const total = ref({})
const moduleStat = ref([])
const featureStat = ref([])
const dailyTrend = ref([])
const userStat = ref([])
const hourStat = ref([])
const statusStat = ref([])
const moduleDaily = ref([])
const alerts = ref([])
const monthlyUsed = ref(0)
const monthlyLimit = ref(0)

const trendChart = ref(null)
const moduleChart = ref(null)
const statusChart = ref(null)
const hourChart = ref(null)
const featureChart = ref(null)
const userChart = ref(null)

const chartInstances = {}
let autoTimer = null

const moduleNames = {
  dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产', platform: '平台'
}

// 功能标识 → 中文说明（与后端 AiClient 各业务调用点的 feature 保持一致）
const featureNames = {
  repair_assistant: '智能报修解析',
  dispatch_recommend: '智能派工推荐',
  knowledge_qa: '知识库问答',
  faq_generate: 'FAQ知识沉淀',
  complete_report: '完工报告生成',
  equipment_health: '设备健康预测',
  workorder_agent: '工单智能助手',
  ncr_advice: '不合格品处置建议',
  report_draft: '质量报告初稿',
  abnormal_advice: '生产异常诊断',
  daily_report: '生产日报生成',
  request_advice: '采购申请建议',
  price_summary: '比价分析摘要',
  attendance_summary: '考勤汇总分析',
  customer_churn_report: '客户流失分析报告',
  sales_weekly_report: '销售周报生成',
  churn_prediction: '客户流失预测',
  hazard_advice: '安全隐患处置建议',
  training_quiz: '安全培训测验',
  stock_analysis: '库存分析',
  stocktake_analysis: '盘点差异分析',
  nl2query: '智能查询（自然语言查数据）',
  tool_route: '智能工具路由'
}

const statusNames = { '0': '成功', '1': '降级', '2': '失败', '3': '限额拦截' }
const statusColors = { '0': '#10b981', '1': '#f59e0b', '2': '#ef4444', '3': '#8b5cf6' }
const palette = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#06b6d4', '#6366f1']

// ===== KPI 数字滚动动画 =====
const animated = reactive({ monthlyUsed: 0, total: 0, successRate: 0, failed: 0, limited: 0 })
let animTimer = null

function easeOut(t) { return 1 - Math.pow(1 - t, 3) }

function animateNumber(key, to)
{
  const from = Number(animated[key]) || 0
  const steps = 24
  let step = 0
  const tick = () => {
    step++
    const p = easeOut(step / steps)
    animated[key] = Math.round(from + (to - from) * p)
    if (step < steps) animTimer = setTimeout(tick, 25)
  }
  tick()
}

function updateAnimated()
{
  const t = total.value || {}
  const tot = Number(t.total) || 0
  const success = Number(t.success) || 0
  animateNumber('monthlyUsed', Number(monthlyUsed.value) || 0)
  animateNumber('total', tot)
  animateNumber('successRate', tot > 0 ? Math.round(success * 100 / tot) : 0)
  animateNumber('failed', Number(t.failed) || 0)
  animateNumber('limited', Number(t.limited) || 0)
}

// ===== 数据加载 =====
function load()
{
  loading.value = true
  getAiUsage(days.value).then(res => {
    const d = res.data || {}
    total.value = d.total || {}
    moduleStat.value = d.moduleStat || []
    featureStat.value = d.featureStat || []
    dailyTrend.value = d.dailyTrend || []
    userStat.value = d.userStat || []
    hourStat.value = d.hourStat || []
    statusStat.value = d.statusStat || []
    moduleDaily.value = d.moduleDaily || []
    alerts.value = d.alerts || []
    monthlyUsed.value = d.monthlyUsed || 0
    monthlyLimit.value = d.monthlyLimit || 0
    loading.value = false
    updateAnimated()
    nextTick(renderAll)
  }).catch(() => { loading.value = false })
}

// ===== 图表渲染 =====
function initChart(key, el)
{
  if (chartInstances[key]) { chartInstances[key].dispose() }
  chartInstances[key] = echarts.init(el)
  return chartInstances[key]
}

function renderAll()
{
  renderTrend()
  renderModule()
  renderStatus()
  renderHour()
  renderFeature()
  renderUser()
}

/** 趋势：模块堆叠面积 + 失败数柱 + 平均耗时折线（双Y轴） */
function renderTrend()
{
  if (!trendChart.value) return
  const chart = initChart('trend', trendChart.value)
  // 补全日期序列
  const dates = []
  const now = new Date()
  for (let i = days.value - 1; i >= 0; i--)
  {
    const d = new Date(now.getTime() - i * 86400000)
    dates.push(d.toISOString().slice(0, 10))
  }
  const trendMap = {}
  dailyTrend.value.forEach(d => { trendMap[d.date] = d })
  // 模块×日期矩阵
  const modules = [...new Set(moduleDaily.value.map(d => d.module))]
  const mdMap = {}
  moduleDaily.value.forEach(d => {
    if (!mdMap[d.module]) mdMap[d.module] = {}
    mdMap[d.module][d.date] = Number(d.total) || 0
  })
  const series = modules.map((m, i) => ({
    name: moduleNames[m] || m,
    type: 'line',
    stack: 'total',
    smooth: true,
    symbol: 'none',
    lineStyle: { width: 0 },
    areaStyle: { opacity: 0.85 },
    itemStyle: { color: palette[i % palette.length] },
    emphasis: { focus: 'series' },
    data: dates.map(dt => mdMap[m] ? (mdMap[m][dt] || 0) : 0)
  }))
  series.push({
    name: '平均耗时',
    type: 'line',
    yAxisIndex: 1,
    smooth: true,
    symbol: 'circle',
    symbolSize: 5,
    itemStyle: { color: '#f97316' },
    lineStyle: { width: 2.5, type: 'dashed' },
    data: dates.map(dt => trendMap[dt] ? (Number(trendMap[dt].avgDuration) || 0) : 0)
  })
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { top: 0, type: 'scroll' },
    grid: { left: 50, right: 55, top: 40, bottom: 30 },
    xAxis: { type: 'category', boundaryGap: false, data: dates.map(d => d.slice(5)) },
    yAxis: [
      { type: 'value', name: '调用量', splitLine: { lineStyle: { type: 'dashed' } } },
      { type: 'value', name: '耗时(ms)', splitLine: { show: false }, axisLabel: { formatter: '{value}' } }
    ],
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    series
  }, true)
}

/** 模块占比：玫瑰环形图 */
function renderModule()
{
  if (!moduleChart.value) return
  const chart = initChart('module', moduleChart.value)
  const data = moduleStat.value.map(m => ({
    name: moduleNames[m.module] || m.module,
    value: Number(m.total) || 0,
    failed: Number(m.failed) || 0
  }))
  chart.setOption({
    tooltip: { trigger: 'item', formatter: p => `${p.name}<br/>调用 ${p.value} 次（${p.percent}%）` },
    legend: { orient: 'vertical', right: 5, top: 'center', type: 'scroll' },
    color: palette,
    series: [{
      type: 'pie',
      radius: ['35%', '68%'],
      center: ['40%', '52%'],
      roseType: 'radius',
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', formatter: '{b}\n{c} 次' },
        itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0,0,0,0.25)' }
      },
      animationType: 'scale',
      animationEasing: 'elasticOut',
      data
    }]
  }, true)
}

/** 状态分布：环形 + 中心成功率 */
function renderStatus()
{
  if (!statusChart.value) return
  const chart = initChart('status', statusChart.value)
  const data = statusStat.value.map(s => ({
    name: statusNames[String(s.status)] || s.status,
    value: Number(s.total) || 0,
    itemStyle: { color: statusColors[String(s.status)] || '#999' }
  }))
  const tot = data.reduce((a, b) => a + b.value, 0)
  const ok = (statusStat.value.find(s => String(s.status) === '0') || {}).total || 0
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}：{c} 次（{d}%）' },
    series: [
      {
        type: 'pie',
        radius: ['52%', '72%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { scale: true, scaleSize: 8 },
        animationType: 'expansion',
        data
      },
      {
        type: 'pie',
        radius: [0, 0],
        center: ['50%', '50%'],
        label: {
          show: true,
          position: 'center',
          formatter: () => tot > 0 ? `${Math.round(ok * 100 / tot)}%\n成功率` : '暂无数据',
          fontSize: 22,
          fontWeight: 'bold',
          color: '#10b981',
          lineHeight: 30
        },
        silent: true,
        data: [{ value: 0 }]
      }
    ]
  }, true)
}

/** 时段分布：24小时渐变柱状 */
function renderHour()
{
  if (!hourChart.value) return
  const chart = initChart('hour', hourChart.value)
  const hourMap = {}
  hourStat.value.forEach(h => { hourMap[Number(h.hour)] = h })
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const values = hours.map(h => hourMap[h] ? (Number(hourMap[h].total) || 0) : 0)
  const maxV = Math.max(...values, 1)
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: ps => `${ps[0].name}:00 — 调用 ${ps[0].value} 次` },
    grid: { left: 45, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: hours.map(h => String(h)), name: '时' },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{
      type: 'bar',
      data: values.map(v => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: 'rgba(59,130,246,0.15)' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      emphasis: { itemStyle: { color: '#1d4ed8' } },
      barMaxWidth: 18,
      animationDelay: i => i * 30
    }],
    animationDuration: 800,
    animationEasing: 'elasticOut'
  }, true)
}

/** 功能 TOP20：横向渐变条形 */
function renderFeature()
{
  if (!featureChart.value) return
  const chart = initChart('feature', featureChart.value)
  const list = featureStat.value.slice(0, 20).map(f => ({
    name: `${moduleNames[f.module] || f.module} · ${featureNames[f.feature] || f.feature}`,
    module: moduleNames[f.module] || f.module,
    feature: featureNames[f.feature] || f.feature,
    value: Number(f.total) || 0,
    avg: Number(f.avgDuration) || 0
  })).reverse()
  chart.setOption({
    tooltip: { trigger: 'item', formatter: p => `${p.data.module} · ${p.data.feature}<br/>调用 ${p.value} 次，平均 ${p.data.avg}ms` },
    grid: { left: 10, right: 50, top: 10, bottom: 10, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    yAxis: { type: 'category', data: list.map(d => d.name.length > 18 ? d.name.slice(0, 17) + '…' : d.name) },
    series: [{
      type: 'bar',
      data: list.map(d => ({
        ...d,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#06b6d4' },
            { offset: 1, color: '#3b82f6' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      })),
      label: { show: true, position: 'right', formatter: '{c}', color: '#64748b' },
      barMaxWidth: 16,
      animationDelay: i => i * 60
    }],
    animationDuration: 900,
    animationEasing: 'cubicOut'
  }, true)
}

/** 人员 TOP15：横向条形（成功绿+失败红堆叠） */
function renderUser()
{
  if (!userChart.value) return
  const chart = initChart('user', userChart.value)
  const list = userStat.value.map(u => ({
    name: u.userName,
    total: Number(u.total) || 0,
    success: (Number(u.total) || 0) - (Number(u.failed) || 0),
    failed: Number(u.failed) || 0,
    avg: Number(u.avgDuration) || 0
  })).reverse()
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: ps => {
        const d = ps[0].data.origin
        return `${d.name}<br/>共 ${d.total} 次（成功 ${d.success} / 失败 ${d.failed}）<br/>平均耗时 ${d.avg}ms`
      }
    },
    legend: { top: 0, data: ['成功/降级', '失败'] },
    grid: { left: 10, right: 40, top: 30, bottom: 10, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    yAxis: { type: 'category', data: list.map(d => d.name) },
    series: [
      {
        name: '成功/降级',
        type: 'bar',
        stack: 'total',
        data: list.map(d => ({ value: d.success, origin: d })),
        itemStyle: { color: '#10b981', borderRadius: [0, 0, 0, 0] },
        barMaxWidth: 16,
        animationDelay: i => i * 60
      },
      {
        name: '失败',
        type: 'bar',
        stack: 'total',
        data: list.map(d => ({ value: d.failed, origin: d })),
        itemStyle: { color: '#ef4444', borderRadius: [0, 4, 4, 0] },
        barMaxWidth: 16,
        animationDelay: i => i * 60 + 150
      }
    ],
    animationDuration: 900
  }, true)
}

// ===== 自动刷新 =====
watch(autoRefresh, v => {
  if (v) { autoTimer = setInterval(load, 60000) }
  else if (autoTimer) { clearInterval(autoTimer); autoTimer = null }
})

// ===== 窗口自适应 =====
const resizeHandler = () => Object.values(chartInstances).forEach(c => c && c.resize())
window.addEventListener('resize', resizeHandler)

// ===== 生命周期 =====
onMounted(() => { load() })
onBeforeUnmount(() => {
  Object.values(chartInstances).forEach(c => c && c.dispose())
  if (autoTimer) clearInterval(autoTimer)
  if (animTimer) clearTimeout(animTimer)
  window.removeEventListener('resize', resizeHandler)
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 14px; }
.page-title { font-size: 18px; font-weight: 700; color: #1e293b; display: flex; align-items: center; gap: 8px; }
.title-icon { font-size: 22px; }
.range-tag { margin-left: 4px; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.auto-switch { margin-right: 2px; }

/* ===== 预警横幅 ===== */
.alert-area { margin-bottom: 14px; display: flex; flex-direction: column; gap: 8px; }
.alert-ok { font-size: 13px; color: #10b981; background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; padding: 9px 14px; display: flex; align-items: center; gap: 8px; }
.alert-item { display: flex; align-items: baseline; gap: 6px; padding: 9px 14px; border-radius: 8px; font-size: 13px; line-height: 1.6; }
.alert-error { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; }
.alert-warning { background: #fffbeb; border: 1px solid #fde68a; color: #b45309; }
.alert-info { background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8; }
.alert-title { font-weight: 700; flex-shrink: 0; }
.alert-dot { width: 8px; height: 8px; border-radius: 50%; background: currentColor; flex-shrink: 0; align-self: center; animation: alertPulse 1.5s infinite; }
.alert-dot.ok { animation: none; }
.alert-slide-enter-active { transition: all 0.5s ease; }
.alert-slide-enter-from { opacity: 0; transform: translateX(-24px); }
@keyframes alertPulse { 0%,100% { box-shadow: 0 0 0 0 rgba(239,68,68,0.5); } 50% { box-shadow: 0 0 0 6px rgba(239,68,68,0); } }

/* ===== KPI 卡片 ===== */
.stat-row { margin-bottom: 14px; }
.kpi-card { display: flex; align-items: center; gap: 14px; padding: 18px 20px; border-radius: 10px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.06); border: 1px solid #f1f5f9; position: relative; overflow: hidden; transition: transform 0.25s ease, box-shadow 0.25s ease; }
.kpi-card:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }
.kpi-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; }
.kpi-blue::before { background: #3b82f6; } .kpi-cyan::before { background: #06b6d4; }
.kpi-green::before { background: #10b981; } .kpi-red::before { background: #ef4444; }
.kpi-icon { font-size: 30px; flex-shrink: 0; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.15)); }
.kpi-label { font-size: 13px; color: #64748b; margin-bottom: 6px; }
.kpi-value { font-size: 30px; font-weight: 800; color: #1e293b; line-height: 1.1; font-variant-numeric: tabular-nums; }
.kpi-value.ok { color: #059669; }
.kpi-value.err { color: #dc2626; }
.kpi-value .sep { color: #cbd5e1; font-weight: 400; }
.kpi-sub { font-size: 12px; color: #94a3b8; margin-top: 6px; }

/* ===== 图表 ===== */
.chart-card { margin-bottom: 14px; border-radius: 10px; }
.chart-card :deep(.el-card__header) { padding: 12px 18px; }
.card-title { font-size: 14px; font-weight: 600; color: #334155; }
.chart { width: 100%; }
.chart-lg { height: 340px; }
.chart-md { height: 300px; }
</style>
