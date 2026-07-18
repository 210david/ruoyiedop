<template>
  <div class="app-container home-dashboard" v-loading="loading">
    <!-- ========== 顶部欢迎栏 ========== -->
    <div class="welcome-banner">
      <div class="welcome-banner__content">
        <div class="welcome-banner__text">
          <h2 class="welcome-banner__title">
            {{ greeting }}，{{ welcome.nickName || welcome.userName || '管理员' }}
          </h2>
          <p class="welcome-banner__desc">
            {{ welcome.today }} {{ welcome.weekday }} · 欢迎使用企业数字化运营平台
          </p>
        </div>
        <div class="welcome-banner__actions">
          <el-button type="primary" plain :icon="Refresh" @click="loadData">刷新数据</el-button>
        </div>
      </div>
      <div class="welcome-banner__deco"></div>
    </div>

    <!-- ========== 平台预警汇总 ========== -->
    <div class="section">
      <div class="section__head">
        <span class="section__title">
          <el-icon><Bell /></el-icon>
          平台预警提醒
        </span>
        <el-tag v-if="alerts.total > 0" type="danger" size="small" effect="dark">
          {{ alerts.total }} 项预警
        </el-tag>
        <el-tag v-else type="success" size="small" effect="dark">系统正常</el-tag>
      </div>
      <div class="alert-row">
        <!-- WMS 预警 -->
        <div class="alert-card alert-card--wms" @click="goPath('/wms/inv/alert')">
          <div class="alert-card__header">
            <span class="alert-card__name">
              <el-icon><Box /></el-icon>
              仓库管理预警
            </span>
            <el-badge :value="alerts.wms.total" :hidden="!alerts.wms.total" type="primary" />
          </div>
          <div class="alert-card__body">
            <div class="alert-item-mini" v-if="alerts.wms.lowStock">
              <span class="alert-item-mini__dot alert-item-mini__dot--danger"></span>
              <span class="alert-item-mini__label">库存不足</span>
              <span class="alert-item-mini__value">{{ alerts.wms.lowStock }}</span>
            </div>
            <div class="alert-item-mini" v-if="alerts.wms.overstock">
              <span class="alert-item-mini__dot alert-item-mini__dot--warning"></span>
              <span class="alert-item-mini__label">库存积压</span>
              <span class="alert-item-mini__value">{{ alerts.wms.overstock }}</span>
            </div>
            <div class="alert-item-mini" v-if="alerts.wms.nearExpiry">
              <span class="alert-item-mini__dot alert-item-mini__dot--warning"></span>
              <span class="alert-item-mini__label">临期预警</span>
              <span class="alert-item-mini__value">{{ alerts.wms.nearExpiry }}</span>
            </div>
            <div class="alert-item-mini" v-if="alerts.wms.expired">
              <span class="alert-item-mini__dot alert-item-mini__dot--danger"></span>
              <span class="alert-item-mini__label">过期预警</span>
              <span class="alert-item-mini__value">{{ alerts.wms.expired }}</span>
            </div>
            <el-empty v-if="!alerts.wms.total" description="暂无预警" :image-size="48" />
          </div>
        </div>

        <!-- DMS 预警 -->
        <div class="alert-card alert-card--dms" @click="goPath('/dms/sparepart/partalert')">
          <div class="alert-card__header">
            <span class="alert-card__name">
              <el-icon><Cpu /></el-icon>
              设备管理预警
            </span>
            <el-badge :value="alerts.dms.total" :hidden="!alerts.dms.total" type="primary" />
          </div>
          <div class="alert-card__body">
            <div class="alert-item-mini" v-if="alerts.dms.partAlert">
              <span class="alert-item-mini__dot alert-item-mini__dot--danger"></span>
              <span class="alert-item-mini__label">备件预警</span>
              <span class="alert-item-mini__value">{{ alerts.dms.partAlert }}</span>
            </div>
            <div class="alert-item-mini" v-if="alerts.dms.faultEquipment">
              <span class="alert-item-mini__dot alert-item-mini__dot--danger"></span>
              <span class="alert-item-mini__label">故障设备</span>
              <span class="alert-item-mini__value">{{ alerts.dms.faultEquipment }}</span>
            </div>
            <div class="alert-item-mini" v-if="alerts.dms.pendingWorkOrder">
              <span class="alert-item-mini__dot alert-item-mini__dot--warning"></span>
              <span class="alert-item-mini__label">待处理工单</span>
              <span class="alert-item-mini__value">{{ alerts.dms.pendingWorkOrder }}</span>
            </div>
            <el-empty v-if="!alerts.dms.total" description="暂无预警" :image-size="48" />
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="alert-card alert-card--todo">
          <div class="alert-card__header">
            <span class="alert-card__name">
              <el-icon><Checked /></el-icon>
              我的待办
            </span>
          </div>
          <div class="alert-card__body todo-body">
            <div class="todo-item" @click="goPath('/dms/workorder/list')">
              <div class="todo-item__icon todo-item__icon--warning">
                <el-icon><Tickets /></el-icon>
              </div>
              <div class="todo-item__info">
                <span class="todo-item__label">维修工单</span>
                <span class="todo-item__value">{{ todos.workOrders || 0 }} 项待处理</span>
              </div>
            </div>
            <div class="todo-item" @click="goPath('/wms/inv/alert')">
              <div class="todo-item__icon todo-item__icon--danger">
                <el-icon><WarningFilled /></el-icon>
              </div>
              <div class="todo-item__info">
                <span class="todo-item__label">库存预警</span>
                <span class="todo-item__value">{{ todos.stockAlerts || 0 }} 项待处理</span>
              </div>
            </div>
            <div class="todo-item" @click="goPath('/dms/sparepart/partalert')">
              <div class="todo-item__icon todo-item__icon--danger">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="todo-item__info">
                <span class="todo-item__label">备件预警</span>
                <span class="todo-item__value">{{ todos.partAlerts || 0 }} 项待处理</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 业务系统概览 ========== -->
    <div class="section">
      <div class="section__head">
        <span class="section__title">
          <el-icon><Grid /></el-icon>
          业务系统概览
        </span>
        <span class="section__hint">点击已上线模块卡片可快速进入</span>
      </div>
      <div class="module-grid">
        <div
          v-for="m in modules"
          :key="m.code"
          :class="['module-card', { 'module-card--planned': m.status === 'planned', 'module-card--clickable': m.status === 'active' }]"
          @click="m.status === 'active' && goPath(m.path)"
        >
          <!-- 模块头部 -->
          <div class="module-card__header" :style="{ background: m.gradient }">
            <div class="module-card__icon">
              <el-icon><component :is="moduleIcon(m.code)" /></el-icon>
            </div>
            <div class="module-card__title-wrap">
              <span class="module-card__name">{{ m.name }}</span>
              <span class="module-card__desc">{{ m.desc }}</span>
            </div>
            <el-tag v-if="m.status === 'planned'" size="small" type="info" effect="plain" class="module-card__badge">规划中</el-tag>
            <el-tag v-else size="small" type="success" effect="dark" class="module-card__badge">已上线</el-tag>
          </div>

          <!-- 模块指标 -->
          <div class="module-card__body" v-if="m.metrics && m.metrics.length > 0">
            <div class="metric-cell" v-for="(metric, i) in m.metrics" :key="i">
              <div class="metric-cell__value" :style="{ color: m.color }">
                {{ metric.value }}<span class="metric-cell__unit" v-if="metric.unit">{{ metric.unit }}</span>
              </div>
              <div class="metric-cell__label">{{ metric.label }}</div>
            </div>
          </div>
          <div class="module-card__body module-card__body--empty" v-else>
            <el-icon><More /></el-icon>
            <span>模块开发中，敬请期待</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 出入库趋势图表 ========== -->
    <div class="section" v-if="trendData.length > 0">
      <div class="section__head">
        <span class="section__title">
          <el-icon><TrendCharts /></el-icon>
          近7天出入库趋势
        </span>
      </div>
      <div class="chart-panel">
        <div ref="trendChart" class="chart-panel__body"></div>
      </div>
    </div>

    <!-- ========== 快捷入口 ========== -->
    <div class="section">
      <div class="section__head">
        <span class="section__title">
          <el-icon><Operation /></el-icon>
          快捷入口
        </span>
      </div>
      <div class="shortcut-grid">
        <div class="shortcut-item" v-for="(s, i) in shortcuts" :key="i" @click="goPath(s.path)">
          <div class="shortcut-item__icon" :style="{ background: s.color }">
            <el-icon><component :is="s.icon" /></el-icon>
          </div>
          <span class="shortcut-item__label">{{ s.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="Index">
import * as echarts from 'echarts'
import {
  Refresh, Bell, Box, Cpu, Checked, Tickets, WarningFilled, Tools,
  Grid, More, TrendCharts, Operation,
  ShoppingCart, Money, Medal, Setting, Calendar, UserFilled,
  Download, Upload, DataAnalysis
} from '@element-plus/icons-vue'
import { getDashboard } from '@/api/home'
import useUserStore from '@/store/modules/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const welcome = ref({})
const alerts = ref({ total: 0, wms: { total: 0 }, dms: { total: 0 } })
const modules = ref([])
const todos = ref({})
const trendData = ref([])

const trendChart = ref(null)
let trendInstance = null

/** 问候语 */
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 17) return '下午好'
  if (h < 19) return '傍晚好'
  return '晚上好'
})

