<template>
  <div class="app-container">
    <!-- ① 顶部操作栏：域标题 + 快照基准日 + 刷新 + 自动刷新开关（规范 6.0 统计看板结构） -->
    <div class="toolbar">
      <div class="toolbar__left">
        <span class="toolbar__title">{{ domainName }}主题看板</span>
        <el-tag v-if="snapshotDate" type="info" effect="plain" size="small">数据基准日：{{ snapshotDate }}</el-tag>
        <el-tag v-if="noDataNames.length" type="info" effect="plain" size="small" class="toolbar__nodata">
          {{ noDataNames.length }} 项指标暂无数据
        </el-tag>
      </div>
      <div class="toolbar__right">
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><QuestionFilled /></el-icon><span>指标说明</span>
        </button>
        <el-switch v-model="autoRefresh" active-text="自动刷新" style="margin-right: 12px;" @change="toggleAutoRefresh" />
        <el-button type="primary" size="small" icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </div>

    <!-- ② 异常预警横幅（红/黄真预警 + 脉冲圆点；info"暂无数据"聚合为工具栏轻提示） -->
    <div v-for="(alert, idx) in visibleAlerts" :key="idx" class="alert-banner" :class="'alert-banner--' + alert.level">
      <span class="alert-banner__dot" :class="'alert-banner__dot--' + alert.level"></span>
      <span class="alert-banner__text">{{ alert.message }}</span>
    </div>

    <!-- ③ KPI 指标卡网格（公司维度：一指标一张卡，左侧色条 + 下钻点击 + 估算角标 + 空状态"—"） -->
    <el-row :gutter="12">
      <el-col v-for="m in metrics" :key="m.metricCode" :xs="12" :sm="8" :md="6">
        <div class="kpi-tile" :title="m.drillUrl ? '点击下钻：' + m.metricName : ''" :style="{ cursor: m.drillUrl ? 'pointer' : 'default' }" @click="drillTo(m)">
          <div class="kpi-tile__name">
            {{ m.metricName }}
            <el-tooltip v-if="m.dataFlag === 'estimate'" content="估算口径，仅供参考" placement="top">
              <span class="kpi-tile__flag">估</span>
            </el-tooltip>
            <el-tooltip v-if="m.caliberDesc" :content="m.caliberDesc" placement="top" :show-after="200">
              <el-icon class="kpi-tile__help"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="kpi-tile__value">
            <template v-if="m.value !== null && m.value !== undefined">{{ m.value }}</template>
            <template v-else>—</template>
            <span v-if="m.value !== null && m.value !== undefined && m.unit" class="kpi-tile__unit">{{ m.unit }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ④ 图表面板：本域指标趋势（ECharts，resize 自适应 + 卸载 dispose） -->
    <div class="chart-panel" style="margin-top: 16px;">
      <div class="chart-panel__head">
        <span class="chart-panel__title">{{ domainName }}指标趋势</span>
        <span class="chart-panel__desc">近30天快照（%）</span>
      </div>
      <div class="chart-panel__body">
        <div ref="trendChartRef" style="width: 100%; height: 360px;"></div>
      </div>
    </div>

    <!-- ⑤ 空状态（规范 5.10：无数据提示来源，不显示误导性 0） -->
    <el-empty v-if="!loading && metrics.length === 0" description="该域暂无指标数据（等待快照任务产出，或在指标字典中配置本域指标）" />

    <!-- ⑥ 指标说明弹窗（规范 3.3 业务操作说明范式：口径与统计周期集中说明） -->
    <el-dialog v-model="showHelp" :title="domainName + '看板指标说明'" width="860px" append-to-body>
      <div class="status-help-content">
        <div class="help-tip">
          本页指标口径统一取自「决策支持 → 指标字典」，可在此追溯每个指标的计算方式与统计周期。
          请注意区分统计周期：<b>当月</b>为自然月累计、<b>全量累计</b>为建库至今全部业务数据、<b>时点存量</b>为快照当日状态值。
        </div>
        <el-table :data="metrics" border size="small" style="width: 100%;">
          <el-table-column label="指标名称" prop="metricName" width="140" />
          <el-table-column label="当前值" width="120">
            <template #default="{ row }">
              <span>{{ row.value !== null && row.value !== undefined ? row.value : '—' }}</span>
              <span v-if="row.unit" class="help-unit">{{ row.unit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="统计周期" width="110">
            <template #default="{ row }">
              <el-tag :type="periodTagType(row.caliberDesc)" size="small" effect="plain">
                {{ periodLabel(row.caliberDesc) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="计算口径" prop="caliberDesc" min-width="360" show-overflow-tooltip />
        </el-table>
        <div class="help-foot">
          数据基准日：{{ snapshotDate || '—' }}；指标由每日快照任务产出，口径变更在指标字典中留痕可查。
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BiBoard">
import * as echarts from 'echarts'
import { useRoute } from 'vue-router'
import { getBoardData } from '@/api/bi/board'

const { proxy } = getCurrentInstance()
const route = useRoute()

const loading = ref(true)
const snapshotDate = ref(null)
const metrics = ref([])
const alerts = ref([])
const trendRaw = ref([])
const autoRefresh = ref(false)
const showHelp = ref(false)

/** 域中文名（与 dashboard/index.vue 的 domainNames 保持一致） */
const domainNames = {
  sales: '营销销售', purchase: '采购供应', production: '生产制造',
  inventory: '仓储库存', quality: '质量管理', equipment: '设备管理',
  hr: '人力资源', safety: '安全生产', bid: '投标管理', synthesis: '综合价值链'
}
const domainColors = {
  sales: '#409EFF', purchase: '#2563eb', production: '#722ed1',
  inventory: '#4f46e5', quality: '#e8900f', equipment: '#0891b2',
  hr: '#eb2f96', safety: '#e9423a', bid: '#67C23A', synthesis: '#909399'
}

/** 当前域编码：菜单路由 /bi/board-{domain}，同组件复用，从 path 尾段剥离 board- 前缀 */
const domain = computed(() => {
  const seg = String(route.path || '').split('/').filter(Boolean)
  const last = seg[seg.length - 1] || ''
  return last.startsWith('board-') ? last.slice(6) : last
})
const domainName = computed(() => domainNames[domain.value] || domain.value)

/** 真预警横幅：仅红/黄级别（info 级"暂无数据"聚合为工具栏轻提示） */
const visibleAlerts = computed(() => alerts.value.filter(a => a.level === 'danger' || a.level === 'warning'))
const noDataNames = computed(() =>
  alerts.value.filter(a => a.level === 'info').map(a => (a.message.match(/指标【(.+?)】/) || [])[1]).filter(Boolean)
)

// 趋势图实例
const trendChartRef = ref(null)
let trendChart = null

/** 统计周期标签：从口径说明首段解析（字典口径统一以「统计周期：xxx｜口径：...」书写） */
function periodLabel(desc) {
  const t = String(desc || '')
  if (t.includes('统计周期：当月')) return '当月'
  if (t.includes('统计周期：全量累计')) return '全量累计'
  if (t.includes('统计周期：时点存量')) return '时点存量'
  return '未标注'
}
function periodTagType(desc) {
  const t = String(desc || '')
  if (t.includes('当月')) return 'primary'
  if (t.includes('全量累计')) return 'warning'
  if (t.includes('时点存量')) return 'info'
  return 'danger'
}

/** F4 指标下钻：有 drillUrl 跳转模块列表页（{value} 占位符替换当前值），外链新开页，站内路由跳转 */
function drillTo(m) {
  if (!m.drillUrl) return
  const url = String(m.drillUrl).replace('{value}', encodeURIComponent(m.value ?? ''))
  if (/^https?:/i.test(url)) { window.open(url, '_blank'); return }
  proxy.$router.push(url).catch(() => {})
}

/** 加载本域看板数据（快照 + 预警 + 趋势） */
function loadData() {
  loading.value = true
  getBoardData(domain.value).then(res => {
    const d = res.data || {}
    snapshotDate.value = d.snapshotDate ? String(d.snapshotDate).slice(0, 10) : null
    metrics.value = d.metrics || []
    alerts.value = d.alerts || []
    trendRaw.value = d.trend || []
    nextTick(renderTrendChart)
    loading.value = false
  }).catch(() => { loading.value = false })
}

/** 本域趋势指标：该域 % 指标优先（最多 6 条），不足时补该域有值指标 */
function trendMetricCodes() {
  const group = metrics.value.filter(m => m.unit === '%' && m.value !== null)
  if (group.length >= 2) return group.slice(0, 6).map(m => m.metricCode)
  const valued = metrics.value.filter(m => m.value !== null).map(m => m.metricCode)
  return valued.slice(0, 6)
}

/** 本域指标趋势图（复用 dashboard 渲染模式：按指标分组 + 日期轴 + notMerge） */
function renderTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const wantedCodes = trendMetricCodes()
  const byCode = {}
  for (const row of trendRaw.value) {
    if (!wantedCodes.includes(row.metricCode) || row.metricValue === null) continue
    if (!byCode[row.metricCode]) byCode[row.metricCode] = []
    byCode[row.metricCode].push([String(row.metricDate).slice(0, 10), Number(row.metricValue)])
  }

  const dates = [...new Set(trendRaw.value.map(r => String(r.metricDate).slice(0, 10)))].sort()
  const series = Object.keys(byCode).map(code => {
    const m = metrics.value.find(x => x.metricCode === code)
    return {
      name: m ? m.metricName : code,
      type: 'line',
      smooth: true,
      showSymbol: false,
      areaStyle: { opacity: 0.08 },
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

/** 自动刷新（5 分钟，大屏口径一致） */
let refreshTimer = null
function toggleAutoRefresh(val) {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
  if (val) refreshTimer = setInterval(loadData, 5 * 60 * 1000)
}

/** 域切换（同组件多菜单）时重新加载 */
watch(() => route.path, () => { if (String(route.path).includes('/bi/board')) loadData() })

function handleResize() {
  trendChart && trendChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) clearInterval(refreshTimer)
  trendChart && trendChart.dispose()
})
</script>

<style scoped>
/* ① 操作栏（规范 6.0：标题 + 基准日 + 刷新/自动刷新） */
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
.toolbar__nodata { cursor: help; }
.toolbar__right { display: flex; align-items: center; }

/* ② 预警横幅（红/橙/蓝三级 + 脉冲圆点，与驾驶舱样式一致） */
.alert-banner {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}
.alert-banner--danger { background: #fef0f0; color: #c45656; }
.alert-banner--warning { background: #fdf6ec; color: #b88230; }
.alert-banner--info { background: #f4f4f5; color: #909399; }
.alert-banner__dot {
  width: 8px; height: 8px; border-radius: 50%;
  margin-right: 8px; flex-shrink: 0;
  animation: bi-dot-pulse 1.6s ease-in-out infinite;
}
.alert-banner__dot--danger { background: #f56c6c; }
.alert-banner__dot--warning { background: #e6a23c; }
.alert-banner__dot--info { background: #909399; animation: none; }
@keyframes bi-dot-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.45; transform: scale(0.82); }
}

/* ③ KPI 指标卡（左侧色条 + 悬停上浮 + 估算角标） */
.kpi-tile {
  background: #fff;
  border-radius: 8px;
  border-left: 4px solid #909399;
  padding: 14px 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}
.kpi-tile:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12); }
.kpi-tile__name { font-size: 13px; color: #909399; margin-bottom: 8px; }
.kpi-tile__flag {
  display: inline-block; margin-left: 4px; padding: 0 4px;
  font-size: 11px; line-height: 16px; border-radius: 3px;
  background: #fdf6ec; color: #e6a23c; border: 1px solid #f5dab1;
}
.kpi-tile__value { font-size: 26px; font-weight: 700; color: #303133; }
.kpi-tile__unit { font-size: 12px; font-weight: 400; color: #909399; margin-left: 4px; }
.kpi-tile__help { margin-left: 4px; font-size: 13px; color: #c0c4cc; vertical-align: -2px; cursor: help; transition: color 0.15s; }
.kpi-tile__help:hover { color: #409eff; }

/* 指标说明按钮（与列表页 tip-pill 同范式） */
.tip-pill {
  display: inline-flex; align-items: center; gap: 5px; height: 28px; padding: 0 10px;
  margin-right: 12px; background: #fffaf0; border: 1px solid #fde68a; color: #92400e;
  border-radius: 999px; font-size: 13px; font-weight: 500; cursor: pointer;
  transition: all 0.15s ease; flex-shrink: 0; white-space: nowrap;
}
.tip-pill:hover { background: #fffbeb; border-color: #f59e0b; color: #7c2d12; }
.tip-pill .el-icon { font-size: 14px; color: #b45309; }

/* 指标说明弹窗内容 */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 6px; }
.help-tip {
  background: #f5f7fa; border-left: 4px solid #409eff; border-radius: 6px;
  padding: 12px 14px; margin-bottom: 14px; font-size: 13px; color: #606266; line-height: 1.7;
}
.help-tip b { color: #303133; }
.help-unit { font-size: 12px; color: #909399; margin-left: 3px; }
.help-foot { margin-top: 12px; font-size: 12px; color: #909399; }

/* ④ 图表面板 */
.chart-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 14px 16px;
}
.chart-panel__head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; }
.chart-panel__title { font-size: 15px; font-weight: 600; color: #303133; }
.chart-panel__desc { font-size: 12px; color: #909399; }
</style>
