<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="dash-header">
      <div>
        <span class="dash-header__title">营销管理看板</span>
        <span class="dash-header__desc">实时监控营销全流程核心指标</span>
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
            <div class="kpi-card__icon"><el-icon size="28"><User /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.customerCount || 0 }}</div>
              <div class="kpi-card__label">客户总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--green">
            <div class="kpi-card__icon"><el-icon size="28"><Connection /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.leadCount || 0 }}</div>
              <div class="kpi-card__label">线索总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--orange">
            <div class="kpi-card__icon"><el-icon size="28"><TrendCharts /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.opportunityCount || 0 }}</div>
              <div class="kpi-card__label">商机总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--cyan">
            <div class="kpi-card__icon"><el-icon size="28"><Document /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.contractCount || 0 }}</div>
              <div class="kpi-card__label">合同总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--purple">
            <div class="kpi-card__icon"><el-icon size="28"><ShoppingCart /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.orderCount || 0 }}</div>
              <div class="kpi-card__label">订单总数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4" :lg="4">
          <div class="kpi-card kpi-card--red">
            <div class="kpi-card__icon"><el-icon size="28"><Promotion /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ overview.activityCount || 0 }}</div>
              <div class="kpi-card__label">活动总数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 金额统计卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--blue">
            <div class="amount-card__label">合同总金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(overview.contractAmount) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--green">
            <div class="amount-card__label">订单总金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(overview.orderAmount) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--orange">
            <div class="amount-card__label">已回款金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(overview.paymentAmount) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="6">
          <div class="amount-card amount-card--cyan">
            <div class="amount-card__label">商机预计金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(overview.expectedAmount) }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第一行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">线索转化漏斗</span>
              <span class="chart-panel__desc">按线索状态统计</span>
            </div>
            <div ref="leadFunnelChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">商机销售管线</span>
              <span class="chart-panel__desc">按阶段统计商机数量与金额</span>
            </div>
            <div ref="oppPipelineChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第二行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">客户行业分布</span>
              <span class="chart-panel__desc">按行业统计客户数</span>
            </div>
            <div ref="industryChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">客户等级分布</span>
              <span class="chart-panel__desc">按等级统计客户数</span>
            </div>
            <div ref="levelChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">活动效果统计</span>
              <span class="chart-panel__desc">按活动类型统计线索与成交</span>
            </div>
            <div ref="activityChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- P2-01: 客户类型维度分析 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">客户类型销售构成</span>
              <span class="chart-panel__desc">企业/经销商/个人：销售额、订单数、客单价与占比</span>
            </div>
            <div ref="custTypeChart" class="chart-panel__body chart-panel__body--pie"></div>
            <el-table :data="custTypeStats" border size="small" class="cust-type-table">
              <el-table-column label="客户类型" prop="typeName" width="90" />
              <el-table-column label="客户数" prop="customerCount" align="center" />
              <el-table-column label="订单数" prop="orderCount" align="center" />
              <el-table-column label="销售额(¥)" align="right">
                <template #default="s">{{ formatAmount(s.row.orderAmount) }}</template>
              </el-table-column>
              <el-table-column label="客单价(¥)" align="right">
                <template #default="s">{{ formatAmount(s.row.avgOrderAmount) }}</template>
              </el-table-column>
              <el-table-column label="销售额占比" align="center">
                <template #default="s">{{ s.row.amountRatio || 0 }}%</template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">客户类型月度趋势</span>
              <span class="chart-panel__desc">按月堆叠各类型销售额，悬浮可见当月占比</span>
            </div>
            <div ref="custTypeTrendChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- P2-04: 个人客户消费Top分析 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">个人客户消费 Top10</span>
              <span class="chart-panel__desc">按累计消费金额排名（有效订单），手机号脱敏展示</span>
            </div>
            <div ref="personalTopChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">个人客户消费明细</span>
              <span class="chart-panel__desc">脱敏统计口径，响应不含明文手机号</span>
            </div>
            <el-table :data="personalTopRows" border size="small" class="cust-type-table">
              <el-table-column label="排名" type="index" width="60" align="center" />
              <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
              <el-table-column label="手机号(脱敏)" prop="contactPhone" width="120" align="center">
                <template #default="s">{{ s.row.contactPhone || '—' }}</template>
              </el-table-column>
              <el-table-column label="订单数" prop="orderCount" width="70" align="center" />
              <el-table-column label="累计消费(¥)" align="right" width="110">
                <template #default="s">{{ formatAmount(s.row.totalAmount) }}</template>
              </el-table-column>
              <el-table-column label="客单价(¥)" align="right" width="100">
                <template #default="s">{{ formatAmount(s.row.avgOrderAmount) }}</template>
              </el-table-column>
              <el-table-column label="最近消费" width="105" align="center">
                <template #default="s">{{ (s.row.lastOrderTime || '').toString().slice(0, 10) || '—' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第三行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">合同/订单月度趋势</span>
              <span class="chart-panel__desc">按签约月份统计合同金额</span>
            </div>
            <div ref="revenueChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">销售业绩排行 TOP10</span>
              <span class="chart-panel__desc">按合同金额排名</span>
            </div>
            <div ref="salesRankChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第四行 - 赢单输单分析 + 销售预测 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">赢单输单分析</span>
              <span class="chart-panel__desc">商机赢/输统计</span>
            </div>
            <div ref="winLossChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">销售预测</span>
              <span class="chart-panel__desc">按月统计预计成交加权金额</span>
            </div>
            <div ref="forecastChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- P3-14: 待跟进联系人提醒 -->
      <el-row :gutter="16" class="kpi-row" v-if="followUpReminders.length > 0">
        <el-col :span="24">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">待跟进联系人提醒</span>
              <span class="chart-panel__desc">以下联系人到了计划跟进时间</span>
            </div>
            <div class="chart-panel__body" style="padding: 0;">
              <el-table :data="followUpReminders" border size="small">
                <el-table-column label="姓名" prop="name" width="100" />
                <el-table-column label="所属客户" prop="customerName" show-overflow-tooltip />
                <el-table-column label="手机号" prop="phone" width="130" />
                <el-table-column label="职位" prop="position" width="120" />
                <el-table-column label="计划跟进时间" prop="nextContactTime" width="180">
                  <template #default="scope"><span style="color: #f56c6c;">{{ scope.row.nextContactTime }}</span></template>
                </el-table-column>
                <el-table-column label="归属销售" prop="ownerUserName" width="100" />
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup name="MkDashboard">
import * as echarts from 'echarts'
import { User, Connection, TrendCharts, Document, ShoppingCart, Promotion } from '@element-plus/icons-vue'
import { getOverview, getLeadFunnel, getOpportunityPipeline, getCustomerByIndustry, getCustomerByLevel, getSalesRanking, getRevenueTrend, getActivityResult, getFollowUpReminders, getWinLossAnalysis, getSalesForecast, getCustomerTypeAnalysis, getPersonalTop } from '@/api/mk/dashboard'