/** 模块图标映射 */
function moduleIcon(code) {
  const iconMap = {
    wms: Box,
    dms: Cpu,
    purchase: ShoppingCart,
    sales: Money,
    quality: Medal,
    safety: WarningFilled,
    production: Setting,
    planning: Calendar,
    hr: UserFilled
  }
  return iconMap[code] || Grid
}

/** 快捷入口 */
const shortcuts = [
  { label: '库存查询', path: '/wms/inv/list', icon: Box, color: '#409eff' },
  { label: '入库管理', path: '/wms/inbound/order', icon: Download, color: '#67c23a' },
  { label: '出库管理', path: '/wms/outbound/order', icon: Upload, color: '#e6a23c' },
  { label: '库存预警', path: '/wms/inv/alert', icon: WarningFilled, color: '#f56c6c' },
  { label: '设备台账', path: '/dms/base/equipment', icon: Cpu, color: '#722ed1' },
  { label: '维修工单', path: '/dms/workorder/list', icon: Tickets, color: '#13c2c2' },
  { label: '备件台账', path: '/dms/sparepart/partledger', icon: Tools, color: '#eb2f96' },
  { label: '设备看板', path: '/dms/dashboard/overview', icon: DataAnalysis, color: '#1890ff' }
]

/** 路由跳转 */
function goPath(path) {
  if (!path) return
  router.push(path)
}

