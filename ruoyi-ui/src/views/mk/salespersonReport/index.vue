<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="report-header">
      <div>
        <span class="report-header__title">销售人员分析报表</span>
        <span class="report-header__desc">多维度分析销售人员业绩、排名与转化情况</span>
      </div>
      <div class="report-header__right">
        <el-select v-model="sortBy" placeholder="排序方式" size="small" style="width: 160px" @change="handleSortChange">
          <el-option label="合同金额" value="contractAmount" />
          <el-option label="订单金额" value="orderAmount" />
          <el-option label="回款金额" value="paymentAmount" />
          <el-option label="赢单金额" value="wonAmount" />
          <el-option label="客户数量" value="customerCount" />
          <el-option label="转化率" value="conversionRate" />
        </el-select>
        <el-button type="primary" icon="Refresh" size="small" @click="loadData" :loading="loading">刷新数据</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- KPI 概览卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--blue">
            <div class="kpi-card__icon"><el-icon size="28"><User /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ summary.totalPersons }}</div>
              <div class="kpi-card__label">销售人数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--green">
            <div class="kpi-card__icon"><el-icon size="28"><Document /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">¥{{ formatWan(summary.totalContractAmount) }}</div>
              <div class="kpi-card__label">合同总额</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--orange">
            <div class="kpi-card__icon"><el-icon size="28"><ShoppingCart /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">¥{{ formatWan(summary.totalOrderAmount) }}</div>
              <div class="kpi-card__label">订单总额</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--cyan">
            <div class="kpi-card__icon"><el-icon size="28"><Money /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">¥{{ formatWan(summary.totalPaymentAmount) }}</div>
              <div class="kpi-card__label">回款总额</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--purple">
            <div class="kpi-card__icon"><el-icon size="28"><TrendCharts /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">¥{{ formatWan(summary.totalWonAmount) }}</div>
              <div class="kpi-card__label">赢单总额</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--red">
            <div class="kpi-card__icon"><el-icon size="28"><Histogram /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ summary.avgConversionRate }}%</div>
              <div class="kpi-card__label">平均转化率</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第一行：销售排名 + 雷达图 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="14">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">销售业绩排名</span>
              <span class="chart-panel__desc">按{{ sortLabel }}排名</span>
            </div>
            <div ref="rankChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">TOP5 综合能力雷达图</span>
              <span class="chart-panel__desc">多维度对比分析</span>
            </div>
            <div ref="radarChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第二行：月度趋势 + 赢单输单 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="14">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">销售人员月度业绩趋势</span>
              <div class="chart-panel__select">
                <el-select v-model="selectedUserId" placeholder="选择销售人员" size="small" style="width: 160px" @change="loadTrendData">
                  <el-option v-for="item in tableData" :key="item.userId" :label="item.userName" :value="item.userId" />
                </el-select>
              </div>
            </div>
            <div ref="trendChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">赢单输单分析</span>
              <span class="chart-panel__desc">各销售人员赢单/输单对比</span>
            </div>
            <div ref="winLossChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第三行：客户/商机分布 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">客户资源分布</span>
              <span class="chart-panel__desc">各销售人员客户数占比</span>
            </div>
            <div ref="customerChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">商机转化漏斗</span>
              <span class="chart-panel__desc">线索→商机→赢单转化</span>
            </div>
            <div ref="funnelChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 销售排名表格 -->
      <div class="chart-panel" style="margin-top: 16px">
        <div class="chart-panel__head">
          <span class="chart-panel__title">销售排名明细表</span>
          <span class="chart-panel__desc">共 {{ tableData.length }} 位销售人员</span>
        </div>
        <div class="chart-panel__body" style="height: auto; padding: 0;">
          <el-table :data="sortedTableData" border stripe size="default" :default-sort="{ prop: sortBy, order: 'descending' }">
            <el-table-column label="排名" width="70" align="center" fixed>
              <template #default="scope">
                <div class="rank-badge" :class="getRankClass(scope.$index + 1)">
                  {{ scope.$index + 1 }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="销售人员" prop="userName" width="120" fixed show-overflow-tooltip />
            <el-table-column label="部门" prop="deptName" width="120" show-overflow-tooltip />
            <el-table-column label="客户数" prop="customerCount" width="80" align="center" sortable />
            <el-table-column label="线索数" prop="leadCount" width="80" align="center" sortable />
            <el-table-column label="商机数" prop="opportunityCount" width="80" align="center" sortable />
            <el-table-column label="合同数" prop="contractCount" width="80" align="center" sortable />
            <el-table-column label="订单数" prop="orderCount" width="80" align="center" sortable />
            <el-table-column label="合同金额" prop="contractAmount" width="130" align="right" sortable>
              <template #default="scope">
                <span class="amount-text">¥{{ formatAmount(scope.row.contractAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="订单金额" prop="orderAmount" width="130" align="right" sortable>
              <template #default="scope">
                <span class="amount-text">¥{{ formatAmount(scope.row.orderAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="回款金额" prop="paymentAmount" width="130" align="right" sortable>
              <template #default="scope">
                <span class="amount-text amount-text--green">¥{{ formatAmount(scope.row.paymentAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="赢单金额" prop="wonAmount" width="130" align="right" sortable>
              <template #default="scope">
                <span class="amount-text amount-text--orange">¥{{ formatAmount(scope.row.wonAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="赢单/输单" width="100" align="center">
              <template #default="scope">
                <span style="color: #67c23a; font-weight: 600;">{{ scope.row.wonCount || 0 }}</span>
                <span style="color: #909399;"> / </span>
                <span style="color: #f56c6c; font-weight: 600;">{{ scope.row.lostCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="转化率" prop="conversionRate" width="100" align="center" sortable>
              <template #default="scope">
                <el-progress :percentage="Number(scope.row.conversionRate || 0)" :stroke-width="14" :text-inside="true" :color="getProgressColor(scope.row.conversionRate)" />
              </template>
            </el-table-column>
            <el-table-column label="平均成交" prop="avgDealAmount" width="120" align="right" sortable>
              <template #default="scope">
                <span class="amount-text">¥{{ formatWan(scope.row.avgDealAmount) }}万</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="MkSalespersonReport">
import * as echarts from 'echarts'
import { User, Document, ShoppingCart, Money, TrendCharts, Histogram } from '@element-plus/icons-vue'
import { getSalespersonOverview, getSalespersonTrend } from '@/api/mk/salespersonReport'

const loading = ref(true)
const tableData = ref([])
const sortBy = ref('contractAmount')
const selectedUserId = ref(null)

// 汇总数据
const summary = ref({
  totalPersons: 0,
  totalContractAmount: 0,
  totalOrderAmount: 0,
  totalPaymentAmount: 0,
  totalWonAmount: 0,
  avgConversionRate: 0
})

// 图表引用
const rankChart = ref(null)
const radarChart = ref(null)
const trendChart = ref(null)
const winLossChart = ref(null)
const customerChart = ref(null)
const funnelChart = ref(null)

// 图表实例
let rankInstance = null
let radarInstance = null
let trendInstance = null
let winLossInstance = null
let customerInstance = null
let funnelInstance = null

// 主题色
const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#606266')
const chartSplitColor = computed(() => isDark.value ? '#434343' : '#eee')

// 排序标签
const sortLabel = computed(() => {
  const map = { contractAmount: '合同金额', orderAmount: '订单金额', paymentAmount: '回款金额', wonAmount: '赢单金额', customerCount: '客户数量', conversionRate: '转化率' }
  return map[sortBy.value] || '合同金额'
})

// 排序后的表格数据
const sortedTableData = computed(() => {
  const data = [...tableData.value]
  const key = sortBy.value
  data.sort((a, b) => {
    let va = a[key] || 0
    let vb = b[key] || 0
    if (typeof va === 'string') va = Number(va) || 0
    if (typeof vb === 'string') vb = Number(vb) || 0
    return vb - va
  })
  return data
})

/** 金额格式化 */
function formatAmount(val) {
  if (!val) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 万元格式化 */
function formatWan(val) {
  if (!val) return '0.00'
  return (Number(val) / 10000).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 排名样式 */
function getRankClass(rank) {
  if (rank === 1) return 'rank-badge--gold'
  if (rank === 2) return 'rank-badge--silver'
  if (rank === 3) return 'rank-badge--bronze'
  return ''
}

/** 进度条颜色 */
function getProgressColor(rate) {
  const r = Number(rate) || 0
  if (r >= 70) return '#67c23a'
  if (r >= 40) return '#e6a23c'
  return '#f56c6c'
}

/** 计算汇总 */
function calcSummary(data) {
  const s = {
    totalPersons: data.length,
    totalContractAmount: 0,
    totalOrderAmount: 0,
    totalPaymentAmount: 0,
    totalWonAmount: 0,
    avgConversionRate: 0
  }
  let totalRate = 0
  data.forEach(d => {
    s.totalContractAmount += Number(d.contractAmount || 0)
    s.totalOrderAmount += Number(d.orderAmount || 0)
    s.totalPaymentAmount += Number(d.paymentAmount || 0)
    s.totalWonAmount += Number(d.wonAmount || 0)
    totalRate += Number(d.conversionRate || 0)
  })
  s.avgConversionRate = data.length > 0 ? (totalRate / data.length).toFixed(1) : 0
  summary.value = s
}

/** 销售业绩排名图 */
function renderRank(data) {
  if (!rankChart.value) return
  const chartData = data.slice().reverse().map(d => ({
    name: d.userName || '未知',
    value: Number(d[sortBy.value] || 0)
  }))

  if (rankInstance) rankInstance.dispose()
  rankInstance = echarts.init(rankChart.value)
  rankInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: function(params) {
      const p = params[0]
      return `${p.name}<br/>${sortLabel.value}: ¥${formatAmount(p.value)}`
    }},
    grid: { left: '3%', right: '12%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: chartTextColor.value, formatter: function(val) { return '¥' + (val / 10000).toFixed(0) + '万' } }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: chartData.map((d, i) => {
        const colors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#13c2c2', '#722ed1']
        const colorIdx = chartData.length - 1 - i
        const color = colorIdx < 3 ? ['#f56c6c', '#e6a23c', '#409eff'][colorIdx] : '#409eff'
        return {
          value: d.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: color + 'aa' },
              { offset: 1, color: color }
            ]),
            borderRadius: [0, 6, 6, 0]
          }
        }
      }),
      label: { show: true, position: 'right', formatter: function(p) { return '¥' + formatWan(p.value) + '万' }, fontSize: 11, color: chartTextColor.value }
    }]
  })
}

/** 雷达图 */
function renderRadar(data) {
  if (!radarChart.value) return
  const top5 = data.slice(0, 5)
  if (top5.length === 0) return

  // 计算各维度最大值用于归一化
  const maxValues = {
    customer: Math.max(...top5.map(d => Number(d.customerCount || 0)), 1),
    lead: Math.max(...top5.map(d => Number(d.leadCount || 0)), 1),
    opportunity: Math.max(...top5.map(d => Number(d.opportunityCount || 0)), 1),
    contract: Math.max(...top5.map(d => Number(d.contractCount || 0)), 1),
    amount: Math.max(...top5.map(d => Number(d.contractAmount || 0)), 1),
    rate: 100
  }

  if (radarInstance) radarInstance.dispose()
  radarInstance = echarts.init(radarChart.value)
  radarInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    radar: {
      indicator: [
        { name: '客户', max: maxValues.customer },
        { name: '线索', max: maxValues.lead },
        { name: '商机', max: maxValues.opportunity },
        { name: '合同', max: maxValues.contract },
        { name: '金额(万)', max: maxValues.amount / 10000 },
        { name: '转化率%', max: maxValues.rate }
      ],
      shape: 'polygon',
      splitNumber: 4,
      axisName: { color: chartTextColor.value, fontSize: 12 },
      splitLine: { lineStyle: { color: chartSplitColor.value } },
      splitArea: { show: true, areaStyle: { color: ['rgba(64,158,255,0.05)', 'rgba(64,158,255,0.02)'] } },
      axisLine: { lineStyle: { color: chartSplitColor.value } }
    },
    series: [{
      type: 'radar',
      emphasis: { areaStyle: { color: 'rgba(64,158,255,0.3)' } },
      data: top5.map((d, i) => ({
        name: d.userName || '未知',
        value: [
          Number(d.customerCount || 0),
          Number(d.leadCount || 0),
          Number(d.opportunityCount || 0),
          Number(d.contractCount || 0),
          Number(d.contractAmount || 0) / 10000,
          Number(d.conversionRate || 0)
        ],
        areaStyle: { color: ['rgba(64,158,255,0.2)', 'rgba(103,194,58,0.2)', 'rgba(230,162,60,0.2)', 'rgba(245,108,108,0.2)', 'rgba(114,46,209,0.2)'][i] },
        lineStyle: { color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#722ed1'][i], width: 2 },
        itemStyle: { color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#722ed1'][i] }
      }))
    }]
  })
}

/** 赢单输单分析图 */
function renderWinLoss(data) {
  if (!winLossChart.value) return
  const chartData = data.slice(0, 10).map(d => ({
    name: d.userName || '未知',
    won: Number(d.wonCount || 0),
    lost: Number(d.lostCount || 0)
  }))

  if (winLossInstance) winLossInstance.dispose()
  winLossInstance = echarts.init(winLossChart.value)
  winLossInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['赢单', '输单'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.name), axisLabel: { color: chartTextColor.value, rotate: chartData.length > 5 ? 30 : 0 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '赢单', type: 'bar', barWidth: '35%', data: chartData.map(d => d.won), itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] } },
      { name: '输单', type: 'bar', barWidth: '35%', data: chartData.map(d => d.lost), itemStyle: { color: '#f56c6c', borderRadius: [4, 4, 0, 0] } }
    ]
  })
}

/** 客户资源分布饼图 */
function renderCustomer(data) {
  if (!customerChart.value) return
  const chartData = data.map(d => ({ name: d.userName || '未知', value: Number(d.customerCount || 0) })).filter(d => d.value > 0)

  if (customerInstance) customerInstance.dispose()
  customerInstance = echarts.init(customerChart.value)
  customerInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}家 ({d}%)' },
    legend: { type: 'scroll', bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#13c2c2', '#722ed1', '#909399', '#fa8c16', '#a52a2a', '#5cdbd3'],
    series: [{
      type: 'pie',
      radius: ['35%', '60%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 商机转化漏斗图 */
function renderFunnel(data) {
  if (!funnelChart.value) return
  const totalLead = data.reduce((sum, d) => sum + Number(d.leadCount || 0), 0)
  const totalOpp = data.reduce((sum, d) => sum + Number(d.opportunityCount || 0), 0)
  const totalWon = data.reduce((sum, d) => sum + Number(d.wonCount || 0), 0)
  const totalContract = data.reduce((sum, d) => sum + Number(d.contractCount || 0), 0)

  if (funnelInstance) funnelInstance.dispose()
  funnelInstance = echarts.init(funnelChart.value)
  funnelInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    series: [{
      type: 'funnel',
      left: '10%',
      right: '10%',
      top: 10,
      bottom: 10,
      minSize: '20%',
      maxSize: '100%',
      sort: 'descending',
      gap: 2,
      label: { show: true, position: 'inside', color: '#fff', fontWeight: 'bold' },
      labelLine: { length: 10, lineStyle: { width: 1 } },
      itemStyle: { borderColor: '#fff', borderWidth: 1 },
      emphasis: { label: { fontSize: 16 } },
      data: [
        { name: '线索', value: totalLead, itemStyle: { color: '#409eff' } },
        { name: '商机', value: totalOpp, itemStyle: { color: '#e6a23c' } },
        { name: '赢单', value: totalWon, itemStyle: { color: '#67c23a' } },
        { name: '签约', value: totalContract, itemStyle: { color: '#f56c6c' } }
      ].filter(d => d.value > 0)
    }]
  })
}

/** 月度趋势图 */
function renderTrend(data) {
  if (!trendChart.value) return
  const chartData = data.map(d => ({
    date: d.date,
    contractCount: Number(d.contractCount || 0),
    contractAmount: Number(d.contractAmount || 0),
    orderAmount: Number(d.orderAmount || 0),
    paymentAmount: Number(d.paymentAmount || 0)
  }))

  if (trendInstance) trendInstance.dispose()
  trendInstance = echarts.init(trendChart.value)
  trendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['合同金额', '订单金额', '回款金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.date), axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'value', name: '金额', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value, formatter: function(val) { return (val / 10000).toFixed(0) + '万' } } },
    series: [
      { name: '合同金额', type: 'bar', barWidth: '25%', data: chartData.map(d => d.contractAmount), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '订单金额', type: 'bar', barWidth: '25%', data: chartData.map(d => d.orderAmount), itemStyle: { color: '#e6a23c', borderRadius: [4, 4, 0, 0] } },
      { name: '回款金额', type: 'line', smooth: true, data: chartData.map(d => d.paymentAmount), itemStyle: { color: '#67c23a' }, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103,194,58,0.3)' }, { offset: 1, color: 'rgba(103,194,58,0.02)' }]) } }
    ]
  })
}

/** 加载月度趋势数据 */
function loadTrendData(userId) {
  if (!userId) return
  getSalespersonTrend(userId).then(res => {
    nextTick(() => renderTrend(res.data || []))
  }).catch(() => {})
}

/** 窗口自适应 */
function handleResize() {
  rankInstance && rankInstance.resize()
  radarInstance && radarInstance.resize()
  trendInstance && trendInstance.resize()
  winLossInstance && winLossInstance.resize()
  customerInstance && customerInstance.resize()
  funnelInstance && funnelInstance.resize()
}

/** 排序变化 */
function handleSortChange() {
  nextTick(() => {
    renderRank(sortedTableData.value)
  })
}

/** 加载所有数据 */
function loadData() {
  loading.value = true
  getSalespersonOverview().then(res => {
    const data = res.data || []
    tableData.value = data
    loading.value = false
    calcSummary(data)
    // 默认选中第一个销售人员
    if (data.length > 0 && !selectedUserId.value) {
      selectedUserId.value = data[0].userId
    }
    nextTick(() => {
      renderRank(sortedTableData.value)
      renderRadar(data)
      renderWinLoss(data)
      renderCustomer(data)
      renderFunnel(data)
      if (selectedUserId.value) {
        loadTrendData(selectedUserId.value)
      }
    })
  }).catch(() => {
    loading.value = false
  })
}

/** 监听暗色模式切换 */
watch(isDark, () => {
  if (!loading.value && tableData.value.length > 0) {
    nextTick(() => {
      renderRank(sortedTableData.value)
      renderRadar(tableData.value)
      renderWinLoss(tableData.value)
      renderCustomer(tableData.value)
      renderFunnel(tableData.value)
      if (selectedUserId.value) loadTrendData(selectedUserId.value)
    })
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  rankInstance && rankInstance.dispose()
  radarInstance && radarInstance.dispose()
  trendInstance && trendInstance.dispose()
  winLossInstance && winLossInstance.dispose()
  customerInstance && customerInstance.dispose()
  funnelInstance && funnelInstance.dispose()
})
</script>

<style scoped>
.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.report-header__title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.report-header__desc {
  font-size: 13px;
  color: #909399;
  margin-left: 12px;
}
.report-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
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
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}
.kpi-card__label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
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
  height: 340px;
  width: 100%;
}

/* 排名徽章 */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-weight: 700;
  font-size: 13px;
  color: #909399;
  background: #f0f0f0;
}
.rank-badge--gold {
  color: #fff;
  background: linear-gradient(135deg, #faad14, #ffc53d);
  box-shadow: 0 2px 6px rgba(250, 173, 20, 0.4);
}
.rank-badge--silver {
  color: #fff;
  background: linear-gradient(135deg, #8c8c8c, #bfbfbf);
  box-shadow: 0 2px 6px rgba(140, 140, 140, 0.4);
}
.rank-badge--bronze {
  color: #fff;
  background: linear-gradient(135deg, #d48806, #faad14);
  box-shadow: 0 2px 6px rgba(212, 136, 6, 0.4);
}

/* 金额文本 */
.amount-text {
  font-weight: 600;
  color: #303133;
}
.amount-text--green {
  color: #67c23a;
}
.amount-text--orange {
  color: #e6a23c;
}

/* 暗色模式 */
html.dark .chart-panel {
  background: var(--el-bg-color-overlay);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
html.dark .chart-panel__title {
  color: var(--el-text-color-primary);
}
html.dark .report-header__title {
  color: var(--el-text-color-primary);
}
html.dark .amount-text {
  color: var(--el-text-color-primary);
}
html.dark .rank-badge {
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
}

/* 响应式 */
@media (max-width: 768px) {
  .report-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  .kpi-card__value {
    font-size: 20px;
  }
}
</style>
