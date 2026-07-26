<template>
  <div class="app-container dashboard-wrap" v-loading="loading">
    <!-- 顶部标题栏 -->
    <div class="dash-header">
      <div class="dash-header__title">
        <el-icon class="dash-header__icon"><DataAnalysis /></el-icon>
        <span>设备管理数据看板</span>
      </div>
      <div class="dash-header__actions">
        <span class="dash-header__time" v-if="lastUpdate">数据更新于 {{ lastUpdate }}</span>
        <el-button :icon="Refresh" circle size="small" @click="loadData" />
      </div>
    </div>

    <!-- KPI 概览卡片 -->
    <div class="kpi-row">
      <div class="kpi-card kpi-card--primary">
        <div class="kpi-card__icon"><el-icon><Cpu /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">设备总数</div>
          <div class="kpi-card__value">{{ eqStats.total }}</div>
          <div class="kpi-card__sub">
            在用率 <b>{{ usageRate }}%</b>
          </div>
        </div>
      </div>
      <div class="kpi-card kpi-card--warning">
        <div class="kpi-card__icon"><el-icon><Tickets /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">工单总数</div>
          <div class="kpi-card__value">{{ woStats.total }}</div>
          <div class="kpi-card__sub">
            待处理 <b class="text-warning">{{ woStats.pending }}</b> · 处理中 <b class="text-warning">{{ woStats.processing }}</b>
          </div>
        </div>
      </div>
      <div class="kpi-card kpi-card--success">
        <div class="kpi-card__icon"><el-icon><Calendar /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">维护计划</div>
          <div class="kpi-card__value">{{ pmStats.total }}</div>
          <div class="kpi-card__sub">
            启用中 <b class="text-success">{{ pmStats.active }}</b>
          </div>
        </div>
      </div>
      <div class="kpi-card kpi-card--danger">
        <div class="kpi-card__icon"><el-icon><WarningFilled /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">备件预警</div>
          <div class="kpi-card__value">{{ stats.lowStockCount || 0 }}</div>
          <div class="kpi-card__sub">
            低于安全库存的备件数
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <!-- 设备状态分布 -->
      <div class="chart-panel">
        <div class="chart-panel__head">
          <span class="chart-panel__title">设备状态分布</span>
          <span class="chart-panel__desc">共 {{ eqStats.total }} 台设备</span>
        </div>
        <div ref="equipmentChart" class="chart-panel__body"></div>
      </div>

      <!-- 工单状态分布 -->
      <div class="chart-panel">
        <div class="chart-panel__head">
          <span class="chart-panel__title">工单状态分布</span>
          <span class="chart-panel__desc">共 {{ woStats.total }} 张工单</span>
        </div>
        <div ref="workOrderChart" class="chart-panel__body"></div>
      </div>

      <!-- 设备健康概览 -->
      <div class="chart-panel">
        <div class="chart-panel__head">
          <span class="chart-panel__title">设备健康度</span>
          <span class="chart-panel__desc">在用 / 异常占比</span>
        </div>
        <div ref="healthChart" class="chart-panel__body"></div>
      </div>
    </div>

    <!-- 备件预警 + 关键指标 -->
    <div class="bottom-row">
      <!-- 备件预警列表 -->
      <div class="chart-panel chart-panel--lg">
        <div class="chart-panel__head">
          <span class="chart-panel__title">
            备件库存预警
            <el-badge :value="stats.lowStockCount || 0" :hidden="!stats.lowStockCount" class="alert-badge" />
          </span>
          <span class="chart-panel__desc">库存 ≤ 安全库存</span>
        </div>
        <div class="alert-list" v-if="lowStockParts.length > 0">
          <div class="alert-item" v-for="(p, i) in lowStockParts" :key="i">
            <div class="alert-item__info">
              <span class="alert-item__name">{{ p.partName }}</span>
              <span class="alert-item__code">{{ p.partCode }}</span>
            </div>
            <div class="alert-item__bar">
              <div class="alert-item__bar-track">
                <div class="alert-item__bar-fill" :style="{ width: stockPercent(p) + '%' }"></div>
              </div>
              <div class="alert-item__bar-label">
                <span class="text-danger">{{ p.currentStock }}{{ dictUnitLabel(p.unit) }}</span>
                <span class="text-muted">/ {{ p.safetyStock }}{{ dictUnitLabel(p.unit) }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无预警，库存充足" :image-size="80" class="alert-empty" />
      </div>

      <!-- 关键指标说明 -->
      <div class="chart-panel chart-panel--sm">
        <div class="chart-panel__head">
          <span class="chart-panel__title">关键指标说明</span>
        </div>
        <div class="kpi-defs">
          <div class="kpi-def" v-for="(item, i) in kpiDefs" :key="i">
            <div class="kpi-def__tag" :style="{ background: item.color }">{{ item.tag }}</div>
            <div class="kpi-def__text">{{ item.text }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="DmsDashboard">
import * as echarts from 'echarts'
import { Refresh, DataAnalysis, Cpu, Tickets, Calendar, WarningFilled } from '@element-plus/icons-vue'
import { getStats } from '@/api/dms/dashboard'

const { proxy } = getCurrentInstance()
const { wms_unit } = proxy.useDict('wms_unit')

const loading = ref(true)
const stats = ref({})
const lastUpdate = ref('')

// 图表引用
const equipmentChart = ref(null)
const workOrderChart = ref(null)
const healthChart = ref(null)

let equipmentInstance = null
let workOrderInstance = null
let healthInstance = null

/** 是否暗色模式 */
const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#606266')
const chartSplitColor = computed(() => isDark.value ? '#434343' : '#eee')
const chartBorderColor = computed(() => isDark.value ? '#1d1e1f' : '#fff')

/** 设备统计 */
const eqStats = computed(() => stats.value.equipment || {})
/** 工单统计 */
const woStats = computed(() => stats.value.workOrder || {})
/** PM计划统计 */
const pmStats = computed(() => stats.value.pmPlan || {})
/** 备件预警列表 */
const lowStockParts = computed(() => stats.value.lowStockParts || [])

/** 在用率 */
const usageRate = computed(() => {
  const total = eqStats.value.total || 0
  if (!total) return 0
  return ((eqStats.value.inUse || 0) / total * 100).toFixed(1)
})

/** 关键指标说明 */
const kpiDefs = [
  { tag: 'MTTR', color: '#409eff', text: '平均修复时间 — 工单完工时长平均值' },
  { tag: 'MTBF', color: '#67c23a', text: '平均故障间隔 — 相邻故障工单时间间隔平均值' },
  { tag: 'PM完成率', color: '#13c2c2', text: '按时完成PM数 / 计划PM数' },
  { tag: '故障率', color: '#f56c6c', text: '故障工单数 / 设备数 / 时间' },
  { tag: 'OEE', color: '#e6a23c', text: '综合设备效率 = 可用率×性能×合格率' },
  { tag: '维修成本', color: '#722ed1', text: '备件消耗 + 外修 + 工时折算' }
]

/** 字典值转单位标签 */
function dictUnitLabel(val) {
  if (!val && val !== '0') return ''
  const dictItem = wms_unit.value.find(d => d.value === val)
  return dictItem ? dictItem.label : val
}

/** 库存百分比（用于进度条） */
function stockPercent(p) {
  const safety = Number(p.safetyStock) || 0
  const current = Number(p.currentStock) || 0
  if (safety <= 0) return 0
  return Math.min(100, Math.max(2, (current / safety) * 100))
}

/** 初始化设备状态图表 */
function renderEquipmentChart() {
  if (!equipmentChart.value) return
  const d = eqStats.value
  const data = [
    { name: '在用', value: d.inUse || 0, itemStyle: { color: '#409eff' } },
    { name: '闲置', value: d.idle || 0, itemStyle: { color: '#909399' } },
    { name: '维修中', value: d.repair || 0, itemStyle: { color: '#e6a23c' } },
    { name: '故障', value: d.fault || 0, itemStyle: { color: '#f56c6c' } },
    { name: '报废', value: d.scrap || 0, itemStyle: { color: '#b37feb' } }
  ].filter(i => i.value > 0)

  if (equipmentInstance) { equipmentInstance.dispose() }
  equipmentInstance = echarts.init(equipmentChart.value)
  equipmentInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}台 ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: chartBorderColor.value, borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', formatter: '{b}\n{c}台', color: chartTextColor.value },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.15)' }
      },
      data: data.length > 0 ? data : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 初始化工单状态图表 */