/** 渲染趋势图表 */
function renderTrendChart() {
  if (!trendChart.value || trendData.value.length === 0) return
  const data = trendData.value
  const dates = data.map(d => d.date)
  const inbound = data.map(d => Number(d.inboundQty) || 0)
  const outbound = data.map(d => Number(d.outboundQty) || 0)

  if (trendInstance) { trendInstance.dispose() }
  trendInstance = echarts.init(trendChart.value)
  trendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库数量', '出库数量'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '8%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      {
        name: '入库数量',
        type: 'line',
        smooth: true,
        data: inbound,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }]) }
      },
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        data: outbound,
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103,194,58,0.3)' }, { offset: 1, color: 'rgba(103,194,58,0.02)' }]) }
      }
    ]
  })
}

/** 加载数据 */
function loadData() {
  loading.value = true
  getDashboard().then(res => {
    const data = res.data || {}
    welcome.value = data.welcome || {}
    alerts.value = data.alerts || { total: 0, wms: { total: 0 }, dms: { total: 0 } }
    modules.value = data.modules || []
    todos.value = data.todos || {}
    trendData.value = data.inboundOutboundTrend || []
    loading.value = false
    nextTick(() => {
      renderTrendChart()
    })
  }).catch(() => {
    loading.value = false
  })
}

function handleResize() {
  trendInstance && trendInstance.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendInstance && trendInstance.dispose()
})
</script>

<style scoped lang="scss">
.home-dashboard {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

/* ========== 欢迎栏 ========== */
.welcome-banner {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 20px;
  overflow: hidden;
  color: #fff;

  &__content {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &__title {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 14px;
    opacity: 0.85;
    margin: 0;
  }

  &__deco {
    position: absolute;
    right: -40px;
    top: -40px;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
  }

  &__deco::after {
    content: '';
    position: absolute;
    right: 60px;
    bottom: -60px;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.06);
  }
}

/* ========== 通用 Section ========== */
.section {
  margin-bottom: 24px;

  &__head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;

    .el-tag {
      margin-left: auto;
    }
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 16px;
    font-weight: 600;
    color: #1d2129;

    .el-icon {
      font-size: 18px;
      color: #409eff;
    }
  }

  &__hint {
    margin-left: auto;
    font-size: 12px;
    color: #909399;
  }
}

