<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="dash-header">
      <div>
        <span class="dash-header__title">生产运营看板</span>
        <span class="dash-header__desc">实时监控生产全流程核心指标</span>
      </div>
      <div class="dash-header__right">
        <span class="text-muted" v-if="lastUpdate">最后更新：{{ lastUpdate }}</span>
        <el-button type="primary" icon="Refresh" size="small" @click="loadData" :loading="loading">刷新数据</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- KPI 概览卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--blue">
            <div class="kpi-card__icon"><el-icon size="28"><Document /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.workOrder?.total || 0 }}</div>
              <div class="kpi-card__label">工单总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--green">
            <div class="kpi-card__icon"><el-icon size="28"><Calendar /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.mps?.total || 0 }}</div>
              <div class="kpi-card__label">计划总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--orange">
            <div class="kpi-card__icon"><el-icon size="28"><Warning /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.abnormal?.total || 0 }}</div>
              <div class="kpi-card__label">异常总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--cyan">
            <div class="kpi-card__icon"><el-icon size="28"><Tickets /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.report?.total || 0 }}</div>
              <div class="kpi-card__label">报工总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--purple">
            <div class="kpi-card__icon"><el-icon size="28"><TrendCharts /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.workOrder?.inProduction || 0 }}</div>
              <div class="kpi-card__label">生产中工单</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--red">
            <div class="kpi-card__icon"><el-icon size="28"><WarningFilled /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.abnormalSummary?.unclosed || 0 }}</div>
              <div class="kpi-card__label">未闭环异常</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 产量统计卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--blue">
            <div class="amount-card__label">计划总数量</div>
            <div class="amount-card__value">{{ formatQty(overview.workOrder?.totalPlanQty) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--green">
            <div class="amount-card__label">完工总数量</div>
            <div class="amount-card__value">{{ formatQty(overview.workOrder?.totalFinishedQty) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--orange">
            <div class="amount-card__label">完工率</div>
            <div class="amount-card__value">{{ overview.workOrder?.finishRate || 0 }}%</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--cyan">
            <div class="amount-card__label">合格率</div>
            <div class="amount-card__value">{{ overview.workOrder?.yieldRate || 0 }}%</div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第一行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">工单状态分布</span>
              <span class="chart-panel__desc">按状态统计工单数量</span>
            </div>
            <div ref="woStatusChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">工单月度趋势</span>
              <span class="chart-panel__desc">按月统计创建数与完工数</span>
            </div>
            <div ref="woTrendChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第二行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">异常类型分布</span>
              <span class="chart-panel__desc">按异常类型统计</span>
            </div>
            <div ref="abTypeChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">异常严重等级</span>
              <span class="chart-panel__desc">按严重等级统计</span>
            </div>
            <div ref="abSeverityChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">工单来源分析</span>
              <span class="chart-panel__desc">按来源类型统计</span>
            </div>
            <div ref="woSourceChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第三行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">报工产量趋势</span>
              <span class="chart-panel__desc">按日统计合格数与不良数</span>
            </div>
            <div ref="reportTrendChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">产品产量 TOP10</span>
              <span class="chart-panel__desc">按完工数量排名</span>
            </div>
            <div ref="productTopChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第四行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">产能单元负荷 TOP10</span>
              <span class="chart-panel__desc">按计划数量排名</span>
            </div>
            <div ref="resourceLoadChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">工单类型分布</span>
              <span class="chart-panel__desc">生产/返工/外协</span>
            </div>
            <div ref="woTypeChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 最新工单列表 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="24">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">最新工单</span>
              <span class="chart-panel__desc">最近创建的5条工单</span>
            </div>
            <div class="chart-panel__body" style="padding: 0; height: auto;">
<el-table :data="latestOrders" border stripe size="small">
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="工单编号" align="center" prop="workOrderNo" width="140" />
                <el-table-column label="产品名称" align="center" prop="productName" min-width="120" show-overflow-tooltip />
                <el-table-column label="计划数量" align="center" prop="planQty" width="100" />
                <el-table-column label="完工数量" align="center" prop="finishedQty" width="100" />
                <el-table-column label="状态" align="center" prop="status" width="100">
                  <template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template>
                </el-table-column>
                <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                  <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup name="MmsDashboard">
import * as echarts from 'echarts'
import { Document, Calendar, Warning, Tickets, TrendCharts, WarningFilled } from '@element-plus/icons-vue'
import { getOverview, getWorkOrderStatusDist, getWorkOrderTrend, getProductTop10, getAbnormalTypeDist, getAbnormalSeverityDist, getReportTrend, getWorkOrderSourceDist, getResourceLoad, getWorkOrderTypeDist } from '@/api/mms/dashboard'
import { listWorkOrder } from '@/api/mms/workorder'

const { proxy } = getCurrentInstance()
const { mms_workorder_status } = proxy.useDict('mms_workorder_status')

const loading = ref(true)
const lastUpdate = ref('')
const overview = ref({})
const latestOrders = ref([])

// 图表引用
const woStatusChart = ref(null)
const woTrendChart = ref(null)
const abTypeChart = ref(null)
const abSeverityChart = ref(null)
const woSourceChart = ref(null)
const reportTrendChart = ref(null)
const productTopChart = ref(null)
const resourceLoadChart = ref(null)
const woTypeChart = ref(null)

// 图表实例
let woStatusInstance = null
let woTrendInstance = null
let abTypeInstance = null
let abSeverityInstance = null
let woSourceInstance = null
let reportTrendInstance = null
let productTopInstance = null
let resourceLoadInstance = null
let woTypeInstance = null

// 主题色
const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#606266')
const chartSplitColor = computed(() => isDark.value ? '#434343' : '#eee')

/** 数量格式化 */
function formatQty(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

/** 工单状态分布饼图 */
function renderWoStatus(data) {
  if (!woStatusChart.value) return
  const chartData = data.map(d => ({ name: d.name, value: d.count || 0 }))
  const colors = ['#909399', '#409eff', '#3370ff', '#67c23a', '#909399', '#e6a23c', '#f56c6c']

  if (woStatusInstance) woStatusInstance.dispose()
  woStatusInstance = echarts.init(woStatusChart.value)
  woStatusInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 工单月度趋势图 */
function renderWoTrend(data) {
  if (!woTrendChart.value) return
  const chartData = data.map(d => ({ date: d.date, created: d.createdCount || 0, finished: d.finishedCount || 0 }))

  if (woTrendInstance) woTrendInstance.dispose()
  woTrendInstance = echarts.init(woTrendChart.value)
  woTrendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['创建数', '完工数'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.date), axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '创建数', type: 'bar', barWidth: '35%', data: chartData.map(d => d.created), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '完工数', type: 'line', smooth: true, data: chartData.map(d => d.finished), itemStyle: { color: '#67c23a' }, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103,194,58,0.3)' }, { offset: 1, color: 'rgba(103,194,58,0.02)' }]) } }
    ]
  })
}

/** 异常类型分布饼图 */
function renderAbType(data) {
  if (!abTypeChart.value) return
  const chartData = data.map(d => ({ name: d.name, value: d.count || 0 }))
  const colors = ['#f56c6c', '#e6a23c', '#409eff', '#f5385b', '#909399']

  if (abTypeInstance) abTypeInstance.dispose()
  abTypeInstance = echarts.init(abTypeChart.value)
  abTypeInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 异常严重等级柱状图 */
function renderAbSeverity(data) {
  if (!abSeverityChart.value) return
  const chartData = data.map(d => ({ name: d.name, value: d.count || 0 }))
  const colors = { '一般': '#e6a23c', '严重': '#f56c6c', '紧急': '#f5385b' }

  if (abSeverityInstance) abSeverityInstance.dispose()
  abSeverityInstance = echarts.init(abSeverityChart.value)
  abSeverityInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}: {c}件' },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: chartData.map(d => ({ value: d.value, itemStyle: { color: colors[d.name] || '#409eff', borderRadius: [0, 6, 6, 0] } })),
      label: { show: true, position: 'right', formatter: '{c}', fontSize: 13, fontWeight: 'bold', color: chartTextColor.value }
    }]
  })
}