const { proxy } = getCurrentInstance()
const { marketing_lead_status, marketing_customer_level, marketing_industry, marketing_activity_type } = proxy.useDict('marketing_lead_status', 'marketing_customer_level', 'marketing_industry', 'marketing_activity_type')

const loading = ref(true)
const lastUpdate = ref('')
const overview = ref({})
const followUpReminders = ref([])

// 图表引用
const leadFunnelChart = ref(null)
const oppPipelineChart = ref(null)
const industryChart = ref(null)
const levelChart = ref(null)
const activityChart = ref(null)
const revenueChart = ref(null)
const salesRankChart = ref(null)
const winLossChart = ref(null)
const forecastChart = ref(null)
const custTypeChart = ref(null)
const custTypeTrendChart = ref(null)
const personalTopChart = ref(null)

// 图表实例
let leadFunnelInstance = null
let oppPipelineInstance = null
let industryInstance = null
let levelInstance = null
let activityInstance = null
let revenueInstance = null
let salesRankInstance = null
let winLossInstance = null
let forecastInstance = null
let custTypeInstance = null
let custTypeTrendInstance = null
let personalTopInstance = null

// P2-01: 客户类型统计数据（stats 明细表）
const custTypeStats = ref([])

// P2-04: 个人客户消费Top10（明细表）
const personalTopRows = ref([])

