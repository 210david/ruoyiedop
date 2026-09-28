<template>
  <div class="app-container promo-dash">
    <!-- 顶部渐变 Hero 区 -->
    <div class="hero">
      <div class="hero__blob hero__blob--1"></div>
      <div class="hero__blob hero__blob--2"></div>
      <div class="hero__blob hero__blob--3"></div>
      <div class="hero__inner">
        <div class="hero__info">
          <div class="hero__title">推广效果看板</div>
          <div class="hero__desc">
            渠道推广效果 · 内容传播分析 · 活动归因闭环
            <span class="hero__range">{{ dateRange[0] }} ~ {{ dateRange[1] }}</span>
          </div>
        </div>
        <div class="hero__actions">
          <div class="quick-btns">
            <span v-for="q in quickRanges" :key="q.days" class="quick-btn" :class="{ 'is-active': activeQuick === q.days }" @click="handleQuick(q.days)">{{ q.label }}</span>
          </div>
          <div class="hero__toolbar">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" :clearable="false" size="default" style="width: 240px" @change="handleQuery" />
            <el-button type="primary" icon="Refresh" size="default" round @click="handleRefresh" :loading="loading">刷新数据</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-loading="loading" v-hasPermi="['marketing:promotionReport:view']">
      <!-- KPI 指标卡 -->
      <el-row :gutter="14" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="6" :lg="6" v-for="(kpi, idx) in kpiCards" :key="kpi.key">
          <div class="kpi-card" :style="{ '--i': idx, '--c1': kpi.c1, '--c2': kpi.c2 }">
            <div class="kpi-card__icon"><el-icon :size="24"><component :is="kpi.icon" /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ formatNum(kpiDisplay[kpi.key]) }}</div>
              <div class="kpi-card__label">{{ kpi.label }}</div>
              <div class="kpi-card__chip" v-if="kpiChip(kpi.key)">{{ kpiChip(kpi.key) }}</div>
            </div>
            <div class="kpi-card__glow"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 第二行：趋势图 + 渠道类型分布 -->
      <el-row :gutter="14" class="chart-row">
        <el-col :xs="24" :lg="16">
          <div class="chart-panel" style="--i: 8">
            <div class="chart-panel__head">
              <div>
                <span class="chart-panel__title">推广数据趋势</span>
                <span class="chart-panel__desc">按日统计曝光 / 阅读 / 点赞 / 评论</span>
              </div>
            </div>
            <div ref="trendChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel" style="--i: 9">
            <div class="chart-panel__head">
              <div>
                <span class="chart-panel__title">渠道类型分布</span>
                <span class="chart-panel__desc">曝光量占比</span>
              </div>
            </div>
            <div ref="pieChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 第三行：渠道对比 + 归因漏斗 -->
      <el-row :gutter="14" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel" style="--i: 10">
            <div class="chart-panel__head">
              <div>
                <span class="chart-panel__title">渠道效果对比</span>
                <span class="chart-panel__desc">曝光 / 阅读看左轴，点赞 / 线索看右轴</span>
              </div>
            </div>
            <div ref="channelChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel" style="--i: 11">
            <div class="chart-panel__head">
              <div>
                <span class="chart-panel__title">活动归因漏斗</span>
                <span class="chart-panel__desc">渠道 → 点击 → 报名 → 线索 → 商机 → 成交</span>
              </div>
              <el-select v-model="activityId" placeholder="选择活动" filterable clearable size="small" style="width: 200px" @change="loadFunnel">
                <el-option v-for="a in activityList" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
              </el-select>
            </div>
            <div ref="funnelChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 第四行：内容包效果排行 TOP10 -->
      <el-row :gutter="14" class="chart-row">
        <el-col :span="24">
          <div class="chart-panel" style="--i: 12">
            <div class="chart-panel__head">
              <div>
                <span class="chart-panel__title">内容包效果排行 TOP10</span>
                <span class="chart-panel__desc">按阅读量排名，含互动率分析</span>
              </div>
            </div>
            <div class="chart-panel__body chart-panel__body--table">
              <el-table :data="rankList" size="default" class="rank-table" :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: 600 }">
                <el-table-column label="排名" width="70" align="center">
                  <template #default="scope"><span class="rank-num" :class="'rank-' + (scope.$index + 1)">{{ scope.$index + 1 }}</span></template>
                </el-table-column>
                <el-table-column label="内容包标题" prop="packageTitle" min-width="240" align="center" show-overflow-tooltip />
                <el-table-column label="所属内容" prop="contentTitle" min-width="180" align="center" show-overflow-tooltip />
                <el-table-column label="渠道类型" prop="channelType" width="110" align="center">
                  <template #default="scope">
                    <span class="channel-tag" :class="'channel-tag--' + scope.row.channelType">{{ dictLabel(marketing_channel_type, scope.row.channelType) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" prop="packageStatus" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.packageStatus === '1' ? 'success' : 'info'" size="small" effect="light">{{ dictLabel(marketing_package_status, scope.row.packageStatus) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="曝光量" prop="exposureCount" width="110" align="center">
                  <template #default="scope">{{ formatNum(scope.row.exposureCount) }}</template>
                </el-table-column>
                <el-table-column label="阅读量" prop="readCount" min-width="180" align="center">
                  <template #default="scope">
                    <div class="read-bar-wrap">
                      <div class="read-bar"><div class="read-bar__fill" :style="{ width: readPercent(scope.row) + '%' }"></div></div>
                      <span class="read-bar__num">{{ formatNum(scope.row.readCount) }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="点赞量" prop="likeCount" width="100" align="center">
                  <template #default="scope">{{ formatNum(scope.row.likeCount) }}</template>
                </el-table-column>
                <el-table-column label="评论量" prop="commentCount" width="100" align="center">
                  <template #default="scope">{{ formatNum(scope.row.commentCount) }}</template>
                </el-table-column>
                <el-table-column label="互动率" width="100" align="center">
                  <template #default="scope">
                    <span class="interact-rate">{{ interactRate(scope.row) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup name="MkPromotionReport">
import * as echarts from 'echarts'
import { Connection, Document, Pointer, User, TrendCharts, View, Reading, Star } from '@element-plus/icons-vue'
import { getOverview, getChannelCompare, getContentRank, getFunnel, getTrend } from '@/api/mk/promotionReport'
import { listActivity } from '@/api/mk/activity'

const { proxy } = getCurrentInstance()
const { marketing_channel_type, marketing_package_status } = proxy.useDict('marketing_channel_type', 'marketing_package_status')

const loading = ref(true)
const overview = ref({})
const channelCompareData = ref([])
const rankList = ref([])
const trendData = ref([])
const activityList = ref([])
const activityId = ref(undefined)

/** 快捷日期区间 */
const quickRanges = [
  { label: '近7天', days: 7 },
  { label: '近30天', days: 30 },
  { label: '近90天', days: 90 }
]
const activeQuick = ref(30)

function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
function rangeOfDays(days) {
  const end = new Date()
  const begin = new Date()
  begin.setDate(begin.getDate() - (days - 1))
  return [formatDate(begin), formatDate(end)]
}
const dateRange = ref(rangeOfDays(30))

function handleQuick(days) {
  activeQuick.value = days
  dateRange.value = rangeOfDays(days)
  loadData()
}

/** KPI 卡片定义 */
const kpiCards = [
  { key: 'exposureCount', label: '总曝光', icon: View, c1: '#6366f1', c2: '#8b5cf6' },
  { key: 'readCount', label: '总阅读', icon: Reading, c1: '#06b6d4', c2: '#0ea5e9' },
  { key: 'clickCount', label: '总点击', icon: Pointer, c1: '#3b82f6', c2: '#2563eb' },
  { key: 'signupCount', label: '总报名', icon: User, c1: '#10b981', c2: '#059669' },
  { key: 'leadCount', label: '总线索', icon: TrendCharts, c1: '#f59e0b', c2: '#d97706' },
  { key: 'interactCount', label: '互动总量', icon: Star, c1: '#ec4899', c2: '#db2777' },
  { key: 'channelCount', label: '启用渠道', icon: Connection, c1: '#8b5cf6', c2: '#7c3aed' },
  { key: 'publishedCount', label: '已发布内容包', icon: Document, c1: '#ef4444', c2: '#dc2626' }
]

/** KPI 数字滚动动画 */
const kpiDisplay = reactive({
  exposureCount: 0, readCount: 0, clickCount: 0, signupCount: 0,
  leadCount: 0, interactCount: 0, channelCount: 0, publishedCount: 0
})
function animateKpi(target) {
  const duration = 1100
  Object.keys(kpiDisplay).forEach(key => {
    const from = Number(kpiDisplay[key]) || 0
    const to = Number(target[key]) || 0
    if (from === to) { kpiDisplay[key] = to; return }
    const startT = performance.now()
    const step = now => {
      const p = Math.min((now - startT) / duration, 1)
      const eased = 1 - Math.pow(1 - p, 3)
      kpiDisplay[key] = Math.round(from + (to - from) * eased)
      if (p < 1) requestAnimationFrame(step)
    }
    requestAnimationFrame(step)
  })
}

/** KPI 副指标（转化率等） */
function kpiChip(key) {
  const ov = overview.value || {}
  const num = v => Number(v || 0)
  const rate = (a, b) => (b > 0 ? ((a / b) * 100).toFixed(1) + '%' : '-')
  switch (key) {
    case 'exposureCount': return '阅读率 ' + rate(num(ov.readCount), num(ov.exposureCount))
    case 'readCount': return '互动率 ' + rate(num(ov.likeCount) + num(ov.commentCount), num(ov.readCount))
    case 'signupCount': return '点击→报名 ' + rate(num(ov.signupCount), num(ov.clickCount))
    case 'leadCount': return '报名→线索 ' + rate(num(ov.leadCount), num(ov.signupCount))
    case 'interactCount': return `赞 ${formatNum(ov.likeCount)} · 评 ${formatNum(ov.commentCount)}`
    case 'channelCount': return `内容包 ${formatNum(ov.publishedCount)} 个`
    case 'publishedCount': return `覆盖平台 ${channelCompareData.value.length} 类`
    default: return ''
  }
}

// 图表引用
const trendChart = ref(null)
const pieChart = ref(null)
const channelChart = ref(null)
const funnelChart = ref(null)

let trendInstance = null
let pieInstance = null
let channelInstance = null
let funnelInstance = null

const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#64748b')
const chartSplitColor = computed(() => isDark.value ? '#3a3a3a' : '#eef2f7')

/** 数值格式化（千分位） */
function formatNum(val) {
  if (val == null || val === '') return 0
  return Number(val).toLocaleString('zh-CN')
}

/** 字典标签转换 */
function dictLabel(dictOptions, code) {
  if (!code && code !== '0') return code || '-'
  const item = dictOptions.value?.find(d => d.value == code)
  return item ? item.label : code
}

/** 渐变色构造 */
function grad(c1, c2, horizontal = false) {
  return new echarts.graphic.LinearGradient(0, 0, horizontal ? 1 : 0, horizontal ? 0 : 1, [{ offset: 0, color: c1 }, { offset: 1, color: c2 }])
}

/** 推广趋势面积图 */
function renderTrend(data) {
  if (!trendChart.value) return
  const dates = data.map(d => d.dataDate)
  const mk = key => data.map(d => Number(d[key] || 0))
  if (trendInstance) trendInstance.dispose()
  trendInstance = echarts.init(trendChart.value)
  trendInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross', crossStyle: { color: '#999' } } },
    legend: { data: ['曝光量', '阅读量', '点赞量', '评论量'], top: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '2%', right: '2%', bottom: '3%', top: '14%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false, axisLabel: { color: chartTextColor.value }, axisLine: { lineStyle: { color: chartSplitColor.value } } },
    yAxis: [
      { type: 'value', name: '曝光/阅读', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '点赞/评论', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    series: [
      { name: '曝光量', type: 'line', smooth: true, symbol: 'circle', symbolSize: 7, showSymbol: false, data: mk('exposureCount'),
        lineStyle: { width: 3, color: '#6366f1' }, itemStyle: { color: '#6366f1' },
        areaStyle: { color: grad('rgba(99,102,241,.28)', 'rgba(99,102,241,0)', false) } },
      { name: '阅读量', type: 'line', smooth: true, symbol: 'circle', symbolSize: 7, showSymbol: false, data: mk('readCount'),
        lineStyle: { width: 3, color: '#06b6d4' }, itemStyle: { color: '#06b6d4' },
        areaStyle: { color: grad('rgba(6,182,212,.24)', 'rgba(6,182,212,0)', false) } },
      { name: '点赞量', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, showSymbol: false, yAxisIndex: 1, data: mk('likeCount'),
        lineStyle: { width: 2.5, color: '#f59e0b', type: 'solid' }, itemStyle: { color: '#f59e0b' } },
      { name: '评论量', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, showSymbol: false, yAxisIndex: 1, data: mk('commentCount'),
        lineStyle: { width: 2.5, color: '#ec4899' }, itemStyle: { color: '#ec4899' } }
    ]
  })
}

/** 渠道类型分布玫瑰图 */
function renderPie(data) {
  if (!pieChart.value) return
  const pieList = data.filter(d => Number(d.exposureCount) > 0)
  if (pieInstance) pieInstance.dispose()
  pieInstance = echarts.init(pieChart.value)
  pieInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: p => {
        const row = pieList[p.dataIndex] || {}
        return `${p.name}<br/>曝光 ${formatNum(row.exposureCount)}（${p.percent}%）<br/>内容包 ${formatNum(row.packageCount)} 个 · 阅读 ${formatNum(row.readCount)}`
      }
    },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    color: ['#6366f1', '#06b6d4', '#f59e0b', '#ec4899', '#10b981', '#8b5cf6'],
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    series: [{
      type: 'pie',
      radius: ['42%', '72%'],
      center: ['50%', '46%'],
      roseType: 'radius',
      itemStyle: { borderRadius: 8, borderColor: isDark.value ? '#263445' : '#fff', borderWidth: 2 },
      label: { color: chartTextColor.value, formatter: '{b}\n{d}%' },
      labelLine: { length: 12, length2: 8 },
      emphasis: { scaleSize: 8, label: { fontWeight: 'bold' } },
      data: pieList.length > 0
        ? pieList.map(d => ({ name: dictLabel(marketing_channel_type, d.channelType), value: Number(d.exposureCount || 0) }))
        : [{ name: '暂无数据', value: 0 }]
    }]
  })
}

/** 渠道效果对比柱状图（双轴 + 渐变） */
function renderChannelCompare(data) {
  if (!channelChart.value) return
  const chartData = data.filter(d => Number(d.packageCount) > 0 || Number(d.exposureCount) > 0 || Number(d.leadCount) > 0)
  if (channelInstance) channelInstance.dispose()
  channelInstance = echarts.init(channelChart.value)
  const names = chartData.map(d => dictLabel(marketing_channel_type, d.channelType))
  channelInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['曝光量', '阅读量', '点赞量', '线索量'], top: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '2%', right: '2%', bottom: '3%', top: '16%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { color: chartTextColor.value, rotate: names.length > 6 ? 30 : 0 }, axisLine: { lineStyle: { color: chartSplitColor.value } } },
    yAxis: [
      { type: 'value', name: '曝光/阅读', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '点赞/线索', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    animationDuration: 1000,
    animationDelay: idx => idx * 120,
    series: [
      { name: '曝光量', type: 'bar', barWidth: '18%', data: chartData.map(d => Number(d.exposureCount || 0)), itemStyle: { color: grad('#6366f1', '#a78bfa', true), borderRadius: [6, 6, 0, 0] } },
      { name: '阅读量', type: 'bar', barWidth: '18%', data: chartData.map(d => Number(d.readCount || 0)), itemStyle: { color: grad('#06b6d4', '#67e8f9', true), borderRadius: [6, 6, 0, 0] } },
      { name: '点赞量', type: 'bar', barWidth: '18%', yAxisIndex: 1, data: chartData.map(d => Number(d.likeCount || 0)), itemStyle: { color: grad('#f59e0b', '#fcd34d', true), borderRadius: [6, 6, 0, 0] } },
      { name: '线索量', type: 'bar', barWidth: '18%', yAxisIndex: 1, data: chartData.map(d => Number(d.leadCount || 0)), itemStyle: { color: grad('#10b981', '#6ee7b7', true), borderRadius: [6, 6, 0, 0] } }
    ]
  })
}

/** 活动归因漏斗（含环比转化率） */
function renderFunnel(data) {
  if (!funnelChart.value) return
  const layers = [
    { name: '渠道数', key: 'channelCount', c1: '#6366f1', c2: '#818cf8' },
    { name: '点击', key: 'clickCount', c1: '#3b82f6', c2: '#60a5fa' },
    { name: '报名', key: 'signupCount', c1: '#06b6d4', c2: '#22d3ee' },
    { name: '线索', key: 'leadCount', c1: '#10b981', c2: '#34d399' },
    { name: '商机', key: 'opportunityCount', c1: '#f59e0b', c2: '#fbbf24' },
    { name: '成交', key: 'dealCount', c1: '#ec4899', c2: '#f472b6' }
  ]
  let prev = null
  let funnelList = layers.map(l => {
    const v = Number(data?.[l.key] || 0)
    const rate = prev != null ? (prev > 0 ? ((v / prev) * 100).toFixed(1) + '%' : '-') : null
    prev = v
    return { name: l.name, value: v, rate, c1: l.c1, c2: l.c2 }
  }).filter(d => d.value > 0)

  if (funnelInstance) funnelInstance.dispose()
  funnelInstance = echarts.init(funnelChart.value)
  funnelInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: p => `${p.name}: ${formatNum(p.value)}${p.data.rate ? `（环比 ${p.data.rate}）` : ''}`
    },
    animationDuration: 1100,
    animationEasing: 'cubicOut',
    series: [{
      type: 'funnel',
      left: '12%',
      right: '12%',
      top: 14,
      bottom: 14,
      minSize: '22%',
      maxSize: '100%',
      sort: 'descending',
      gap: 4,
      label: { show: true, position: 'inside', color: '#fff', fontWeight: 'bold', formatter: '{b}  {c}' },
      itemStyle: { borderColor: isDark.value ? '#263445' : '#fff', borderWidth: 1 },
      emphasis: { label: { fontSize: 16 } },
      data: funnelList.length > 0
        ? funnelList.map(d => ({ name: d.name, value: d.value, rate: d.rate, itemStyle: { color: grad(d.c1, d.c2, true) } }))
        : [{ name: '暂无数据', value: 1 }]
    }]
  })
}

/** 加载看板数据（KPI + 趋势 + 渠道 + 排行） */
function loadData() {
  loading.value = true
  const params = { beginDate: dateRange.value[0], endDate: dateRange.value[1] }
  Promise.all([getOverview(params), getTrend(params), getChannelCompare(params), getContentRank(params)]).then(([ovRes, trRes, ccRes, crRes]) => {
    overview.value = ovRes.data || {}
    trendData.value = trRes.data || []
    channelCompareData.value = ccRes.data || []
    rankList.value = crRes.data || []
    animateKpi({
      ...overview.value,
      interactCount: Number(overview.value.likeCount || 0) + Number(overview.value.commentCount || 0)
    })
    loading.value = false
    nextTick(() => {
      renderTrend(trendData.value)
      renderPie(channelCompareData.value)
      renderChannelCompare(channelCompareData.value)
    })
  }).catch(() => {
    loading.value = false
  })
}

/** 加载活动归因漏斗 */
function loadFunnel() {
  if (!activityId.value) {
    nextTick(() => renderFunnel({}))
    return
  }
  getFunnel({ activityId: activityId.value }).then(res => {
    nextTick(() => renderFunnel(res.data || {}))
  }).catch(() => {})
}

/** 加载活动下拉 */
function loadActivities() {
  listActivity({ pageNum: 1, pageSize: 9999 }).then(res => {
    activityList.value = res.rows || []
    if (activityList.value.length > 0) {
      activityId.value = activityList.value[0].activityId
    }
    loadFunnel()
  }).catch(() => {
    loadFunnel()
  })
}

function handleQuery() {
  const days = Math.round((new Date(dateRange.value[1]) - new Date(dateRange.value[0])) / 86400000) + 1
  activeQuick.value = [7, 30, 90].includes(days) ? days : null
  loadData()
}
function handleRefresh() { loadData(); loadFunnel() }

/** 排行榜阅读量进度条 */
const maxRead = computed(() => Math.max(...rankList.value.map(r => Number(r.readCount || 0)), 1))
function readPercent(row) {
  return Math.max(Number(row.readCount || 0) / maxRead.value * 100, 2)
}
function interactRate(row) {
  const read = Number(row.readCount || 0)
  const inter = Number(row.likeCount || 0) + Number(row.commentCount || 0)
  return read > 0 ? ((inter / read) * 100).toFixed(1) + '%' : '-'
}

/** 窗口自适应 */
function handleResize() {
  trendInstance && trendInstance.resize()
  pieInstance && pieInstance.resize()
  channelInstance && channelInstance.resize()
  funnelInstance && funnelInstance.resize()
}

/** 监听暗色模式切换，重绘全部图表 */
watch(isDark, () => {
  if (loading.value) return
  nextTick(() => {
    renderTrend(trendData.value)
    renderPie(channelCompareData.value)
    renderChannelCompare(channelCompareData.value)
    if (activityId.value) {
      getFunnel({ activityId: activityId.value }).then(res => renderFunnel(res.data || {}))
    } else {
      renderFunnel({})
    }
  })
})

onMounted(() => {
  loadData()
  loadActivities()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendInstance && trendInstance.dispose()
  pieInstance && pieInstance.dispose()
  channelInstance && channelInstance.dispose()
  funnelInstance && funnelInstance.dispose()
})
</script>

<style scoped>
/* ============ Hero 头部 ============ */
.hero {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  padding: 22px 26px;
  margin-bottom: 16px;
  background: linear-gradient(120deg, #4338ca 0%, #7c3aed 48%, #c026d3 100%);
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.25);
}
.hero__blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(28px);
  opacity: 0.45;
  pointer-events: none;
}
.hero__blob--1 { width: 220px; height: 220px; right: -40px; top: -90px; background: radial-gradient(circle, #f0abfc, transparent 70%); animation: drift 9s ease-in-out infinite; }
.hero__blob--2 { width: 160px; height: 160px; right: 220px; bottom: -80px; background: radial-gradient(circle, #38bdf8, transparent 70%); animation: drift 11s ease-in-out infinite reverse; }
.hero__blob--3 { width: 120px; height: 120px; left: 30%; top: -50px; background: radial-gradient(circle, #fde68a, transparent 70%); opacity: 0.25; animation: drift 13s ease-in-out infinite; }
.hero__inner {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
}
.hero__title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}
.hero__desc {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
}
.hero__range {
  margin-left: 10px;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(4px);
  font-size: 12px;
}
.hero__actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.quick-btns {
  display: flex;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(6px);
  border-radius: 999px;
  padding: 3px;
}
.quick-btn {
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  transition: all 0.25s;
}
.quick-btn:hover { color: #fff; }
.quick-btn.is-active {
  background: #fff;
  color: #6d28d9;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.hero__toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  padding: 6px 10px 6px 14px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

/* ============ KPI 卡片 ============ */
.kpi-row { margin-bottom: 4px; }
.kpi-card {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 18px;
  border-radius: 12px;
  background: var(--el-bg-color, #fff);
  box-shadow: 0 2px 10px rgba(100, 116, 139, 0.08);
  margin-bottom: 14px;
  cursor: default;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeUp 0.55s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  animation-delay: calc(var(--i) * 0.06s);
}
.kpi-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 26px rgba(100, 116, 139, 0.18);
}
.kpi-card__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-right: 14px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, var(--c1), var(--c2));
  box-shadow: 0 4px 12px color-mix(in srgb, var(--c1) 35%, transparent);
  animation: floaty 3.4s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.18s);
}
.kpi-card__body { min-width: 0; }
.kpi-card__value {
  font-size: 25px;
  font-weight: 700;
  line-height: 1.15;
  color: var(--el-text-color-primary, #1e293b);
  font-variant-numeric: tabular-nums;
}
.kpi-card__label {
  font-size: 13px;
  color: var(--el-text-color-secondary, #94a3b8);
  margin-top: 3px;
}
.kpi-card__chip {
  display: inline-block;
  margin-top: 7px;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 11px;
  color: var(--c2);
  background: color-mix(in srgb, var(--c1) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--c1) 22%, transparent);
}
.kpi-card__glow {
  position: absolute;
  right: -34px;
  top: -34px;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--c1) 16%, transparent), transparent 70%);
  pointer-events: none;
}

/* ============ 图表面板 ============ */
.chart-row { margin-bottom: 4px; }
.chart-panel {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(100, 116, 139, 0.07);
  margin-bottom: 14px;
  animation: fadeUp 0.55s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  animation-delay: calc(var(--i, 0) * 0.06s);
  transition: box-shadow 0.3s ease;
}
.chart-panel:hover { box-shadow: 0 6px 20px rgba(100, 116, 139, 0.13); }
.chart-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter, #f0f0f0);
}
.chart-panel__title {
  position: relative;
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1e293b);
  padding-left: 12px;
}
.chart-panel__title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, #6366f1, #c026d3);
}
.chart-panel__desc {
  font-size: 12px;
  color: var(--el-text-color-secondary, #94a3b8);
  margin-left: 12px;
}
.chart-panel__body {
  height: 330px;
  width: 100%;
}
.chart-panel__body--table { height: auto; padding: 0; }

/* ============ 排行榜 ============ */
.rank-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  background: #f1f5f9;
  color: #64748b;
}
.rank-num.rank-1 { background: linear-gradient(135deg, #fbbf24, #f59e0b); color: #fff; box-shadow: 0 3px 8px rgba(245, 158, 11, 0.4); }
.rank-num.rank-2 { background: linear-gradient(135deg, #cbd5e1, #94a3b8); color: #fff; box-shadow: 0 3px 8px rgba(148, 163, 184, 0.4); }
.rank-num.rank-3 { background: linear-gradient(135deg, #fdba74, #ea580c); color: #fff; box-shadow: 0 3px 8px rgba(234, 88, 12, 0.35); }

.channel-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
}
.channel-tag--0 { background: #fef3c7; color: #b45309; }
.channel-tag--1 { background: #dcfce7; color: #15803d; }
.channel-tag--2 { background: #fce7f3; color: #be185d; }
.channel-tag--3 { background: #e0e7ff; color: #4338ca; }
.channel-tag--4 { background: #cffafe; color: #0e7490; }
.channel-tag--5 { background: #f1f5f9; color: #475569; }

.read-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
}
.read-bar {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  background: #f1f5f9;
  overflow: hidden;
}
.read-bar__fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #6366f1, #a855f7, #ec4899);
  background-size: 200% 100%;
  animation: shimmer 2.6s linear infinite;
  transition: width 0.8s cubic-bezier(0.22, 0.61, 0.36, 1);
}
.read-bar__num {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  min-width: 52px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}
.interact-rate {
  font-weight: 700;
  color: #db2777;
}

/* ============ 动画 ============ */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: none; }
}
@keyframes floaty {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
@keyframes drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(22px, -16px) scale(1.1); }
}
@keyframes shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}

/* ============ 暗色模式 ============ */
html.dark .kpi-card { box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3); }
html.dark .kpi-card:hover { box-shadow: 0 10px 26px rgba(0, 0, 0, 0.45); }
html.dark .chart-panel { box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3); }
html.dark .read-bar { background: #2a3441; }
html.dark .read-bar__num { color: #cbd5e1; }
html.dark .channel-tag--0 { background: rgba(251, 191, 36, 0.15); }
html.dark .channel-tag--1 { background: rgba(34, 197, 94, 0.15); }
html.dark .channel-tag--2 { background: rgba(236, 72, 153, 0.15); }
html.dark .channel-tag--3 { background: rgba(99, 102, 241, 0.15); }
html.dark .channel-tag--4 { background: rgba(6, 182, 212, 0.15); }
html.dark .channel-tag--5 { background: rgba(148, 163, 184, 0.15); }

/* ============ 响应式 ============ */
@media (max-width: 768px) {
  .hero__inner { flex-direction: column; align-items: flex-start; }
  .hero__toolbar { flex-wrap: wrap; }
  .read-bar-wrap { padding: 0 6px; }
}
</style>