/** 工单来源分析饼图 */
function renderWoSource(data) {
  if (!woSourceChart.value) return
  const chartData = data.map(d => ({ name: d.name, value: d.count || 0 }))
  const colors = ['#409eff', '#67c23a', '#e6a23c']

  if (woSourceInstance) woSourceInstance.dispose()
  woSourceInstance = echarts.init(woSourceChart.value)
  woSourceInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 报工产量趋势图 */
function renderReportTrend(data) {
  if (!reportTrendChart.value) return
  const chartData = data.map(d => ({ date: d.date, good: Number(d.goodQty || 0), defect: Number(d.defectQty || 0) }))

  if (reportTrendInstance) reportTrendInstance.dispose()
  reportTrendInstance = echarts.init(reportTrendChart.value)
  reportTrendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['合格数', '不良数'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.date), axisLabel: { color: chartTextColor.value, rotate: chartData.length > 15 ? 30 : 0 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '合格数', type: 'bar', barWidth: '35%', data: chartData.map(d => d.good), itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] } },
      { name: '不良数', type: 'line', smooth: true, data: chartData.map(d => d.defect), itemStyle: { color: '#f56c6c' }, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245,108,108,0.3)' }, { offset: 1, color: 'rgba(245,108,108,0.02)' }]) } }
    ]
  })
}