// 主题色
const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#606266')
const chartSplitColor = computed(() => isDark.value ? '#434343' : '#eee')

/** 金额格式化 */
function formatAmount(val) {
  if (!val) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 字典标签转换 */
function dictLabel(dictOptions, code) {
  if (!code && code !== '0') return code || '未知'
  const item = dictOptions.value?.find(d => d.value === code)
  return item ? item.label : code
}

/** 线索转化漏斗图 */
function renderLeadFunnel(data) {
  if (!leadFunnelChart.value) return
  const statusOrder = ['0', '1', '2', '3', '4', '5']
  const funnelData = statusOrder.map(code => {
    const item = data.find(d => d.code === code)
    return { name: dictLabel(marketing_lead_status, code), value: item?.count || 0 }
  }).filter(d => d.value > 0)

  if (leadFunnelInstance) leadFunnelInstance.dispose()
  leadFunnelInstance = echarts.init(leadFunnelChart.value)
  leadFunnelInstance.setOption({
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
      data: funnelData.length > 0 ? funnelData : [{ name: '暂无数据', value: 1 }]
    }]
  })
}

/** 商机销售管线图 */
function renderOppPipeline(data) {
  if (!oppPipelineChart.value) return
  const chartData = data.map(d => ({
    name: d.name || d.code,
    count: d.count || 0,
    amount: Number(d.amount || 0)
  }))

  if (oppPipelineInstance) oppPipelineInstance.dispose()
  oppPipelineInstance = echarts.init(oppPipelineChart.value)
  oppPipelineInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['商机数量', '预计金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.name), axisLabel: { color: chartTextColor.value, rotate: chartData.length > 6 ? 30 : 0 } },
    yAxis: [
      { type: 'value', name: '数量', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '金额', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    series: [
      { name: '商机数量', type: 'bar', barWidth: '35%', data: chartData.map(d => d.count), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '预计金额', type: 'line', yAxisIndex: 1, smooth: true, data: chartData.map(d => d.amount), itemStyle: { color: '#e6a23c' }, lineStyle: { width: 3 } }
    ]
  })
}

/** 客户行业分布饼图 */
function renderIndustry(data) {
  if (!industryChart.value) return
  const chartData = data.map(d => ({ name: dictLabel(marketing_industry, d.code), value: d.count || 0 }))

  if (industryInstance) industryInstance.dispose()
  industryInstance = echarts.init(industryChart.value)
  industryInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
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

/** 客户等级分布图 */
function renderLevel(data) {
  if (!levelChart.value) return
  const levelOrder = ['1', '2', '3', '4']
  const colors = { '1': '#f56c6c', '2': '#e6a23c', '3': '#409eff', '4': '#909399' }
  const chartData = levelOrder.map(code => {
    const item = data.find(d => d.code === code)
    return { name: dictLabel(marketing_customer_level, code), value: item?.count || 0, itemStyle: { color: colors[code] } }
  })

  if (levelInstance) levelInstance.dispose()
  levelInstance = echarts.init(levelChart.value)
  levelInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}: {c}家' },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: chartData.map(d => ({ value: d.value, itemStyle: { color: d.itemStyle.color, borderRadius: [0, 6, 6, 0] } })),
      label: { show: true, position: 'right', formatter: '{c}', fontSize: 13, fontWeight: 'bold', color: chartTextColor.value }
    }]
  })
}

/** 活动效果统计图 */
function renderActivity(data) {
  if (!activityChart.value) return
  const chartData = data.map(d => ({
    name: dictLabel(marketing_activity_type, d.code),
    leadCount: d.activityLeadCount || 0,
    oppCount: d.opportunityCount || 0,
    amount: Number(d.amount || 0)
  }))

  if (activityInstance) activityInstance.dispose()
  activityInstance = echarts.init(activityChart.value)
  activityInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['获取线索', '转化商机'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.name), axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '获取线索', type: 'bar', barWidth: '30%', data: chartData.map(d => d.leadCount), itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] } },
      { name: '转化商机', type: 'bar', barWidth: '30%', data: chartData.map(d => d.oppCount), itemStyle: { color: '#e6a23c', borderRadius: [4, 4, 0, 0] } }
    ]
  })
}

