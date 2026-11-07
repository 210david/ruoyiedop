<template>
  <div class="app-container" :class="{ 'screen-mode': screenMode }">
    <!-- F9 大屏模式悬浮条（退出 + 轮播指示） -->
    <div v-if="screenMode" class="screen-bar">
      <span class="screen-bar__title">决策驾驶舱 · 大屏模式</span>
      <span class="screen-bar__domain">{{ domainNames[activeDomain] || '全部域' }}（自动轮播 15s）</span>
      <el-button type="primary" size="small" icon="Close" @click="exitScreen">退出大屏（Esc）</el-button>
    </div>
    <!-- ① 顶部操作栏：时间范围 + 快照基准日 + 刷新 + 自动刷新开关 -->
    <div v-show="!screenMode" class="toolbar">
      <div class="toolbar__left">
        <span class="toolbar__title">决策驾驶舱</span>
        <el-tag v-if="snapshotDate" type="info" effect="plain" size="small">
          数据基准日：{{ snapshotDate }}
        </el-tag>
        <el-tooltip v-if="noDataNames.length" :content="'暂无数据：' + noDataNames.join('、') + '（业务模块未产生或未核实，详见数据完整度环图）'" placement="bottom">
          <el-tag type="info" effect="plain" size="small" class="toolbar__nodata">
            {{ noDataNames.length }} 项指标暂无数据
          </el-tag>
        </el-tooltip>
      </div>
      <div class="toolbar__right">
        <el-radio-group v-model="activeView" size="small" style="margin-right: 12px;" @change="onViewChange">
          <el-radio-button label="kpi">KPI 总览</el-radio-button>
          <el-radio-button label="chain">价值链路</el-radio-button>
        </el-radio-group>
        <el-radio-group v-show="activeView === 'kpi'" v-model="trendDays" size="small" @change="renderTrendChart">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
        </el-radio-group>
        <el-switch v-show="activeView === 'kpi'" v-model="autoRefresh" active-text="自动刷新" style="margin: 0 12px;" @change="toggleAutoRefresh" />
        <el-button type="primary" size="small" icon="Refresh" :loading="loading" @click="onViewChange">刷新</el-button>
        <el-button size="small" icon="Monitor" @click="enterScreen" v-hasPermi="['bi:dashboard:view']">大屏模式</el-button>
        <el-button type="warning" size="small" icon="Download" @click="handleExport" v-hasPermi="['bi:metric:export']">导出</el-button>
      </div>
    </div>

    <!-- ② 异常预警横幅区（仅红/黄真预警 + 脉冲圆点；info"暂无数据"不占横幅） -->
    <template v-if="!screenMode">
    <div v-for="(alert, idx) in visibleAlerts" :key="idx" class="alert-banner" :class="'alert-banner--' + alert.level">
      <span class="alert-banner__dot" :class="'alert-banner__dot--' + alert.level"></span>
      <span class="alert-banner__text">{{ alert.message }}</span>
    </div>
    </template>

    <!-- ②.5 主题看板域导航（仅 KPI 视图且非大屏显示） -->
    <div v-show="activeView === 'kpi' && !screenMode" class="domain-nav">
      <el-radio-group v-model="activeDomain" size="small">
        <el-radio-button label="all">全部域</el-radio-button>
        <el-radio-button v-for="(group, domain) in metricGroups" :key="domain" :label="domain">
          {{ domainNames[domain] || domain }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- ③ KPI 指标卡网格（按域分组、左侧色条、数字滚动、悬停上浮、环比箭头；大屏模式也显示） -->
    <template v-if="activeView === 'kpi' || screenMode">
    <div v-for="(group, domain) in visibleGroups" :key="domain" class="domain-section">
      <div class="domain-section__head">
        <span class="domain-section__bar" :style="{ background: domainColors[domain] || '#909399' }"></span>
        <span class="domain-section__name">{{ domainNames[domain] || domain }}</span>
        <span class="domain-section__desc">{{ group.length }} 项指标</span>
      </div>
      <el-row :gutter="12">
        <el-col v-for="m in group" :key="m.metricCode" :xs="12" :sm="8" :md="6" :lg="activeDomain === 'all' ? 4 : 6">
          <div class="kpi-tile" :style="{ borderLeftColor: domainColors[domain] || '#909399', cursor: m.drillUrl ? 'pointer' : 'default' }" :title="m.drillUrl ? '点击下钻：' + m.metricName : ''" @click="drillTo(m)">
            <div class="kpi-tile__name">
              {{ m.metricName }}
              <el-tooltip v-if="m.dataFlag === 'estimate'" content="估算口径，仅供参考" placement="top">
                <span class="kpi-tile__flag">估</span>
              </el-tooltip>
            </div>
            <div class="kpi-tile__value">
              <template v-if="m.value !== null && m.value !== undefined">{{ animated(m.metricCode) }}</template>
              <template v-else>—</template>
              <span v-if="m.value !== null && m.value !== undefined && m.unit" class="kpi-tile__unit">{{ m.unit }}</span>
            </div>
            <div v-if="deltaOf(m)" class="kpi-tile__delta" :class="deltaOf(m).up ? 'kpi-tile__delta--up' : 'kpi-tile__delta--down'">
              {{ deltaOf(m).up ? '↑' : '↓' }} {{ deltaOf(m).text }} 较上期
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- ④ 图表区：核心指标趋势 + 数据完整度环形（大屏下隐藏，只读轮播 KPI） -->
    <el-row v-show="!screenMode" :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :md="14">
        <div class="chart-panel">
          <div class="chart-panel__head">
            <span class="chart-panel__title">{{ activeDomain === 'all' ? '核心指标趋势' : (domainNames[activeDomain] || activeDomain) + '指标趋势' }}</span>
            <span class="chart-panel__desc">近{{ trendDays }}天快照（%）</span>
          </div>
          <div class="chart-panel__body">
            <div ref="trendChartRef" style="width: 100%; height: 340px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :md="10">
        <div class="chart-panel">
          <div class="chart-panel__head">
            <span class="chart-panel__title">指标数据完整度</span>
            <span class="chart-panel__desc">最新快照 有值 / 估算 / 无数据</span>
          </div>
          <div class="chart-panel__body">
            <div ref="healthChartRef" style="width: 100%; height: 340px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>
    </template>

    <!-- ③.6 F6 价值链路视图（销售到现金链 / 供应保障链） -->
    <template v-if="activeView === 'chain'">
      <div v-if="chains.length === 0 && !loading" class="chain-empty">
        <span>暂无链路数据（等待快照任务产出）</span>
      </div>
      <div v-for="chain in chains" :key="chain.code" class="chart-panel chain-panel">
        <div class="chart-panel__head">
          <span class="chart-panel__title">{{ chain.name }}</span>
          <span class="chart-panel__desc">{{ chain.desc }} · 对比上月</span>
        </div>
        <div class="chart-panel__body">
          <div class="chain-flow">
            <template v-for="(node, ni) in chain.nodes" :key="node.metricCode">
              <div class="chain-node" :style="{ borderTopColor: chainColors[chain.code] || '#409EFF' }">
                <div class="chain-node__step">
                  <span class="chain-node__label">{{ node.linkLabel }}</span>
                  <el-tooltip v-if="node.dataFlag === 'estimate'" content="估算口径，仅供参考" placement="top">
                    <span class="chain-node__flag">估</span>
                  </el-tooltip>
                </div>
                <div class="chain-node__name">{{ node.metricName }}</div>
                <div class="chain-node__value">
                  <template v-if="node.value !== null && node.value !== undefined">{{ node.value }}</template>
                  <template v-else>—</template>
                  <span v-if="node.value !== null && node.value !== undefined && node.unit" class="chain-node__unit">{{ node.unit }}</span>
                </div>
                <div v-if="chainDelta(node)" class="chain-node__delta" :class="chainDelta(node).up ? 'chain-node__delta--up' : 'chain-node__delta--down'">
                  {{ chainDelta(node).up ? '↑' : '↓' }} {{ chainDelta(node).text }}
                </div>
                <div v-else class="chain-node__delta chain-node__delta--none">—</div>
              </div>
              <div v-if="ni < chain.nodes.length - 1" class="chain-arrow">➜</div>
            </template>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup name="BiDashboard">
import * as echarts from 'echarts'
import { getBiOverview, getValueChain } from '@/api/bi/dashboard'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const snapshotDate = ref(null)
const metrics = ref([])
const alerts = ref([])
const trendRaw = ref([])
const trendDays = ref(30)
const autoRefresh = ref(false)
/** 主题看板域导航：''=初始未选（数据到达后自动聚焦第一个域），all=全部域，否则聚焦单域 */
const activeDomain = ref('')
/** F6 视图切换：kpi=KPI 总览（默认），chain=价值链路 */
const activeView = ref('kpi')
/** F6 链路数据 */
const chains = ref([])
const chainColors = { sales_to_cash: '#409EFF', supply_assurance: '#2563eb' }

// 图表引用与实例
const trendChartRef = ref(null)
const healthChartRef = ref(null)
let trendChart = null
let healthChart = null

// 域配色与中文名
const domainColors = {
  sales: '#409EFF', purchase: '#2563eb', production: '#722ed1',
  inventory: '#4f46e5', quality: '#e8900f', equipment: '#0891b2',
  hr: '#eb2f96', safety: '#e9423a', bid: '#67C23A', synthesis: '#909399'
}
const domainNames = {
  sales: '营销销售', purchase: '采购供应', production: '生产制造',
  inventory: '仓储库存', quality: '质量管理', equipment: '设备管理',
  hr: '人力资源', safety: '安全生产', bid: '投标管理', synthesis: '综合价值链'
}

// 数字滚动动画状态：metricCode → 当前显示值
const displayValues = ref({})
let animHandles = {}

/** 按域分组 */
const metricGroups = computed(() => {
  const groups = {}
  for (const m of metrics.value) {
    if (!groups[m.domain]) groups[m.domain] = []
    groups[m.domain].push(m)
  }
  return groups
})

/** 域导航过滤：all 显示全部分组，否则仅当前域 */
const visibleGroups = computed(() => {
  if (activeDomain.value === 'all') return metricGroups.value
  const g = metricGroups.value[activeDomain.value]
  return g ? { [activeDomain.value]: g } : {}
})

/** 真预警横幅：仅红/黄级别（info 级"暂无数据"不占横幅，聚合为工具栏轻提示） */
const visibleAlerts = computed(() => alerts.value.filter(a => a.level === 'danger' || a.level === 'warning'))
/** 暂无数据的指标名（info 级横幅聚合来源） */
const noDataNames = computed(() =>
  alerts.value.filter(a => a.level === 'info').map(a => (a.message.match(/指标【(.+?)】/) || [])[1]).filter(Boolean)
)

/**
 * 环比：当前值 vs 上一快照值（取趋势序列倒数第二个点）。
 * 中性展示（↑/↓ + 幅度），不做好坏着色——指标方向语义各异（合格率升为好、NCR 数升为坏）。
 */
function deltaOf(m) {
  if (m.value === null || m.value === undefined) return null
  const series = trendRaw.value.filter(r => r.metricCode === m.metricCode && r.metricValue !== null)
  if (series.length < 2) return null
  const sorted = series.map(r => Number(r.metricValue))
  const prev = sorted[sorted.length - 2]
  if (prev === 0) return null
  const diff = m.value - prev
  if (Math.abs(diff) < 1e-9) return null
  const pct = m.unit === '%' ? Math.abs(diff).toFixed(2) + 'pct' : (Math.abs(diff / prev) * 100).toFixed(1) + '%'
  return { up: diff > 0, text: pct }
}

/** 数字滚动：目标值变化时从当前值平滑过渡 */
function animated(code) {
  return displayValues.value[code] ?? metrics.value.find(m => m.metricCode === code)?.value ?? 0
}

function animateTo(code, target) {
  if (typeof target !== 'number') return
  cancelAnimationFrame(animHandles[code])
  const decimals = (String(target).split('.')[1] || '').length
  const start = displayValues.value[code] ?? 0
  const startTime = performance.now()
  const duration = 600
  function step(now) {
    const p = Math.min((now - startTime) / duration, 1)
    const eased = 1 - Math.pow(1 - p, 3)
    displayValues.value[code] = Number((start + (target - start) * eased).toFixed(decimals))
    if (p < 1) {
      animHandles[code] = requestAnimationFrame(step)
    }
  }
  animHandles[code] = requestAnimationFrame(step)
}

/** 视图切换/刷新按钮：按当前视图拉对应数据 */
function onViewChange() {
  if (activeView.value === 'chain') {
    loadValueChain()
  } else {
    loadData()
  }
}

// ==================== F9 大屏模式（只读轮播，车间/展厅部署） ====================
const screenMode = ref(false)
let screenTimer = null

/** 进入大屏：全屏 + 强制开启 5 分钟数据自动刷新 + 15s 域轮播 */
function enterScreen() {
  screenMode.value = true
  if (document.documentElement.requestFullscreen) {
    document.documentElement.requestFullscreen().catch(() => {})
  }
  // 大屏数据只读自动刷新（间隔可调 PRD F9，v1 固定 5 分钟）
  if (!autoRefresh.value) { autoRefresh.value = true; toggleAutoRefresh(true) }
  // 域轮播：all 或单域 → 依次切换到每个域
  const domains = Object.keys(metricGroups.value)
  if (domains.length > 0) {
    let idx = Math.max(0, domains.indexOf(activeDomain.value === 'all' ? domains[0] : activeDomain.value))
    activeDomain.value = domains[idx % domains.length]
    if (screenTimer) clearInterval(screenTimer)
    screenTimer = setInterval(() => {
      idx = (idx + 1) % domains.length
      activeDomain.value = domains[idx]
    }, 15 * 1000)
  }
}

/** 退出大屏 */
function exitScreen() {
  screenMode.value = false
  if (screenTimer) { clearInterval(screenTimer); screenTimer = null }
  if (document.fullscreenElement && document.exitFullscreen) {
    document.exitFullscreen().catch(() => {})
  }
}

/** 浏览器 Esc 退出全屏时同步状态 */
function onFullscreenChange() {
  if (!document.fullscreenElement && screenMode.value) {
    screenMode.value = false
    if (screenTimer) { clearInterval(screenTimer); screenTimer = null }
  }
}

/** F6 链路环比：当前月值 vs 上月值（中性展示，方向语义各异不着色语义） */
function chainDelta(node) {
  if (node.value === null || node.value === undefined || node.prevValue === null || node.prevValue === undefined) return null
  const diff = Number(node.value) - Number(node.prevValue)
  if (Math.abs(diff) < 1e-9) return null
  const pct = node.unit === '%' ? Math.abs(diff).toFixed(2) + 'pct' : (Math.abs(diff / Number(node.prevValue)) * 100).toFixed(1) + '%'
  return { up: diff > 0, text: pct + ' 环比' }
}

/** 加载 F6 价值链路数据 */
function loadValueChain() {
  loading.value = true
  getValueChain().then(res => {
    chains.value = (res.data && res.data.chains) || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

/** 加载数据 */
function loadData() {
  loading.value = true
  getBiOverview().then(res => {
    const d = res.data || {}
    snapshotDate.value = d.snapshotDate ? String(d.snapshotDate).slice(0, 10) : null
    metrics.value = d.metrics || []
    alerts.value = d.alerts || []
    trendRaw.value = d.trend || []
    metrics.value.forEach(m => animateTo(m.metricCode, m.value))
    // 默认聚焦第一个业务域（一次只看一个模块，避免全部域堆叠；用户可手动切"全部域"）
    if (!activeDomain.value) {
      activeDomain.value = Object.keys(metricGroups.value)[0] || 'all'
    }
    nextTick(() => {
      renderTrendChart()
      renderHealthChart()
    })
    loading.value = false
  }).catch(() => { loading.value = false })
}

/** F8 驾驶舱导出（快照指标 Excel，遵循规范 4.3.11） */
function handleExport() {
  proxy.download('/bi/dashboard/export', {}, `bi_dashboard_${Date.now()}.xlsx`)
}

/** F4 指标下钻：有 drillUrl 跳转模块列表页（{value} 占位符替换当前值），外链新开页，站内路由跳转 */
function drillTo(m) {
  if (!m.drillUrl) return
  const url = String(m.drillUrl).replace('{value}', encodeURIComponent(m.value ?? ''))
  if (/^https?:/i.test(url)) { window.open(url, '_blank'); return }
  proxy.$router.push(url).catch(() => {})
}

/** 核心指标趋势（全部域：固定核心 % 指标；单域：该域 % 指标优先，不足时补该域有值指标） */
const CORE_PCT_CODES = ['QMS_PASS_RATE', 'QMS_CAPA_RATE', 'MMS_WO_RATE', 'PMS_ONTIME_RATE', 'MK_WIN_RATE', 'SAFETY_RECT_RATE']

function trendMetricCodes() {
  if (activeDomain.value === 'all') return CORE_PCT_CODES
  const group = metricGroups.value[activeDomain.value] || []
  const pctCodes = group.filter(m => m.unit === '%' && m.value !== null).map(m => m.metricCode)
  if (pctCodes.length >= 2) return pctCodes.slice(0, 6)
  const valued = group.filter(m => m.value !== null).map(m => m.metricCode)
  return valued.slice(0, 6)
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const wantedCodes = trendMetricCodes()

  // 按指标分组
  const byCode = {}
  for (const row of trendRaw.value) {
    if (!wantedCodes.includes(row.metricCode) || row.metricValue === null) continue
    if (!byCode[row.metricCode]) byCode[row.metricCode] = []
    byCode[row.metricCode].push([String(row.metricDate).slice(0, 10), Number(row.metricValue)])
  }

  // 日期轴（近 N 天）
  const dates = [...new Set(trendRaw.value.map(r => String(r.metricDate).slice(0, 10)))].sort()
  const series = Object.keys(byCode).map(code => {
    const m = metrics.value.find(x => x.metricCode === code)
    return {
      name: m ? m.metricName : code,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: dates.map(d => {
        const hit = byCode[code].find(x => x[0] === d)
        return hit ? hit[1] : null
      })
    }
  })

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, type: 'scroll' },
    grid: { left: '3%', right: '4%', bottom: '14%', top: '8%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: dates },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
    color: ['#409EFF', '#67C23A', '#E6A23C', '#722ed1', '#0891b2', '#e9423a'],
    series
  }, { notMerge: true })
}

/** 数据完整度环形（中心完整率） */
function renderHealthChart() {
  if (!healthChartRef.value) return
  if (!healthChart) healthChart = echarts.init(healthChartRef.value)

  const total = metrics.value.length || 1
  const hasValue = metrics.value.filter(m => m.value !== null && m.value !== undefined).length
  const estimate = metrics.value.filter(m => m.dataFlag === 'estimate' && m.value !== null).length
  const noData = total - hasValue
  const rate = Math.round(hasValue * 100 / total)

  healthChart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center' },
    title: {
      text: rate + '%',
      subtext: '完整率',
      left: 'center',
      top: '38%',
      textStyle: { fontSize: 26, fontWeight: 700, color: '#303133' },
      subtextStyle: { fontSize: 12, color: '#909399' }
    },
    series: [{
      name: '数据完整度',
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['50%', '45%'],
      label: { show: true, formatter: '{b}: {c}' },
      data: [
        { name: '正常', value: hasValue - estimate, itemStyle: { color: '#67C23A' } },
        { name: '估算', value: estimate, itemStyle: { color: '#E6A23C' } },
        { name: '无数据', value: noData, itemStyle: { color: '#c0c4cc' } }
      ]
    }]
  }, { notMerge: true })
}

