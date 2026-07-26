<template>
  <div class="app-container dms-dashboard" v-loading="loading">
    <!-- ========== 顶部标题栏 ========== -->
    <div class="page-header">
      <div class="page-header__left">
        <div class="page-header__icon">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div class="page-header__text">
          <h2 class="page-header__title">设备管理数据看板</h2>
          <p class="page-header__desc">设备、工单、备件、维保计划 KPI 汇总</p>
        </div>
      </div>
      <div class="page-header__actions">
        <el-button :icon="Refresh" plain @click="loadData">刷新</el-button>
        <el-button type="primary" :icon="Monitor" @click="goScreen">进入全屏大屏</el-button>
      </div>
    </div>

    <!-- ========== KPI 卡片 ========== -->
    <div class="kpi-row">
      <div class="kpi-card kpi-card--total">
        <div class="kpi-card__icon">
          <el-icon><Cpu /></el-icon>
        </div>
        <div class="kpi-card__body">
          <div class="kpi-card__value">{{ stats.equipment.total }}</div>
          <div class="kpi-card__label">设备总数</div>
        </div>
        <div class="kpi-card__foot">
          <span class="kpi-tag kpi-tag--green">运行 {{ stats.equipment.inUse }}</span>
          <span class="kpi-tag kpi-tag--grey">闲置 {{ stats.equipment.idle }}</span>
        </div>
      </div>

      <div class="kpi-card kpi-card--fault">
        <div class="kpi-card__icon">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <div class="kpi-card__body">
          <div class="kpi-card__value">{{ stats.equipment.fault + stats.equipment.repair }}</div>
          <div class="kpi-card__label">故障 / 维修中</div>
        </div>
        <div class="kpi-card__foot">
          <span class="kpi-tag kpi-tag--red">故障 {{ stats.equipment.fault }}</span>
          <span class="kpi-tag kpi-tag--orange">维修 {{ stats.equipment.repair }}</span>
        </div>
      </div>

      <div class="kpi-card kpi-card--workorder">
        <div class="kpi-card__icon">
          <el-icon><Tickets /></el-icon>
        </div>
        <div class="kpi-card__body">
          <div class="kpi-card__value">{{ stats.workOrder.total }}</div>
          <div class="kpi-card__label">工单总数</div>
        </div>
        <div class="kpi-card__foot">
          <span class="kpi-tag kpi-tag--red">待处理 {{ stats.workOrder.pending }}</span>
          <span class="kpi-tag kpi-tag--orange">处理中 {{ stats.workOrder.processing }}</span>
        </div>
      </div>

      <div class="kpi-card kpi-card--part">
        <div class="kpi-card__icon">
          <el-icon><Tools /></el-icon>
        </div>
        <div class="kpi-card__body">
          <div class="kpi-card__value">{{ stats.lowStockCount }}</div>
          <div class="kpi-card__label">备件预警</div>
        </div>
        <div class="kpi-card__foot">
          <span class="kpi-tag" :class="stats.lowStockCount > 0 ? 'kpi-tag--red' : 'kpi-tag--green'">
            {{ stats.lowStockCount > 0 ? '需补货' : '库存正常' }}
          </span>
        </div>
      </div>

      <div class="kpi-card kpi-card--pm">
        <div class="kpi-card__icon">
          <el-icon><Setting /></el-icon>
        </div>
        <div class="kpi-card__body">
          <div class="kpi-card__value">{{ stats.pmPlan.total }}</div>
          <div class="kpi-card__label">PM 维保计划</div>
        </div>
        <div class="kpi-card__foot">
          <span class="kpi-tag kpi-tag--blue">进行中 {{ stats.pmPlan.active }}</span>
        </div>
      </div>
    </div>

    <!-- ========== 图表区 ========== -->
    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-card__head">
          <span class="chart-card__title"><el-icon><PieChart /></el-icon> 设备状态分布</span>
          <span class="chart-card__hint">共 {{ stats.equipment.total }} 台</span>
        </div>
        <div ref="eqChartRef" class="chart-card__body"></div>
      </div>

      <div class="chart-card">
        <div class="chart-card__head">
          <span class="chart-card__title"><el-icon><PieChart /></el-icon> 工单状态分布</span>
          <span class="chart-card__hint">共 {{ stats.workOrder.total }} 单</span>
        </div>
        <div ref="woChartRef" class="chart-card__body"></div>
      </div>
    </div>

    <!-- ========== 备件预警列表 ========== -->
    <div class="alert-section">
      <div class="alert-section__head">
        <span class="alert-section__title">
          <el-icon><Warning /></el-icon> 备件库存预警
        </span>
        <el-tag v-if="stats.lowStockCount > 0" type="danger" size="small" effect="dark">
          {{ stats.lowStockCount }} 项预警
        </el-tag>
        <el-tag v-else type="success" size="small" effect="dark">库存充足</el-tag>
      </div>
      <el-table :data="stats.lowStockParts" stripe size="default" empty-text="暂无预警，备件库存均高于安全库存">
        <el-table-column label="备件编码" prop="partCode" min-width="140" />
        <el-table-column label="备件名称" prop="partName" min-width="180" show-overflow-tooltip />
        <el-table-column label="当前库存" prop="currentStock" width="110" align="center">
          <template #default="{ row }">
            <span class="stock-num stock-num--danger">{{ row.currentStock }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="安全库存" prop="safetyStock" width="110" align="center">
          <template #default="{ row }">
            <span class="stock-num">{{ row.safetyStock }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="缺口" width="110" align="center">
          <template #default="{ row }">
            <span class="stock-num stock-num--danger">
              {{ (Number(row.safetyStock) - Number(row.currentStock)).toFixed(0) }} {{ row.unit }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default>
            <el-button type="primary" link size="small" @click="goPath('/dms/sparepart/partalert')">去处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ========== 快捷入口 ========== -->
    <div class="shortcut-section">
      <div class="shortcut-section__head">
        <span class="shortcut-section__title"><el-icon><Operation /></el-icon> 快捷入口</span>
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

<script setup name="DmsDashboard">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  DataAnalysis, Refresh, Monitor, Cpu, WarningFilled, Tickets, Tools, Setting,
  PieChart, Warning, Operation, List, Document
} from '@element-plus/icons-vue'
import { getStats } from '@/api/dms/dashboard'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)

const eqChartRef = ref(null)
const woChartRef = ref(null)
let eqChart = null
let woChart = null

const stats = reactive({
  equipment: { total: 0, inUse: 0, idle: 0, repair: 0, fault: 0, scrap: 0 },
  workOrder: { total: 0, pending: 0, processing: 0, completed: 0, verified: 0 },
  lowStockParts: [],
  lowStockCount: 0,
  pmPlan: { total: 0, active: 0 }
})

const shortcuts = [
  { label: '设备台账', path: '/dms/base/equipment', icon: Cpu, color: '#722ed1' },
  { label: '维修工单', path: '/dms/workorder/list', icon: Tickets, color: '#13c2c2' },
  { label: '备件台账', path: '/dms/sparepart/partledger', icon: Tools, color: '#eb2f96' },
  { label: '备件预警', path: '/dms/sparepart/partalert', icon: WarningFilled, color: '#f56c6c' },
  { label: '维保计划', path: '/dms/pmplan/list', icon: Setting, color: '#409eff' },
  { label: '巡检任务', path: '/dms/inspection/task', icon: List, color: '#52c41a' },
  { label: '设备分类', path: '/dms/category', icon: Document, color: '#faad14' },
  { label: '全屏大屏', path: '/dms/dashboard/screen', icon: Monitor, color: '#1890ff' }
]

function goPath(path) {
  if (!path) return
  router.push(path)
}

function goScreen() {
  router.push('/dms/dashboard/screen')
}

/** 设备状态环形图 */
function renderEqChart() {
  if (!eqChartRef.value) return
  if (eqChart) eqChart.dispose()
  eqChart = echarts.init(eqChartRef.value)
  const e = stats.equipment
  eqChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 台 ({d}%)' },
    legend: { bottom: 0, left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{c}', fontSize: 12 },
      labelLine: { length: 10, length2: 8 },
      data: [
        { value: e.inUse, name: '运行中', itemStyle: { color: '#67c23a' } },
        { value: e.idle, name: '闲置', itemStyle: { color: '#909399' } },
        { value: e.repair, name: '维修中', itemStyle: { color: '#e6a23c' } },
        { value: e.fault, name: '故障', itemStyle: { color: '#f56c6c' } },
        { value: e.scrap, name: '报废', itemStyle: { color: '#c0c4cc' } }
      ].filter(d => d.value > 0)
    }]
  })
}

