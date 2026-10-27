<template>
  <div class="app-container safety-dashboard" v-loading="loading">
    <!-- 顶部标题栏 -->
    <div class="dash-header">
      <div class="dash-header__title">
        <el-icon class="dash-header__icon"><DataAnalysis /></el-icon>
        <span>安全生产驾驶舱</span>
      </div>
      <div class="dash-header__actions">
        <span class="dash-header__time" v-if="lastUpdate">数据更新于 {{ lastUpdate }}</span>
        <el-button :icon="Refresh" circle size="small" @click="loadData" />
      </div>
    </div>

    <!-- KPI 概览卡片 -->
    <div class="kpi-row">
      <div class="kpi-card kpi-card--danger">
        <div class="kpi-card__icon"><el-icon><Warning /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">隐患总数</div>
          <div class="kpi-card__value">{{ stats.hazardTotal || 0 }}</div>
          <div class="kpi-card__sub">
            闭环率 <b :class="closeRate >= 95 ? 'text-success' : closeRate >= 80 ? 'text-warning' : 'text-danger'">{{ closeRate }}%</b>
            <span v-if="(stats.hazardOverdue || 0) > 0" class="kpi-card__sub-tag">超期 {{ stats.hazardOverdue }}</span>
          </div>
        </div>
      </div>
      <div class="kpi-card kpi-card--warning">
        <div class="kpi-card__icon"><el-icon><Guide /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">风险点总数</div>
          <div class="kpi-card__value">{{ riskTotal }}</div>
          <div class="kpi-card__sub">
            <span class="risk-tag red" v-if="(stats.riskRed || 0) > 0">红 {{ stats.riskRed }}</span>
            <span class="risk-tag orange" v-if="(stats.riskOrange || 0) > 0">橙 {{ stats.riskOrange }}</span>
            <span class="risk-tag yellow" v-if="(stats.riskYellow || 0) > 0">黄 {{ stats.riskYellow }}</span>
            <span class="risk-tag blue" v-if="(stats.riskBlue || 0) > 0">蓝 {{ stats.riskBlue }}</span>
          </div>
        </div>
      </div>
      <div class="kpi-card kpi-card--primary">
        <div class="kpi-card__icon"><el-icon><Bell /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">到期提醒</div>
          <div class="kpi-card__value">{{ stats.remindPending || 0 }}</div>
          <div class="kpi-card__sub">
            待处理 <b class="text-danger">{{ stats.remindPending || 0 }}</b> · 已处理 <b class="text-success">{{ stats.remindHandled || 0 }}</b>
          </div>
        </div>
      </div>
      <div class="kpi-card kpi-card--success">
        <div class="kpi-card__icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="kpi-card__body">
          <div class="kpi-card__label">隐患闭环</div>
          <div class="kpi-card__value">{{ stats.hazardClosed || 0 }}</div>
          <div class="kpi-card__sub">
            待整改 <b class="text-warning">{{ stats.hazardPending || 0 }}</b> · 整改中 <b class="text-primary">{{ stats.hazardRectifying || 0 }}</b>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 第一行 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 隐患状态分布 - 环形饼图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><PieChart /></el-icon> 隐患状态分布</span>
            </div>
          </template>
          <div ref="hazardStatusChart" class="chart-body"></div>
        </el-card>
      </el-col>

      <!-- 风险四色分布 - 玫瑰图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><DataLine /></el-icon> 风险四色分布</span>
            </div>
          </template>
          <div ref="riskLevelChart" class="chart-body"></div>
        </el-card>
      </el-col>

      <!-- 隐患等级分布 - 饼图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><Histogram /></el-icon> 隐患等级分布</span>
            </div>
          </template>
          <div ref="hazardLevelChart" class="chart-body"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 第二行 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 隐患趋势 - 折线图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><TrendCharts /></el-icon> 隐患发现与闭环趋势（近6个月）</span>
            </div>
          </template>
          <div ref="hazardTrendChart" class="chart-body"></div>
        </el-card>
      </el-col>

      <!-- 隐患类型分布 - 柱状图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><Box /></el-icon> 隐患类型分布</span>
            </div>
          </template>
          <div ref="hazardTypeChart" class="chart-body"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 第三行 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 隐患部门分布 Top5 - 横向柱状图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><OfficeBuilding /></el-icon> 部门隐患Top5</span>
            </div>
          </template>
          <div ref="hazardDeptChart" class="chart-body"></div>
        </el-card>
      </el-col>

      <!-- 隐患来源分布 - 饼图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><Search /></el-icon> 隐患来源分布</span>
            </div>
          </template>
          <div ref="hazardSourceChart" class="chart-body"></div>
        </el-card>
      </el-col>

      <!-- 到期提醒类型分布 - 柱状图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><Bell /></el-icon> 到期提醒类型</span>
            </div>
          </template>
          <div ref="remindTypeChart" class="chart-body"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部区域 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 即将到期提醒列表 -->
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title">
                <el-icon><WarningFilled /></el-icon> 到期提醒（待处理）
                <el-badge :value="upcomingReminds.length" :hidden="upcomingReminds.length === 0" class="header-badge" />
              </span>
              <router-link to="/safety/remind" class="chart-card__link">查看全部 &rarr;</router-link>
            </div>
          </template>
          <div class="remind-list" v-if="upcomingReminds.length > 0">
            <div class="remind-item" v-for="(item, i) in upcomingReminds" :key="i">
              <div class="remind-item__icon" :class="getRemindIconClass(item.remindType)">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="remind-item__info">
                <div class="remind-item__name">{{ item.relatedName || '—' }}</div>
                <div class="remind-item__desc">{{ getRemindTypeLabel(item.remindType) }} · {{ item.remindContent || '—' }}</div>
              </div>
              <div class="remind-item__meta">
                <div class="remind-item__date" :class="{ overdue: isOverdue(item.expireDate) }">
                  {{ formatDate(item.expireDate) }}
                </div>
                <div class="remind-item__person">{{ item.toPersonName || '—' }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无到期提醒" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 快捷入口 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card quick-card">
          <template #header>
            <div class="chart-card__header">
              <span class="chart-card__title"><el-icon><Menu /></el-icon> 快捷入口</span>
            </div>
          </template>
          <div class="quick-links">
            <router-link to="/safety/dualcontrol/hazard" class="quick-link">
              <div class="quick-link__icon danger"><el-icon><Warning /></el-icon></div>
              <span>隐患管理</span>
            </router-link>
            <router-link to="/safety/dualcontrol/risk" class="quick-link">
              <div class="quick-link__icon warning"><el-icon><Guide /></el-icon></div>
              <span>风险点管理</span>
            </router-link>
            <router-link to="/safety/remind" class="quick-link">
              <div class="quick-link__icon primary"><el-icon><Bell /></el-icon></div>
              <span>到期提醒</span>
            </router-link>
            <router-link to="/safety/arch/enterprise" class="quick-link">
              <div class="quick-link__icon success"><el-icon><Files /></el-icon></div>
              <span>企业档案</span>
            </router-link>
            <router-link to="/safety/dualcontrol/task" class="quick-link">
              <div class="quick-link__icon info"><el-icon><List /></el-icon></div>
              <span>排查任务</span>
            </router-link>
            <router-link to="/safety/arch/cert" class="quick-link">
              <div class="quick-link__icon purple"><el-icon><Document /></el-icon></div>
              <span>证照管理</span>
            </router-link>
          </div>

          <!-- 指标说明 -->
          <div class="kpi-defs">
            <div class="kpi-def">
              <div class="kpi-def__tag" style="background: #f56c6c;">红</div>
              <div class="kpi-def__text">重大风险（D≥320）</div>
            </div>
            <div class="kpi-def">
              <div class="kpi-def__tag" style="background: #e6a23c;">橙</div>
              <div class="kpi-def__text">较大风险（160≤D&lt;320）</div>
            </div>
            <div class="kpi-def">
              <div class="kpi-def__tag" style="background: #f0d020;">黄</div>
              <div class="kpi-def__text">一般风险（70≤D&lt;160）</div>
            </div>
            <div class="kpi-def">
              <div class="kpi-def__tag" style="background: #409eff;">蓝</div>
              <div class="kpi-def__text">低风险（D&lt;70）</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="SafetyDashboard">
import * as echarts from 'echarts'
import { getStats, getChartData } from '@/api/safety/dashboard'
import {
  DataAnalysis, Refresh, Warning, Guide, Bell, CircleCheck, PieChart, DataLine,
  Histogram, TrendCharts, Box, OfficeBuilding, Search, WarningFilled, Menu,
  Files, List, Document
} from '@element-plus/icons-vue'

const loading = ref(true)
const stats = ref({})
const chartData = ref({})
const upcomingReminds = ref([])
const lastUpdate = ref('')

// 图表引用
const hazardStatusChart = ref(null)
const riskLevelChart = ref(null)
const hazardLevelChart = ref(null)
const hazardTrendChart = ref(null)
const hazardTypeChart = ref(null)
const hazardDeptChart = ref(null)
const hazardSourceChart = ref(null)
const remindTypeChart = ref(null)

// 图表实例
let hazardStatusInstance = null
let riskLevelInstance = null
let hazardLevelInstance = null
let hazardTrendInstance = null
let hazardTypeInstance = null
let hazardDeptInstance = null
let hazardSourceInstance = null
let remindTypeInstance = null

// 是否暗色模式
const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#606266')
const chartSplitColor = computed(() => isDark.value ? '#434343' : '#eee')

// 计算属性
const riskTotal = computed(() => {
  return (stats.value.riskRed || 0) + (stats.value.riskOrange || 0) + (stats.value.riskYellow || 0) + (stats.value.riskBlue || 0)
})

const closeRate = computed(() => {
  const total = stats.value.hazardTotal || 0
  if (!total) return 0
  return Math.round(((stats.value.hazardClosed || 0) / total) * 100)
})

// 字典映射
const hazardTypeMap = { '1': '人的不安全行为', '2': '物的不安全状态', '3': '管理缺陷', '4': '环境因素' }
const hazardLevelMap = { '1': '一般隐患', '2': '重大隐患' }
const hazardSourceMap = { '1': '随手拍', '2': '计划排查', '3': '专项排查', '4': '举报', '5': '上级交办' }
const remindTypeMap = { '1': '证照到期', '2': '特种作业证', '3': '设备检验', '4': '应急物资', '5': '隐患整改', '6': '预案修订', '7': '培训证书' }

function getRemindTypeLabel(type) {
  return remindTypeMap[type] || '其他'
}

function getRemindIconClass(type) {
  const classes = { '1': 'danger', '2': 'warning', '3': 'primary', '4': 'info', '5': 'danger', '6': 'warning', '7': 'primary' }
  return classes[type] || 'info'
}

function isOverdue(date) {
  if (!date) return false
  return new Date(date) < new Date(new Date().toDateString())
}

function formatDate(date) {
  if (!date) return '—'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** 加载数据 */
function loadData() {
  loading.value = true
  let statsLoaded = false
  let chartLoaded = false

  function checkAllLoaded() {
    if (statsLoaded && chartLoaded) {
      lastUpdate.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
      loading.value = false
      nextTick(() => {
        renderAllCharts()
      })
    }
  }

  // 独立加载统计数据（失败不影响图表数据加载）
  getStats().then(res => {
    stats.value = res.data || {}
  }).catch(() => {}).finally(() => {
    statsLoaded = true
    checkAllLoaded()
  })

  // 独立加载图表数据
  getChartData().then(res => {
    chartData.value = res.data || {}
    upcomingReminds.value = chartData.value.upcomingReminds || []
  }).catch(() => {}).finally(() => {
    chartLoaded = true
    checkAllLoaded()
  })
}

/** 隐患状态分布 - 环形饼图 */
function renderHazardStatusChart() {
  if (!hazardStatusChart.value) return
  if (hazardStatusInstance) hazardStatusInstance.dispose()
  hazardStatusInstance = echarts.init(hazardStatusChart.value)
  const data = [
    { name: '待整改', value: stats.value.hazardPending || 0, itemStyle: { color: '#e6a23c' } },
    { name: '整改中', value: stats.value.hazardRectifying || 0, itemStyle: { color: '#409eff' } },
    { name: '待验收', value: stats.value.hazardVerifying || 0, itemStyle: { color: '#722ed1' } },
    { name: '已闭环', value: stats.value.hazardClosed || 0, itemStyle: { color: '#67c23a' } },
    { name: '超期', value: stats.value.hazardOverdue || 0, itemStyle: { color: '#f56c6c' } }
  ].filter(d => d.value > 0)
  hazardStatusInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.length > 0 ? data : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 风险四色分布 - 玫瑰图 */
function renderRiskLevelChart() {
  if (!riskLevelChart.value) return
  if (riskLevelInstance) riskLevelInstance.dispose()
  riskLevelInstance = echarts.init(riskLevelChart.value)
  const data = [
    { name: '重大风险', value: stats.value.riskRed || 0, itemStyle: { color: '#f56c6c' } },
    { name: '较大风险', value: stats.value.riskOrange || 0, itemStyle: { color: '#e6a23c' } },
    { name: '一般风险', value: stats.value.riskYellow || 0, itemStyle: { color: '#f0d020' } },
    { name: '低风险', value: stats.value.riskBlue || 0, itemStyle: { color: '#409eff' } }
  ]
  riskLevelInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: ['20%', '70%'],
      center: ['50%', '42%'],
      roseType: 'area',
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}', fontSize: 11, color: chartTextColor.value },
      data: data.some(d => d.value > 0) ? data.filter(d => d.value > 0) : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 隐患等级分布 - 饼图 */
function renderHazardLevelChart() {
  if (!hazardLevelChart.value) return
  if (hazardLevelInstance) hazardLevelInstance.dispose()
  hazardLevelInstance = echarts.init(hazardLevelChart.value)
  const rawData = chartData.value.hazardByLevel || []
  const data = rawData.map(d => ({
    name: hazardLevelMap[d.hazardLevel] || '未知',
    value: d.count,
    itemStyle: { color: d.hazardLevel === '2' ? '#f56c6c' : '#e6a23c' }
  })).filter(d => d.value > 0)
  hazardLevelInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: '65%',
      center: ['50%', '42%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}', fontSize: 12, color: chartTextColor.value },
      emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
      data: data.length > 0 ? data : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 隐患趋势 - 折线图 */
function renderHazardTrendChart() {
  if (!hazardTrendChart.value) return
  if (hazardTrendInstance) hazardTrendInstance.dispose()
  hazardTrendInstance = echarts.init(hazardTrendChart.value)
  const data = chartData.value.hazardTrend || []
  hazardTrendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['发现总数', '一般隐患', '重大隐患', '已闭环'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.map(d => d.month), axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '发现总数', type: 'line', smooth: true, data: data.map(d => d.total), itemStyle: { color: '#409eff' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }]) } },
      { name: '一般隐患', type: 'line', smooth: true, data: data.map(d => d.general), itemStyle: { color: '#e6a23c' } },
      { name: '重大隐患', type: 'line', smooth: true, data: data.map(d => d.major), itemStyle: { color: '#f56c6c' } },
      { name: '已闭环', type: 'line', smooth: true, data: data.map(d => d.closed), itemStyle: { color: '#67c23a' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103,194,58,0.3)' }, { offset: 1, color: 'rgba(103,194,58,0.02)' }]) } }
    ]
  })
}