/** 自动刷新（5 分钟） */
let refreshTimer = null
function toggleAutoRefresh(val) {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (val) refreshTimer = setInterval(loadData, 5 * 60 * 1000)
}

/** 域切换时重绘趋势图 */
watch(activeDomain, () => {
  nextTick(renderTrendChart)
})

function handleResize() {
  trendChart && trendChart.resize()
  healthChart && healthChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
  document.addEventListener('fullscreenchange', onFullscreenChange)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  Object.values(animHandles).forEach(h => cancelAnimationFrame(h))
  if (refreshTimer) clearInterval(refreshTimer)
  if (screenTimer) clearInterval(screenTimer)
  trendChart && trendChart.dispose()
  healthChart && healthChart.dispose()
})
</script>

<style scoped>
/* ① 操作栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 12px;
}
.toolbar__left { display: flex; align-items: center; }
.toolbar__title { font-size: 16px; font-weight: 600; color: #303133; margin-right: 12px; }
.toolbar__nodata { cursor: help; margin-right: 12px; }
.toolbar__right { display: flex; align-items: center; }

/* ② 预警横幅（红/橙/蓝三级 + 脉冲圆点） */
.alert-banner {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 13px;
}
.alert-banner--danger { background: #fef0f0; color: #c45656; border: 1px solid #fde2e2; }
.alert-banner--warning { background: #fdf6ec; color: #b88230; border: 1px solid #faecd8; }
.alert-banner--info { background: #f4f4f5; color: #909399; border: 1px solid #e9e9eb; }
.alert-banner__dot {
  width: 8px; height: 8px; border-radius: 50%;
  margin-right: 10px; flex-shrink: 0;
  animation: pulse 1.6s ease-in-out infinite;
}
.alert-banner__dot--danger { background: #f56c6c; }
.alert-banner__dot--warning { background: #e6a23c; }
.alert-banner__dot--info { background: #909399; animation: none; }
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 currentColor; opacity: 1; }
  50% { box-shadow: 0 0 0 5px transparent; opacity: 0.6; }
}

/* ③ 域分组 + KPI 卡 */
.domain-section { margin-top: 14px; }
.domain-section__head { display: flex; align-items: center; margin-bottom: 8px; }
.domain-section__bar { width: 4px; height: 16px; border-radius: 2px; margin-right: 8px; }
.domain-section__name { font-size: 14px; font-weight: 600; color: #303133; margin-right: 8px; }
.domain-section__desc { font-size: 12px; color: #909399; }

.kpi-tile {
  background: #fff;
  border-radius: 6px;
  border-left: 4px solid #909399;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 12px 14px;
  margin-bottom: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.kpi-tile:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.kpi-tile__name { font-size: 12px; color: #606266; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.kpi-tile__flag {
  display: inline-block;
  font-size: 10px;
  color: #e6a23c;
  border: 1px solid #e6a23c;
  border-radius: 3px;
  padding: 0 3px;
  margin-left: 4px;
  line-height: 14px;
  cursor: help;
}
.kpi-tile__value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.kpi-tile__unit { font-size: 12px; font-weight: 400; color: #909399; margin-left: 3px; }
.kpi-tile__delta { font-size: 11px; margin-top: 4px; }
.kpi-tile__delta--up { color: #c45656; }
.kpi-tile__delta--down { color: #4f9e4f; }

/* ②.5 主题看板域导航 */
.domain-nav {
  margin-top: 12px;
  padding: 10px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

/* ③.6 F6 价值链路视图 */
.chain-panel { margin-top: 14px; }.chain-empty {
  margin-top: 14px;
  padding: 40px;
  text-align: center;
  color: #909399;
  background: #fff;
  border-radius: 8px;
}
.chain-flow {
  display: flex;
  align-items: stretch;
  flex-wrap: wrap;
  gap: 4px;
}
.chain-node {
  flex: 1;
  min-width: 130px;
  background: #f8fafc;
  border-top: 3px solid #409EFF;
  border-radius: 6px;
  padding: 12px 14px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.chain-node:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.chain-node__step { display: flex; align-items: center; margin-bottom: 4px; }
.chain-node__label { font-size: 11px; color: #909399; }
.chain-node__flag {
  display: inline-block;
  font-size: 10px;
  color: #e6a23c;
  border: 1px solid #e6a23c;
  border-radius: 3px;
  padding: 0 3px;
  margin-left: 4px;
  line-height: 14px;
  cursor: help;
}
.chain-node__name { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.chain-node__value { font-size: 20px; font-weight: 700; color: #303133; line-height: 1.2; }
.chain-node__unit { font-size: 12px; font-weight: 400; color: #909399; margin-left: 3px; }
.chain-node__delta { font-size: 11px; margin-top: 4px; }
.chain-node__delta--up { color: #c45656; }
.chain-node__delta--down { color: #4f9e4f; }
.chain-node__delta--none { color: #c0c4cc; }
.chain-arrow {
  display: flex;
  align-items: center;
  color: #b1b8c2;
  font-size: 16px;
  padding: 0 2px;
}

/* ④ 图表面板（沿用 QMS dashboard 样式体系） */
.chart-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
.chart-panel__head { padding: 16px 20px; border-bottom: 1px solid #ebeef5; }
.chart-panel__title { font-size: 15px; font-weight: 600; color: #303133; }
.chart-panel__desc { font-size: 12px; color: #909399; margin-left: 8px; }
.chart-panel__body { padding: 16px 20px; }

/* ⑤ F9 大屏模式：字号放大 + 疏朗留白，只读轮播 */
.screen-mode { padding: 24px 32px; }
.screen-mode .kpi-tile { padding: 20px 24px; margin-bottom: 20px; }
.screen-mode .kpi-tile__name { font-size: 16px; margin-bottom: 10px; }
.screen-mode .kpi-tile__value { font-size: 38px; }
.screen-mode .kpi-tile__unit { font-size: 16px; }
.screen-mode .kpi-tile__delta { font-size: 14px; margin-top: 8px; }
.screen-mode .domain-section__name { font-size: 22px; }
.screen-mode .domain-section__desc { font-size: 15px; }
.screen-mode .domain-section__bar { height: 22px; width: 5px; }
.screen-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.screen-bar__title { font-size: 18px; font-weight: 700; color: #303133; }
.screen-bar__domain { font-size: 14px; color: #909399; }
</style>