/** 工单状态环形图 */
function renderWoChart() {
  if (!woChartRef.value) return
  if (woChart) woChart.dispose()
  woChart = echarts.init(woChartRef.value)
  const w = stats.workOrder
  woChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
    legend: { bottom: 0, left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{c}', fontSize: 12 },
      labelLine: { length: 10, length2: 8 },
      data: [
        { value: w.pending, name: '待处理', itemStyle: { color: '#f56c6c' } },
        { value: w.processing, name: '处理中', itemStyle: { color: '#e6a23c' } },
        { value: w.completed, name: '已完成', itemStyle: { color: '#67c23a' } },
        { value: w.verified, name: '已验收', itemStyle: { color: '#409eff' } }
      ].filter(d => d.value > 0)
    }]
  })
}

/** 加载数据 */
function loadData() {
  loading.value = true
  getStats().then(res => {
    const data = res.data || {}
    stats.equipment = data.equipment || { total: 0, inUse: 0, idle: 0, repair: 0, fault: 0, scrap: 0 }
    stats.workOrder = data.workOrder || { total: 0, pending: 0, processing: 0, completed: 0, verified: 0 }
    stats.lowStockParts = data.lowStockParts || []
    stats.lowStockCount = data.lowStockCount || 0
    stats.pmPlan = data.pmPlan || { total: 0, active: 0 }
    loading.value = false
    nextTick(() => {
      renderEqChart()
      renderWoChart()
    })
  }).catch(() => {
    loading.value = false
  })
}