/* ========== 预警卡片 ========== */
.alert-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.alert-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 18px;
    border-bottom: 1px solid #f0f0f0;
  }

  &__name {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;

    .el-icon {
      font-size: 16px;
    }
  }

  &--wms &__name .el-icon { color: #409eff; }
  &--dms &__name .el-icon { color: #67c23a; }
  &--todo &__name .el-icon { color: #e6a23c; }

  &__body {
    padding: 12px 18px 16px;
    min-height: 120px;
  }
}

.alert-item-mini {
  display: flex;
  align-items: center;
  padding: 8px 0;

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 10px;

    &--danger { background: #f56c6c; }
    &--warning { background: #e6a23c; }
  }

  &__label {
    flex: 1;
    font-size: 13px;
    color: #606266;
  }

  &__value {
    font-size: 15px;
    font-weight: 700;
    color: #303133;
  }
}

/* ========== 待办 ========== */
.todo-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #f5f7fa;
  }

  &__icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;

    .el-icon {
      font-size: 18px;
      color: #fff;
    }

    &--warning { background: #e6a23c; }
    &--danger { background: #f56c6c; }
  }

  &__info {
    display: flex;
    flex-direction: column;
  }

  &__label {
    font-size: 13px;
    font-weight: 500;
    color: #303133;
  }

  &__value {
    font-size: 12px;
    color: #909399;
  }
}

/* ========== 业务模块 ========== */
.module-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.module-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;

  &--clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    }
  }

  &--planned {
    opacity: 0.75;
  }

  &__header {
    display: flex;
    align-items: center;
    padding: 16px 18px;
    color: #fff;
    position: relative;
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;

    .el-icon {
      font-size: 20px;
      color: #fff;
    }
  }

  &__title-wrap {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  &__name {
    font-size: 15px;
    font-weight: 600;
  }

  &__desc {
    font-size: 11px;
    opacity: 0.85;
    margin-top: 2px;
  }

  &__badge {
    flex-shrink: 0;
  }

  &__body {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 16px 18px;
    min-height: 110px;

    &--empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #c0c4cc;
      font-size: 13px;
      gap: 8px;

      .el-icon {
        font-size: 24px;
      }
    }
  }
}

.metric-cell {
  text-align: center;

  &__value {
    font-size: 22px;
    font-weight: 700;
    line-height: 1.3;
  }

  &__unit {
    font-size: 12px;
    font-weight: 400;
    margin-left: 2px;
  }

  &__label {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
  }
}

/* ========== 图表面板 ========== */
.chart-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  &__body {
    height: 320px;
    padding: 12px;
  }
}

/* ========== 快捷入口 ========== */
.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16px;
}

.shortcut-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 18px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 22px;
      color: #fff;
    }
  }

  &__label {
    font-size: 13px;
    color: #303133;
    font-weight: 500;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .alert-row {
    grid-template-columns: 1fr;
  }
  .module-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .shortcut-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .module-grid {
    grid-template-columns: 1fr;
  }
  .shortcut-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .home-dashboard {
    padding: 12px;
  }
}

/* ========== 暗色模式 ========== */
html.dark {
  .home-dashboard {
    background: var(--el-bg-color);
  }
  .section__title {
    color: var(--el-text-color-primary);
  }
  .alert-card,
  .module-card,
  .chart-panel,
  .shortcut-item {
    background: var(--el-bg-color-overlay);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .alert-card__name,
  .module-card__name {
    color: var(--el-text-color-primary);
  }
  .alert-item-mini__label,
  .metric-cell__label,
  .shortcut-item__label {
    color: var(--el-text-color-regular);
  }
  .alert-item-mini__value,
  .metric-cell__value {
    color: var(--el-text-color-primary);
  }
  .todo-item:hover {
    background: var(--el-fill-color-light);
  }
}
</style>