/** 合同/订单月度趋势图 */
function renderRevenue(data) {
  if (!revenueChart.value) return
  const chartData = data.map(d => ({ date: d.date, count: d.count || 0, amount: Number(d.amount || 0) }))

  if (revenueInstance) revenueInstance.dispose()
  revenueInstance = echarts.init(revenueChart.value)
  revenueInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['合同数量', '合同金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.date), axisLabel: { color: chartTextColor.value } },
    yAxis: [
      { type: 'value', name: '数量', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '金额', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    series: [
      { name: '合同数量', type: 'bar', barWidth: '40%', data: chartData.map(d => d.count), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '合同金额', type: 'line', yAxisIndex: 1, smooth: true, data: chartData.map(d => d.amount), itemStyle: { color: '#f56c6c' }, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245,108,108,0.3)' }, { offset: 1, color: 'rgba(245,108,108,0.02)' }]) } }
    ]
  })
}

/** 销售业绩排行图 */
function renderSalesRank(data) {
  if (!salesRankChart.value) return
  const chartData = data.slice().reverse().map(d => ({
    name: d.userName || '未知',
    count: d.contractCount || 0,
    amount: Number(d.contractAmount || 0)
  }))

  if (salesRankInstance) salesRankInstance.dispose()
  salesRankInstance = echarts.init(salesRankChart.value)
  salesRankInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: function(params) {
      const p = params[0]
      const item = chartData[p.dataIndex]
      return `${item.name}<br/>合同数: ${item.count}<br/>合同金额: ¥${formatAmount(item.amount)}`
    }},
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: chartTextColor.value, formatter: function(val) { return '¥' + (val / 10000).toFixed(0) + '万' } }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: chartData.map((d, i) => ({
        value: d.amount,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409eff' + 'cc' },
            { offset: 1, color: '#409eff' }
          ]),
          borderRadius: [0, 6, 6, 0]
        }
      })),
      label: { show: true, position: 'right', formatter: function(p) { return '¥' + formatAmount(p.value) }, fontSize: 12, color: chartTextColor.value }
    }]
  })
}

/** 赢单输单分析图 */
function renderWinLoss(data) {
  if (!winLossChart.value) return
  const chartData = data.map(d => ({
    name: d.name || d.code,
    count: d.count || 0,
    amount: Number(d.wonAmount || d.amount || 0)
  }))

  if (winLossInstance) winLossInstance.dispose()
  winLossInstance = echarts.init(winLossChart.value)
  winLossInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['数量', '金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.name), axisLabel: { color: chartTextColor.value } },
    yAxis: [
      { type: 'value', name: '数量', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '金额', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    series: [
      { name: '数量', type: 'bar', barWidth: '35%', data: chartData.map(d => ({ value: d.count, itemStyle: { color: d.name === '已赢单' ? '#67c23a' : '#f56c6c', borderRadius: [4, 4, 0, 0] } })) },
      { name: '金额', type: 'line', yAxisIndex: 1, smooth: true, data: chartData.map(d => d.amount), itemStyle: { color: '#e6a23c' }, lineStyle: { width: 3 } }
    ]
  })
}

/** 销售预测图 */
function renderForecast(data) {
  if (!forecastChart.value) return
  const chartData = data.map(d => ({
    date: d.date,
    count: d.count || 0,
    amount: Number(d.amount || 0),
    weighted: Number(d.weightedAmount || 0)
  }))

  if (forecastInstance) forecastInstance.dispose()
  forecastInstance = echarts.init(forecastChart.value)
  forecastInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['预计金额', '加权金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.date), axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'value', name: '金额', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '预计金额', type: 'bar', barWidth: '35%', data: chartData.map(d => d.amount), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '加权金额', type: 'line', smooth: true, data: chartData.map(d => d.weighted), itemStyle: { color: '#67c23a' }, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103,194,58,0.3)' }, { offset: 1, color: 'rgba(103,194,58,0.02)' }]) } }
    ]
  })
}