function handleResize() {
  eqChart && eqChart.resize()
  woChart && woChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  eqChart && eqChart.dispose()
  woChart && woChart.dispose()
})
</script>

<style scoped lang="scss">
.dms-dashboard {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

/* ========== 顶部标题栏 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  color: #fff;

  &__left {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 26px;
      color: #fff;
    }
  }

  &__title {
    font-size: 22px;
    font-weight: 700;
    margin: 0 0 4px;
  }

  &__desc {
    font-size: 13px;
    opacity: 0.85;
    margin: 0;
  }

  &__actions {
    display: flex;
    gap: 10px;

    .el-button--primary {
      background: rgba(255, 255, 255, 0.9);
      border-color: transparent;
      color: #096dd9;
    }

    .el-button.is-plain {
      color: #fff;
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(255, 255, 255, 0.4);
    }
  }
}

/* ========== KPI 卡片 ========== */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.kpi-card {
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
  }

  &--total::before { background: #722ed1; }
  &--fault::before { background: #f56c6c; }
  &--workorder::before { background: #13c2c2; }
  &--part::before { background: #faad14; }
  &--pm::before { background: #409eff; }

  &__icon {
    position: absolute;
    right: 14px;
    top: 14px;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0.12;

    .el-icon {
      font-size: 22px;
    }
  }

  &--total .kpi-card__icon { background: #722ed1; .el-icon { color: #722ed1; } }
  &--fault .kpi-card__icon { background: #f56c6c; .el-icon { color: #f56c6c; } }
  &--workorder .kpi-card__icon { background: #13c2c2; .el-icon { color: #13c2c2; } }
  &--part .kpi-card__icon { background: #faad14; .el-icon { color: #faad14; } }
  &--pm .kpi-card__icon { background: #409eff; .el-icon { color: #409eff; } }

  &__body {
    margin-right: 48px;
  }

  &__value {
    font-size: 32px;
    font-weight: 700;
    line-height: 1.2;
    color: #1d2129;
  }

  &__label {
    font-size: 13px;
    color: #909399;
    margin-top: 4px;
  }

  &__foot {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    flex-wrap: wrap;
  }
}

.kpi-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;

  &--green { background: rgba(103, 194, 58, 0.12); color: #67c23a; }
  &--grey { background: rgba(144, 147, 153, 0.12); color: #909399; }
  &--red { background: rgba(245, 108, 108, 0.12); color: #f56c6c; }
  &--orange { background: rgba(230, 162, 60, 0.12); color: #e6a23c; }
  &--blue { background: rgba(64, 158, 255, 0.12); color: #409eff; }
}

/* ========== 图表区 ========== */
.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 18px;
    border-bottom: 1px solid #f0f0f0;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;

    .el-icon {
      color: #409eff;
    }
  }

  &__hint {
    font-size: 12px;
    color: #909399;
  }

  &__body {
    height: 300px;
    padding: 8px;
  }
}

/* ========== 备件预警 ========== */
.alert-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px 18px;
  margin-bottom: 20px;

  &__head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;

    .el-tag {
      margin-left: auto;
    }
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;

    .el-icon {
      color: #faad14;
    }
  }
}

.stock-num {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #606266;

  &--danger {
    color: #f56c6c;
  }
}

/* ========== 快捷入口 ========== */
.shortcut-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px 18px;

  &__head {
    margin-bottom: 14px;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;

    .el-icon {
      color: #409eff;
    }
  }
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 14px;
}

.shortcut-item {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s, background 0.2s;

  &:hover {
    transform: translateY(-3px);
    background: #ecf5ff;
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
@media (max-width: 1400px) {
  .kpi-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-row {
    grid-template-columns: 1fr;
  }
  .shortcut-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
  .shortcut-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .dms-dashboard {
    padding: 12px;
  }
}

/* ========== 暗色模式 ========== */
html.dark {
  .dms-dashboard {
    background: var(--el-bg-color);
  }
  .kpi-card,
  .chart-card,
  .alert-section,
  .shortcut-section {
    background: var(--el-bg-color-overlay);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  .kpi-card__value,
  .chart-card__title,
  .alert-section__title,
  .shortcut-section__title {
    color: var(--el-text-color-primary);
  }
  .kpi-card__label,
  .chart-card__hint {
    color: var(--el-text-color-secondary);
  }
  .shortcut-item {
    background: var(--el-fill-color-light);

    &:hover {
      background: var(--el-fill-color);
    }
  }
  .shortcut-item__label {
    color: var(--el-text-color-regular);
  }
}
</style>