/** 产品产量TOP10横向柱状图 */
function renderProductTop(data) {
  if (!productTopChart.value) return
  const chartData = data.slice().reverse().map(d => ({
    name: d.productName || d.productCode,
    code: d.productCode,
    qty: Number(d.finishedQty || 0)
  }))

  if (productTopInstance) productTopInstance.dispose()
  productTopInstance = echarts.init(productTopChart.value)
  productTopInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: function(params) {
      const p = params[0]
      const item = chartData[p.dataIndex]
      return `${item.name}<br/>完工数量: ${formatQty(item.qty)}`
    }},
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: chartTextColor.value }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value, width: 120, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: chartData.map(d => ({
        value: d.qty,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409effcc' },
            { offset: 1, color: '#409eff' }
          ]),
          borderRadius: [0, 6, 6, 0]
        }
      })),
      label: { show: true, position: 'right', formatter: function(p) { return formatQty(p.value) }, fontSize: 12, color: chartTextColor.value }
    }]
  })
}

/** 产能单元负荷TOP10横向柱状图 */
function renderResourceLoad(data) {
  if (!resourceLoadChart.value) return
  const chartData = data.slice().reverse().map(d => ({
    name: d.resourceName,
    planQty: Number(d.planQty || 0),
    finishedQty: Number(d.finishedQty || 0)
  }))

  if (resourceLoadInstance) resourceLoadInstance.dispose()
  resourceLoadInstance = echarts.init(resourceLoadChart.value)
  resourceLoadInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['计划数量', '完工数量'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '8%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: chartTextColor.value }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value, width: 100, overflow: 'truncate' } },
    series: [
      { name: '计划数量', type: 'bar', barWidth: '30%', data: chartData.map(d => d.planQty), itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] } },
      { name: '完工数量', type: 'bar', barWidth: '30%', data: chartData.map(d => d.finishedQty), itemStyle: { color: '#67c23a', borderRadius: [0, 4, 4, 0] } }
    ]
  })
}