/** P2-01: 客户类型销售构成饼图（按销售额占比） */
function renderCustType(data) {
  if (!custTypeChart.value) return
  const typeColors = { '1': '#409eff', '2': '#e6a23c', '3': '#67c23a' }
  const chartData = (data || []).map(d => ({
    name: d.typeName || d.code,
    value: Number(d.orderAmount || 0),
    itemStyle: { color: typeColors[d.code] || '#909399' }
  })).filter(d => d.value > 0)

  if (custTypeInstance) custTypeInstance.dispose()
  custTypeInstance = echarts.init(custTypeChart.value)
  custTypeInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>销售额: ¥{c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold', formatter: '{b}\n¥{c} ({d}%)' } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** P2-01: 客户类型月度销售额趋势（堆叠柱状图，tooltip 含当月占比） */
function renderCustTypeTrend(data) {
  if (!custTypeTrendChart.value) return
  const typeOrder = ['1', '2', '3']
  const typeNames = { '1': '企业客户', '2': '经销商', '3': '个人客户' }
  const typeColors = { '1': '#409eff', '2': '#e6a23c', '3': '#67c23a' }
  const rows = data || []
  const months = [...new Set(rows.map(d => d.date))].sort()
  // 组装 map[month][type] = { cnt, amount }
  const map = {}
  rows.forEach(d => {
    if (!map[d.date]) map[d.date] = {}
    const prev = map[d.date][d.code] || { cnt: 0, amount: 0 }
    map[d.date][d.code] = { cnt: prev.cnt + (d.count || 0), amount: prev.amount + Number(d.orderAmount || 0) }
  })

  if (custTypeTrendInstance) custTypeTrendInstance.dispose()
  custTypeTrendInstance = echarts.init(custTypeTrendChart.value)
  custTypeTrendInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        let html = params[0].axisValue + '<br/>'
        let total = 0
        params.forEach(p => { total += p.value || 0 })
        params.forEach(p => {
          const pct = total > 0 ? ((p.value || 0) / total * 100).toFixed(1) : '0.0'
          html += `${p.marker}${p.seriesName}: ¥${formatAmount(p.value || 0)} (${pct}%)<br/>`
        })
        html += `当月合计: ¥${formatAmount(total)}`
        return html
      }
    },
    legend: { data: typeOrder.map(c => typeNames[c]), bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: months, axisLabel: { color: chartTextColor.value, rotate: months.length > 6 ? 30 : 0 } },
    yAxis: { type: 'value', name: '销售额', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: typeOrder.map(code => ({
      name: typeNames[code],
      type: 'bar',
      stack: 'total',
      barWidth: '40%',
      emphasis: { focus: 'series' },
      itemStyle: { color: typeColors[code] },
      data: months.map(m => (map[m] && map[m][code] ? map[m][code].amount : 0))
    }))
  })
}

/** P2-04: 个人客户消费Top10横向条形图（按累计消费金额） */
function renderPersonalTop(data) {
  if (!personalTopChart.value) return
  const rows = (data || []).filter(d => Number(d.totalAmount || 0) > 0)
  // 横向条形图：yAxis 自下而上，反转数组使第一名在顶部
  const chartData = [...rows].reverse()

  if (personalTopInstance) personalTopInstance.dispose()
  personalTopInstance = echarts.init(personalTopChart.value)
  personalTopInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const p = params[0]
        const row = chartData[p.dataIndex] || {}
        return `${p.name}<br/>累计消费: ¥${formatAmount(p.value)}<br/>订单数: ${row.orderCount || 0} 单<br/>客单价: ¥${formatAmount(row.avgOrderAmount)}<br/>手机号: ${row.contactPhone || '—'}`
      }
    },
    grid: { left: '3%', right: '8%', bottom: '6%', top: '5%', containLabel: true },
    xAxis: { type: 'value', name: '累计消费', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'category', data: chartData.map(d => d.customerName || '未知'), axisLabel: { color: chartTextColor.value, width: 110, overflow: 'truncate' } },
    series: [{
      name: '累计消费',
      type: 'bar',
      barWidth: '55%',
      itemStyle: { color: '#67c23a', borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: 'right', color: chartTextColor.value, fontSize: 11, formatter: p => '¥' + formatAmount(p.value) },
      data: chartData.map(d => Number(d.totalAmount || 0))
    }]
  })
}