function renderWorkOrderChart() {
  if (!workOrderChart.value) return
  const d = woStats.value
  const data = [
    { name: '待处理', value: d.pending || 0, color: '#909399' },
    { name: '处理中', value: d.processing || 0, color: '#e6a23c' },
    { name: '已完成', value: d.completed || 0, color: '#67c23a' },
    { name: '已验收', value: d.verified || 0, color: '#13c2c2' }
  ]

  if (workOrderInstance) { workOrderInstance.dispose() }
  workOrderInstance = echarts.init(workOrderChart.value)
  workOrderInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}: {c}张' },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    yAxis: {
      type: 'category',
      data: data.map(i => i.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: chartTextColor.value }
    },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: data.map(i => ({
        value: i.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: i.color + 'cc' },
            { offset: 1, color: i.color }
          ]),
          borderRadius: [0, 6, 6, 0]
        }
      })),
      label: { show: true, position: 'right', formatter: '{c}', fontSize: 13, fontWeight: 'bold', color: chartTextColor.value }
    }]
  })
}

/** 初始化设备健康度图表 */
function renderHealthChart() {
  if (!healthChart.value) return
  const d = eqStats.value
  const total = d.total || 0
  const healthy = d.inUse || 0
  const rate = total > 0 ? Math.round((healthy / total) * 100) : 0

  if (healthInstance) { healthInstance.dispose() }
  healthInstance = echarts.init(healthChart.value)
  healthInstance.setOption({
    series: [{
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      center: ['50%', '55%'],
      radius: '90%',
      progress: {
        show: true,
        width: 14,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 0.5, color: '#e6a23c' },
            { offset: 1, color: '#f56c6c' }
          ])
        }
      },
      axisLine: { lineStyle: { width: 14, color: [[1, isDark.value ? '#434343' : '#f0f0f0']] } },
      pointer: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      anchor: { show: false },
      title: {
        show: true,
        offsetCenter: [0, '25%'],
        fontSize: 13,
        color: chartTextColor.value
      },
      detail: {
        valueAnimation: true,
        fontSize: 32,
        fontWeight: 'bold',
        offsetCenter: [0, '-5%'],
        formatter: '{value}%',
        color: rate >= 80 ? '#67c23a' : rate >= 50 ? '#e6a23c' : '#f56c6c'
      },
      data: [{
        value: rate,
        name: '设备在用率'
      }]
    }],
    graphic: [{
      type: 'text',
      left: 'center',
      bottom: '8%',
      style: {
        text: `在用 ${healthy} / 共 ${total} 台`,
        fontSize: 12,
        fill: chartTextColor.value
      }
    }]
  })
}