/** 隐患类型分布 - 柱状图 */
function renderHazardTypeChart() {
  if (!hazardTypeChart.value) return
  if (hazardTypeInstance) hazardTypeInstance.dispose()
  hazardTypeInstance = echarts.init(hazardTypeChart.value)
  const rawData = chartData.value.hazardByType || []
  const data = rawData.map(d => ({ name: hazardTypeMap[d.hazardType] || '未知', value: d.count }))
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']
  hazardTypeInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { color: chartTextColor.value, rotate: data.length > 4 ? 20 : 0 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '45%',
      data: data.map((d, i) => ({
        value: d.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[i % colors.length] },
            { offset: 1, color: colors[i % colors.length] + '88' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      label: { show: true, position: 'top', fontSize: 12, color: chartTextColor.value }
    }]
  })
}

/** 隐患部门分布 Top5 - 横向柱状图 */
function renderHazardDeptChart() {
  if (!hazardDeptChart.value) return
  if (hazardDeptInstance) hazardDeptInstance.dispose()
  hazardDeptInstance = echarts.init(hazardDeptChart.value)
  const rawData = (chartData.value.hazardByDept || []).slice().reverse()
  hazardDeptInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'category', data: rawData.map(d => d.deptName), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: rawData.map(d => ({
        value: d.count,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#f56c6ccc' },
            { offset: 1, color: '#f56c6c' }
          ]),
          borderRadius: [0, 6, 6, 0]
        }
      })),
      label: { show: true, position: 'right', fontSize: 12, color: chartTextColor.value }
    }]
  })
}