/** 窗口自适应 */
function handleResize() {
  leadFunnelInstance && leadFunnelInstance.resize()
  oppPipelineInstance && oppPipelineInstance.resize()
  industryInstance && industryInstance.resize()
  levelInstance && levelInstance.resize()
  activityInstance && activityInstance.resize()
  revenueInstance && revenueInstance.resize()
  salesRankInstance && salesRankInstance.resize()
  winLossInstance && winLossInstance.resize()
  forecastInstance && forecastInstance.resize()
  custTypeInstance && custTypeInstance.resize()
  custTypeTrendInstance && custTypeTrendInstance.resize()
  personalTopInstance && personalTopInstance.resize()
}

/** 加载所有数据 */
function loadData() {
  loading.value = true
  Promise.all([
    getOverview(),
    getLeadFunnel(),
    getOpportunityPipeline(),
    getCustomerByIndustry(),
    getCustomerByLevel(),
    getSalesRanking(),
    getRevenueTrend(),
    getActivityResult(),
    getWinLossAnalysis(),
    getSalesForecast(),
    getCustomerTypeAnalysis(),
    getPersonalTop()
  ]).then(([ovRes, lfRes, opRes, indRes, lvlRes, srRes, revRes, actRes, wlRes, fcRes, ctRes, ptRes]) => {
    overview.value = ovRes.data || {}
    loading.value = false
    lastUpdate.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    nextTick(() => {
      renderLeadFunnel(lfRes.data || [])
      renderOppPipeline(opRes.data || [])
      renderIndustry(indRes.data || [])
      renderLevel(lvlRes.data || [])
      renderActivity(actRes.data || [])
      renderRevenue(revRes.data || [])
      renderSalesRank(srRes.data || [])
      renderWinLoss(wlRes.data || [])
      renderForecast(fcRes.data || [])
      custTypeStats.value = ctRes.data?.stats || []
      renderCustType(ctRes.data?.stats || [])
      renderCustTypeTrend(ctRes.data?.trend || [])
      personalTopRows.value = ptRes.data || []
      renderPersonalTop(ptRes.data || [])
    })
  }).catch(() => {
    loading.value = false
  })
  // 加载待跟进联系人
  getFollowUpReminders().then(res => { followUpReminders.value = res.data || [] }).catch(e => { proxy.$modal.msgWarning('跟进提醒加载失败：' + (e?.message || e)) })
}

/** 监听暗色模式切换 */
watch(isDark, () => {
  if (!loading.value) {
    nextTick(() => {
      // 重新获取数据并重绘
      getLeadFunnel().then(res => renderLeadFunnel(res.data || []))
      getOpportunityPipeline().then(res => renderOppPipeline(res.data || []))
      getCustomerByIndustry().then(res => renderIndustry(res.data || []))
      getCustomerByLevel().then(res => renderLevel(res.data || []))
      getActivityResult().then(res => renderActivity(res.data || []))
      getRevenueTrend().then(res => renderRevenue(res.data || []))
      getSalesRanking().then(res => renderSalesRank(res.data || []))
      getWinLossAnalysis().then(res => renderWinLoss(res.data || []))
      getSalesForecast().then(res => renderForecast(res.data || []))
      getCustomerTypeAnalysis().then(res => {
        custTypeStats.value = res.data?.stats || []
        renderCustType(res.data?.stats || [])
        renderCustTypeTrend(res.data?.trend || [])
      })
      getPersonalTop().then(res => {
        personalTopRows.value = res.data || []
        renderPersonalTop(res.data || [])
      })
    })
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  leadFunnelInstance && leadFunnelInstance.dispose()
  oppPipelineInstance && oppPipelineInstance.dispose()
  industryInstance && industryInstance.dispose()
  levelInstance && levelInstance.dispose()
  activityInstance && activityInstance.dispose()
  revenueInstance && revenueInstance.dispose()
  salesRankInstance && salesRankInstance.dispose()
  winLossInstance && winLossInstance.dispose()
  forecastInstance && forecastInstance.dispose()
  custTypeInstance && custTypeInstance.dispose()
  custTypeTrendInstance && custTypeTrendInstance.dispose()
  personalTopInstance && personalTopInstance.dispose()
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
.chart-panel__body--pie {
  height: 240px;
}
.cust-type-table {
  margin-top: 4px;
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