/** 工单类型分布饼图 */
function renderWoType(data) {
  if (!woTypeChart.value) return
  const chartData = data.map(d => ({ name: d.name, value: d.count || 0 }))
  const colors = ['#409eff', '#e6a23c', '#909399']

  if (woTypeInstance) woTypeInstance.dispose()
  woTypeInstance = echarts.init(woTypeChart.value)
  woTypeInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 窗口自适应 */
function handleResize() {
  woStatusInstance && woStatusInstance.resize()
  woTrendInstance && woTrendInstance.resize()
  abTypeInstance && abTypeInstance.resize()
  abSeverityInstance && abSeverityInstance.resize()
  woSourceInstance && woSourceInstance.resize()
  reportTrendInstance && reportTrendInstance.resize()
  productTopInstance && productTopInstance.resize()
  resourceLoadInstance && resourceLoadInstance.resize()
  woTypeInstance && woTypeInstance.resize()
}

/** 加载所有数据 */
function loadData() {
  loading.value = true
  Promise.all([
    getOverview(),
    getWorkOrderStatusDist(),
    getWorkOrderTrend(),
    getProductTop10(),
    getAbnormalTypeDist(),
    getAbnormalSeverityDist(),
    getReportTrend(),
    getWorkOrderSourceDist(),
    getResourceLoad(),
    getWorkOrderTypeDist()
  ]).then(([ovRes, woStatusRes, woTrendRes, prodTopRes, abTypeRes, abSevRes, rptTrendRes, woSrcRes, resLoadRes, woTypeRes]) => {
    overview.value = ovRes.data || {}
    loading.value = false
    lastUpdate.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    nextTick(() => {
      renderWoStatus(woStatusRes.data || [])
      renderWoTrend(woTrendRes.data || [])
      renderAbType(abTypeRes.data || [])
      renderAbSeverity(abSevRes.data || [])
      renderWoSource(woSrcRes.data || [])
      renderReportTrend(rptTrendRes.data || [])
      renderProductTop(prodTopRes.data || [])
      renderResourceLoad(resLoadRes.data || [])
      renderWoType(woTypeRes.data || [])
    })
  }).catch(() => {
    loading.value = false
  })
  // 加载最新工单
  listWorkOrder({ pageNum: 1, pageSize: 5 }).then(response => {
    latestOrders.value = response.rows || []
  }).catch(() => {})
}

/** 监听暗色模式切换 */
watch(isDark, () => {
  if (!loading.value) {
    nextTick(() => {
      getWorkOrderStatusDist().then(res => renderWoStatus(res.data || []))
      getWorkOrderTrend().then(res => renderWoTrend(res.data || []))
      getAbnormalTypeDist().then(res => renderAbType(res.data || []))
      getAbnormalSeverityDist().then(res => renderAbSeverity(res.data || []))
      getWorkOrderSourceDist().then(res => renderWoSource(res.data || []))
      getReportTrend().then(res => renderReportTrend(res.data || []))
      getProductTop10().then(res => renderProductTop(res.data || []))
      getResourceLoad().then(res => renderResourceLoad(res.data || []))
      getWorkOrderTypeDist().then(res => renderWoType(res.data || []))
    })
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  woStatusInstance && woStatusInstance.dispose()
  woTrendInstance && woTrendInstance.dispose()
  abTypeInstance && abTypeInstance.dispose()
  abSeverityInstance && abSeverityInstance.dispose()
  woSourceInstance && woSourceInstance.dispose()
  reportTrendInstance && reportTrendInstance.dispose()
  productTopInstance && productTopInstance.dispose()
  resourceLoadInstance && resourceLoadInstance.dispose()
  woTypeInstance && woTypeInstance.dispose()
})
</script>

<style scoped>
.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.dash-header__title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.dash-header__desc {
  font-size: 13px;
  color: #909399;
  margin-left: 12px;
}
.dash-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.text-muted {
  color: #909399;
  font-size: 13px;
}

.kpi-row {
  margin-bottom: 16px;
}
.kpi-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 10px;
  color: #fff;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.kpi-card--blue { background: linear-gradient(135deg, #409eff, #66b1ff); }
.kpi-card--green { background: linear-gradient(135deg, #67c23a, #85ce61); }
.kpi-card--orange { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.kpi-card--cyan { background: linear-gradient(135deg, #13c2c2, #5cdbd3); }
.kpi-card--purple { background: linear-gradient(135deg, #722ed1, #9254de); }
.kpi-card--red { background: linear-gradient(135deg, #f56c6c, #f89898); }
.kpi-card__icon {
  margin-right: 16px;
  opacity: 0.85;
}
.kpi-card__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.kpi-card__label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

.amount-card {
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 10px;
  border-left: 4px solid;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.amount-card--blue { border-left-color: #409eff; }
.amount-card--green { border-left-color: #67c23a; }
.amount-card--orange { border-left-color: #e6a23c; }
.amount-card--cyan { border-left-color: #13c2c2; }
.amount-card__label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.amount-card__value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.chart-row {
  margin-bottom: 16px;
}
.chart-panel {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 10px;
}
.chart-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.chart-panel__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.chart-panel__desc {
  font-size: 12px;
  color: #909399;
}
.chart-panel__body {
  height: 320px;
  width: 100%;
}

/* 暗色模式 */
html.dark .amount-card {
  background: var(--el-bg-color-overlay);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
html.dark .amount-card__label {
  color: var(--el-text-color-secondary);
}
html.dark .amount-card__value {
  color: var(--el-text-color-primary);
}
html.dark .chart-panel {
  background: var(--el-bg-color-overlay);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
html.dark .chart-panel__title {
  color: var(--el-text-color-primary);
}
html.dark .dash-header__title {
  color: var(--el-text-color-primary);
}

/* 响应式 */
@media (max-width: 768px) {
  .dash-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>