/** 隐患来源分布 - 饼图 */
function renderHazardSourceChart() {
  if (!hazardSourceChart.value) return
  if (hazardSourceInstance) hazardSourceInstance.dispose()
  hazardSourceInstance = echarts.init(hazardSourceChart.value)
  const rawData = chartData.value.hazardBySource || []
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#722ed1']
  const data = rawData.map((d, i) => ({
    name: hazardSourceMap[d.sourceType] || '未知',
    value: d.count,
    itemStyle: { color: colors[i % colors.length] }
  })).filter(d => d.value > 0)
  hazardSourceInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.length > 0 ? data : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 到期提醒类型分布 - 柱状图 */
function renderRemindTypeChart() {
  if (!remindTypeChart.value) return
  if (remindTypeInstance) remindTypeInstance.dispose()
  remindTypeInstance = echarts.init(remindTypeChart.value)
  const rawData = chartData.value.remindByType || []
  const data = rawData.map(d => ({ name: remindTypeMap[d.remindType] || '其他', value: d.count }))
  const colors = ['#409eff', '#e6a23c', '#13c2c2', '#722ed1', '#f56c6c', '#fa8c16', '#52c41a']
  remindTypeInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { color: chartTextColor.value, rotate: data.length > 4 ? 20 : 0 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '45%',
      data: data.map((d, i) => ({
        value: d.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[i % colors.length] },
            { offset: 1, color: colors[i % colors.length] + '88' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      label: { show: true, position: 'top', fontSize: 12, color: chartTextColor.value }
    }]
  })
}

/** 渲染所有图表 */
function renderAllCharts() {
  renderHazardStatusChart()
  renderRiskLevelChart()
  renderHazardLevelChart()
  renderHazardTrendChart()
  renderHazardTypeChart()
  renderHazardDeptChart()
  renderHazardSourceChart()
  renderRemindTypeChart()
}

/** 窗口resize */
function handleResize() {
  hazardStatusInstance && hazardStatusInstance.resize()
  riskLevelInstance && riskLevelInstance.resize()
  hazardLevelInstance && hazardLevelInstance.resize()
  hazardTrendInstance && hazardTrendInstance.resize()
  hazardTypeInstance && hazardTypeInstance.resize()
  hazardDeptInstance && hazardDeptInstance.resize()
  hazardSourceInstance && hazardSourceInstance.resize()
  remindTypeInstance && remindTypeInstance.resize()
}

/** 监听暗色模式切换 */
watch(isDark, () => {
  if (!loading.value) {
    nextTick(() => renderAllCharts())
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  hazardStatusInstance && hazardStatusInstance.dispose()
  riskLevelInstance && riskLevelInstance.dispose()
  hazardLevelInstance && hazardLevelInstance.dispose()
  hazardTrendInstance && hazardTrendInstance.dispose()
  hazardTypeInstance && hazardTypeInstance.dispose()
  hazardDeptInstance && hazardDeptInstance.dispose()
  hazardSourceInstance && hazardSourceInstance.dispose()
  remindTypeInstance && remindTypeInstance.dispose()
})
</script>

<style scoped lang="scss">
.safety-dashboard {
  .dash-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 4px;
    &__title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 700;
      color: #303133;
    }
    &__icon {
      font-size: 24px;
      color: #409eff;
    }
    &__time {
      font-size: 13px;
      color: #909399;
      margin-right: 8px;
    }
  }

  .kpi-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 16px;
  }

  .kpi-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: all 0.3s;
    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    &__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56px;
      height: 56px;
      border-radius: 12px;
      font-size: 28px;
      color: #fff;
      flex-shrink: 0;
    }
    &__label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 4px;
    }
    &__value {
      font-size: 32px;
      font-weight: 700;
      color: #303133;
      line-height: 1.2;
    }
    &__sub {
      font-size: 12px;
      color: #909399;
      margin-top: 6px;
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
      &-tag {
        background: #fef0f0;
        color: #f56c6c;
        padding: 1px 8px;
        border-radius: 10px;
        font-size: 11px;
        font-weight: 600;
      }
    }
    &--danger .kpi-card__icon { background: linear-gradient(135deg, #f56c6c, #f89898); }
    &--danger { border-left: 4px solid #f56c6c; }
    &--warning .kpi-card__icon { background: linear-gradient(135deg, #e6a23c, #f0d020); }
    &--warning { border-left: 4px solid #e6a23c; }
    &--primary .kpi-card__icon { background: linear-gradient(135deg, #409eff, #79bbff); }
    &--primary { border-left: 4px solid #409eff; }
    &--success .kpi-card__icon { background: linear-gradient(135deg, #67c23a, #95d475); }
    &--success { border-left: 4px solid #67c23a; }
  }

  .risk-tag {
    display: inline-block;
    padding: 1px 8px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 600;
    &.red { background: #fef0f0; color: #f56c6c; }
    &.orange { background: #fdf6ec; color: #e6a23c; }
    &.yellow { background: #fdfdfd; color: #f0d020; }
    &.blue { background: #ecf5ff; color: #409eff; }
  }

  .chart-row {
    margin-bottom: 16px;
  }

  .chart-card {
    height: 100%;
    :deep(.el-card__header) {
      padding: 12px 16px;
    }
    :deep(.el-card__body) {
      padding: 12px 16px;
    }
    &__header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    &__title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
    &__link {
      font-size: 13px;
      color: #409eff;
      text-decoration: none;
      &:hover { opacity: 0.8; }
    }
  }

  .chart-body {
    height: 280px;
    width: 100%;
  }

  .header-badge {
    margin-left: 8px;
  }

  // 提醒列表
  .remind-list {
    max-height: 320px;
    overflow-y: auto;
  }
  .remind-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;
    &:last-child { border-bottom: none; }
    &__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      font-size: 18px;
      color: #fff;
      flex-shrink: 0;
      &.danger { background: #f56c6c; }
      &.warning { background: #e6a23c; }
      &.primary { background: #409eff; }
      &.info { background: #909399; }
    }
    &__info {
      flex: 1;
      min-width: 0;
    }
    &__name {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    &__desc {
      font-size: 12px;
      color: #909399;
      margin-top: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    &__meta {
      text-align: right;
      flex-shrink: 0;
    }
    &__date {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      &.overdue { color: #f56c6c; }
    }
    &__person {
      font-size: 12px;
      color: #909399;
      margin-top: 2px;
    }
  }

  // 快捷入口
  .quick-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
  .quick-links {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }
  .quick-link {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    border-radius: 10px;
    background: #f5f7fa;
    color: #606266;
    text-decoration: none;
    transition: all 0.2s;
    &:hover {
      background: #ecf5ff;
      color: #409eff;
      transform: translateY(-2px);
    }
    &__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 10px;
      font-size: 20px;
      color: #fff;
      &.danger { background: #f56c6c; }
      &.warning { background: #e6a23c; }
      &.primary { background: #409eff; }
      &.success { background: #67c23a; }
      &.info { background: #909399; }
      &.purple { background: #722ed1; }
    }
    span {
      font-size: 13px;
      font-weight: 500;
    }
  }

  // 指标说明
  .kpi-defs {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
  .kpi-def {
    display: flex;
    align-items: center;
    gap: 8px;
    &__tag {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 20px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      flex-shrink: 0;
    }
    &__text {
      font-size: 12px;
      color: #606266;
    }
  }

  // 通用文字颜色
  .text-success { color: #67c23a; }
  .text-warning { color: #e6a23c; }
  .text-danger { color: #f56c6c; }
  .text-primary { color: #409eff; }
  .text-muted { color: #c0c4cc; }
}

// 暗色模式适配
.dark .safety-dashboard {
  .kpi-card {
    background: #1d1e1f;
    &__value { color: #e0e0e0; }
    &__label { color: #a0a0a0; }
    &__sub { color: #a0a0a0; }
  }
  .chart-card__title { color: #e0e0e0; }
  .quick-link {
    background: #262626;
    color: #a0a0a0;
    &:hover { background: #1a3a5c; color: #409eff; }
  }
  .remind-item {
    border-color: #333;
    &__name { color: #e0e0e0; }
    &__desc { color: #a0a0a0; }
    &__date { color: #e0e0e0; }
  }
  .kpi-def__text { color: #a0a0a0; }
  .kpi-defs { border-color: #333; }
}
</style>