/** 窗口自适应 */
function handleResize() {
  equipmentInstance && equipmentInstance.resize()
  workOrderInstance && workOrderInstance.resize()
  healthInstance && healthInstance.resize()
}

function loadData() {
  loading.value = true
  getStats().then(res => {
    stats.value = res.data || {}
    loading.value = false
    lastUpdate.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    nextTick(() => {
      renderEquipmentChart()
      renderWorkOrderChart()
      renderHealthChart()
    })
  }).catch(() => {
    loading.value = false
  })
}

/** 监听暗色模式切换，重绘图表 */
watch(isDark, () => {
  if (!loading.value && stats.value.equipment) {
    nextTick(() => {
      renderEquipmentChart()
      renderWorkOrderChart()
      renderHealthChart()
    })
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  equipmentInstance && equipmentInstance.dispose()
  workOrderInstance && workOrderInstance.dispose()
  healthInstance && healthInstance.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-wrap {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

/* ========== 顶部标题栏 ========== */
.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  &__title {
    display: flex;
    align-items: center;
    font-size: 20px;
    font-weight: 700;
    color: #1d2129;
  }

  &__icon {
    font-size: 24px;
    color: #409eff;
    margin-right: 10px;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__time {
    font-size: 12px;
    color: #909399;
  }
}

/* ========== KPI 概览卡片 ========== */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.kpi-card {
  position: relative;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  color: #fff;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  }

  &::before {
    content: '';
    position: absolute;
    right: -20px;
    top: -20px;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
  }

  &::after {
    content: '';
    position: absolute;
    right: 10px;
    bottom: -30px;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
  }

  &--primary {
    background: linear-gradient(135deg, #409eff, #36cfc9);
  }
  &--warning {
    background: linear-gradient(135deg, #e6a23c, #ffa940);
  }
  &--success {
    background: linear-gradient(135deg, #67c23a, #95de64);
  }
  &--danger {
    background: linear-gradient(135deg, #f56c6c, #ff7875);
  }

  &__icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    flex-shrink: 0;
    z-index: 1;

    .el-icon {
      font-size: 26px;
      color: #fff;
    }
  }

  &__body {
    z-index: 1;
  }

  &__label {
    font-size: 13px;
    opacity: 0.9;
    margin-bottom: 4px;
  }

  &__value {
    font-size: 30px;
    font-weight: 700;
    line-height: 1.2;
    margin-bottom: 4px;
  }

  &__sub {
    font-size: 12px;
    opacity: 0.85;

    b {
      font-weight: 600;
    }
  }
}

/* ========== 图表面板通用 ========== */
.chart-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 16px;
}

.chart-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  &--lg {
    min-height: 320px;
  }

  &--sm {
    min-height: 320px;
  }

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px 8px;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;
  }

  &__desc {
    font-size: 12px;
    color: #909399;
  }

  &__body {
    height: 280px;
    padding: 0 12px 12px;
  }
}

.alert-badge {
  margin-left: 8px;
  :deep(.el-badge__content) {
    background: #f56c6c;
  }
}

/* ========== 备件预警列表 ========== */
.alert-list {
  padding: 4px 20px 16px;
  max-height: 260px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
  }
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &__info {
    width: 200px;
    flex-shrink: 0;
    margin-right: 16px;
  }

  &__name {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: #303133;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__code {
    font-size: 11px;
    color: #909399;
  }

  &__bar {
    flex: 1;
    display: flex;
    align-items: center;
  }

  &__bar-track {
    flex: 1;
    height: 8px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
    margin-right: 12px;
  }

  &__bar-fill {
    height: 100%;
    border-radius: 4px;
    background: linear-gradient(90deg, #f56c6c, #ff7875);
    transition: width 0.6s ease;
  }

  &__bar-label {
    font-size: 12px;
    white-space: nowrap;
    display: flex;
    gap: 4px;
  }
}

.alert-empty {
  padding: 40px 0;
}

/* ========== 关键指标说明 ========== */
.kpi-defs {
  padding: 4px 20px 16px;
}

.kpi-def {
  display: flex;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &__tag {
    flex-shrink: 0;
    width: 72px;
    text-align: center;
    padding: 2px 0;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    margin-right: 12px;
  }

  &__text {
    font-size: 12px;
    color: #606266;
    line-height: 1.6;
    padding-top: 1px;
  }
}

/* ========== 通用文字 ========== */
.text-warning {
  color: #fff;
}
.text-success {
  color: #fff;
}
.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
.text-muted {
  color: #909399;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-row {
    grid-template-columns: 1fr;
  }
  .bottom-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .dashboard-wrap {
    padding: 12px;
  }
}

/* ========== 暗色模式 ========== */
html.dark {
  .dashboard-wrap {
    background: var(--el-bg-color);
  }
  .dash-header__title {
    color: var(--el-text-color-primary);
  }
  .chart-panel {
    background: var(--el-bg-color-overlay);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .chart-panel__title {
    color: var(--el-text-color-primary);
  }
  .alert-item {
    border-bottom-color: var(--el-border-color-light);
  }
  .alert-item__name {
    color: var(--el-text-color-primary);
  }
  .alert-item__bar-track {
    background: var(--el-border-color-light);
  }
  .kpi-def {
    border-bottom-color: var(--el-border-color-light);
  }
  .kpi-def__text {
    color: var(--el-text-color-regular);
  }
}
</style>
