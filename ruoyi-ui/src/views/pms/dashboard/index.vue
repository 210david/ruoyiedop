<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="dash-header">
      <div>
        <span class="dash-header__title">采购管理看板</span>
        <span class="dash-header__desc">实时监控采购全流程核心指标</span>
      </div>
      <div class="dash-header__right">
        <span class="text-muted" v-if="lastUpdate">最后更新：{{ lastUpdate }}</span>
        <el-button type="primary" icon="Refresh" size="small" @click="loadData" :loading="loading">刷新数据</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- KPI 概览卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="24" :sm="8">
          <div class="kpi-card kpi-card--blue">
            <div class="kpi-card__icon"><el-icon size="28"><Document /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ summary.reqTotal || 0 }}</div>
              <div class="kpi-card__label">采购申请</div>
              <div class="kpi-card__sub">待审批 {{ summary.reqPending || 0 }} · 已审批 {{ summary.reqApproved || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="kpi-card kpi-card--green">
            <div class="kpi-card__icon"><el-icon size="28"><Files /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ summary.contractTotal || 0 }}</div>
              <div class="kpi-card__label">采购合同</div>
              <div class="kpi-card__sub">已签订 {{ summary.contractSigned || 0 }} · 待审核 {{ summary.contractPending || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="kpi-card kpi-card--orange">
            <div class="kpi-card__icon"><el-icon size="28"><ShoppingCart /></el-icon></div>
            <div class="kpi-card__body">
              <div class="kpi-card__value">{{ summary.ordTotal || 0 }}</div>
              <div class="kpi-card__label">采购订单</div>
              <div class="kpi-card__sub">待审批 {{ summary.ordPending || 0 }} · 已完成 {{ summary.ordCompleted || 0 }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 金额统计卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="12" :sm="6">
          <div class="amount-card amount-card--blue">
            <div class="amount-card__label">采购申请金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(summary.reqTotalAmount) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="amount-card amount-card--green">
            <div class="amount-card__label">采购合同金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(summary.contractTotalAmount) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="amount-card amount-card--orange">
            <div class="amount-card__label">采购订单总金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(summary.ordTotalAmount) }}</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="amount-card amount-card--cyan">
            <div class="amount-card__label">已付金额 / 待付金额</div>
            <div class="amount-card__value">¥ {{ formatAmount(summary.ordReceivedAmount) }}</div>
            <div class="amount-card__sub">¥ {{ formatAmount(unpaidAmount) }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第一行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">采购合同状态分布</span>
              <span class="chart-panel__desc">按合同状态统计数量与金额</span>
            </div>
            <div ref="contractStatusChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">采购订单状态分布</span>
              <span class="chart-panel__desc">按订单状态统计数量与金额</span>
            </div>
            <div ref="orderStatusChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第二行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">采购月度趋势</span>
              <span class="chart-panel__desc">近12个月采购订单数量与金额</span>
            </div>
            <div ref="monthlyTrendChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">供应商采购金额排行 TOP10</span>
              <span class="chart-panel__desc">按采购金额排名</span>
            </div>
            <div ref="supplierRankChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 第三行 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">询比价状态分布</span>
              <span class="chart-panel__desc">按询价状态统计数量与预估金额</span>
            </div>
            <div ref="inquiryStatusChart" class="chart-panel__body"></div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">采购金额对比</span>
              <span class="chart-panel__desc">采购申请 / 合同 / 订单金额对比</span>
            </div>
            <div ref="amountCompareChart" class="chart-panel__body"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 最近采购订单 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :span="24">
          <div class="chart-panel">
            <div class="chart-panel__head">
              <span class="chart-panel__title">最近采购订单</span>
              <span class="chart-panel__desc">最近10条采购订单记录</span>
            </div>
            <div class="chart-panel__body" style="padding: 0;">
              <el-table :data="recentOrders" border size="small">
                <el-table-column label="采购单号" prop="orderNo" min-width="160" />
                <el-table-column label="供应商" prop="supplierName" min-width="200" show-overflow-tooltip />
                <el-table-column label="状态" prop="status" width="100" align="center">
                  <template #default="scope"><dict-tag :options="pms_order_status" :value="scope.row.status" /></template>
                </el-table-column>
                <el-table-column label="采购日期" prop="orderDate" width="120" align="center" />
                <el-table-column label="总金额" prop="totalAmount" width="130" align="right">
                  <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template>
                </el-table-column>
                <el-table-column label="采购员" prop="buyerName" width="100" />
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup name="PmsDashboard">
import * as echarts from 'echarts'
import { Document, Files, ShoppingCart } from '@element-plus/icons-vue'
import { getDashboardSummary, getRecentOrders, getOrderStatusDist, getContractStatusDist, getInquiryStatusDist, getMonthlyTrend, getSupplierRanking } from '@/api/pms/dashboard'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_order_status } = proxy.useDict('pms_order_status')

const loading = ref(true)
const lastUpdate = ref('')
const summary = ref({})
const recentOrders = ref([])

// 图表引用
const contractStatusChart = ref(null)
const orderStatusChart = ref(null)
const monthlyTrendChart = ref(null)
const supplierRankChart = ref(null)
const inquiryStatusChart = ref(null)
const amountCompareChart = ref(null)

// 图表实例
let contractStatusInstance = null
let orderStatusInstance = null
let monthlyTrendInstance = null
let supplierRankInstance = null
let inquiryStatusInstance = null
let amountCompareInstance = null

// 主题色
const isDark = computed(() => document.documentElement.classList.contains('dark'))
const chartTextColor = computed(() => isDark.value ? '#d0d0d0' : '#606266')
const chartSplitColor = computed(() => isDark.value ? '#434343' : '#eee')

const unpaidAmount = computed(() => {
  const total = summary.value.ordTotalAmount || 0
  const paid = summary.value.ordReceivedAmount || 0
  return parseFloat((total - paid).toFixed(2))
})

/** 金额格式化 */
function formatAmount(val) {
  if (!val) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 采购合同状态分布 - 柱状图 */
function renderContractStatus(data) {
  if (!contractStatusChart.value) return
  const chartData = data.map(d => ({ name: d.name || d.code, count: d.count || 0, amount: Number(d.amount || 0) }))

  if (contractStatusInstance) contractStatusInstance.dispose()
  contractStatusInstance = echarts.init(contractStatusChart.value)
  contractStatusInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const p = params[0]
        const item = chartData[p.dataIndex]
        return `${item.name}<br/>数量: ${item.count}<br/>金额: ¥${formatAmount(item.amount)}`
      }
    },
    legend: { data: ['合同数量', '合同金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.name), axisLabel: { color: chartTextColor.value, rotate: chartData.length > 5 ? 30 : 0 } },
    yAxis: [
      { type: 'value', name: '数量', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '金额', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    series: [
      { name: '合同数量', type: 'bar', barWidth: '35%', data: chartData.map(d => d.count), itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] } },
      { name: '合同金额', type: 'line', yAxisIndex: 1, smooth: true, data: chartData.map(d => d.amount), itemStyle: { color: '#e6a23c' }, lineStyle: { width: 3 } }
    ]
  })
}

/** 采购订单状态分布 - 饼图 */
function renderOrderStatus(data) {
  if (!orderStatusChart.value) return
  const chartData = data.map(d => ({ name: d.name || d.code, value: d.count || 0, amount: Number(d.amount || 0) }))

  if (orderStatusInstance) orderStatusInstance.dispose()
  orderStatusInstance = echarts.init(orderStatusChart.value)
  orderStatusInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: function(p) {
        const item = chartData[p.dataIndex]
        return `${item.name}<br/>数量: ${item.value}<br/>金额: ¥${formatAmount(item.amount)}`
      }
    },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: '65%',
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}', fontSize: 12, color: chartTextColor.value },
      emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 && chartData.some(d => d.value > 0) ? chartData.filter(d => d.value > 0) : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 采购月度趋势图 - 柱状图+折线图 */
function renderMonthlyTrend(data) {
  if (!monthlyTrendChart.value) return
  const chartData = data.map(d => ({ date: d.date, count: d.count || 0, amount: Number(d.amount || 0) }))

  if (monthlyTrendInstance) monthlyTrendInstance.dispose()
  monthlyTrendInstance = echarts.init(monthlyTrendChart.value)
  monthlyTrendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数量', '采购金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: chartData.map(d => d.date), axisLabel: { color: chartTextColor.value } },
    yAxis: [
      { type: 'value', name: '数量', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
      { type: 'value', name: '金额', splitLine: { show: false }, axisLabel: { color: chartTextColor.value } }
    ],
    series: [
      { name: '订单数量', type: 'bar', barWidth: '40%', data: chartData.map(d => d.count), itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '采购金额', type: 'line', yAxisIndex: 1, smooth: true, data: chartData.map(d => d.amount), itemStyle: { color: '#f56c6c' }, lineStyle: { width: 3 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245,108,108,0.3)' }, { offset: 1, color: 'rgba(245,108,108,0.02)' }]) } }
    ]
  })
}

/** 供应商采购金额排行图 - 横向柱状图 */
function renderSupplierRank(data) {
  if (!supplierRankChart.value) return
  const chartData = data.slice().reverse().map(d => ({
    name: d.name || '未知',
    count: d.count || 0,
    amount: Number(d.amount || 0)
  }))

  if (supplierRankInstance) supplierRankInstance.dispose()
  supplierRankInstance = echarts.init(supplierRankChart.value)
  supplierRankInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const p = params[0]
        const item = chartData[p.dataIndex]
        return `${item.name}<br/>订单数: ${item.count}<br/>采购金额: ¥${formatAmount(item.amount)}`
      }
    },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: chartTextColor.value, formatter: function(val) { return '¥' + (val / 10000).toFixed(0) + '万' } }, splitLine: { lineStyle: { type: 'dashed', color: chartSplitColor.value } } },
    yAxis: { type: 'category', data: chartData.map(d => d.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: chartTextColor.value } },
    series: [{
      type: 'bar',
      barWidth: '55%',
      data: chartData.map(d => ({
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

/** 询比价状态分布图 - 饼图 */
function renderInquiryStatus(data) {
  if (!inquiryStatusChart.value) return
  const chartData = data.map(d => ({ name: d.name || d.code, value: d.count || 0, amount: Number(d.amount || 0) }))

  if (inquiryStatusInstance) inquiryStatusInstance.dispose()
  inquiryStatusInstance = echarts.init(inquiryStatusChart.value)
  inquiryStatusInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: function(p) {
        const item = chartData[p.dataIndex]
        return `${item.name}<br/>数量: ${item.value}<br/>预估金额: ¥${formatAmount(item.amount)}`
      }
    },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 12, color: chartTextColor.value } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData.length > 0 && chartData.some(d => d.value > 0) ? chartData.filter(d => d.value > 0) : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
    }]
  })
}

/** 采购金额对比图 - 采购申请/合同/订单金额对比 */
function renderAmountCompare() {
  if (!amountCompareChart.value) return
  const reqAmount = Number(summary.value.reqTotalAmount || 0)
  const contractAmount = Number(summary.value.contractTotalAmount || 0)
  const ordAmount = Number(summary.value.ordTotalAmount || 0)
  const ordPaid = Number(summary.value.ordReceivedAmount || 0)

  if (amountCompareInstance) amountCompareInstance.dispose()
  amountCompareInstance = echarts.init(amountCompareChart.value)
  amountCompareInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['总金额', '已付金额'], bottom: 0, textStyle: { color: chartTextColor.value } },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: ['采购申请', '采购合同', '采购订单'], axisLabel: { color: chartTextColor.value } },
    yAxis: { type: 'value', name: '金额', splitLine: { lineStyle: { color: chartSplitColor.value } }, axisLabel: { color: chartTextColor.value } },
    series: [
      { name: '总金额', type: 'bar', barWidth: '30%', data: [reqAmount, contractAmount, ordAmount], itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } },
      { name: '已付金额', type: 'bar', barWidth: '30%', data: [null, null, ordPaid], itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] } }
    ]
  })
}

/** 窗口自适应 */
function handleResize() {
  contractStatusInstance && contractStatusInstance.resize()
  orderStatusInstance && orderStatusInstance.resize()
  monthlyTrendInstance && monthlyTrendInstance.resize()
  supplierRankInstance && supplierRankInstance.resize()
  inquiryStatusInstance && inquiryStatusInstance.resize()
  amountCompareInstance && amountCompareInstance.resize()
}

/** 加载所有数据 */
function loadData() {
  loading.value = true
  Promise.all([
    getDashboardSummary(),
    getRecentOrders(),
    getContractStatusDist(),
    getOrderStatusDist(),
    getInquiryStatusDist(),
    getMonthlyTrend(),
    getSupplierRanking()
  ]).then(([sumRes, ordRes, conDistRes, ordDistRes, inqDistRes, trendRes, rankRes]) => {
    summary.value = sumRes.data || {}
    recentOrders.value = ordRes.data || []
    loading.value = false
    lastUpdate.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    nextTick(() => {
      renderContractStatus(conDistRes.data || [])
      renderOrderStatus(ordDistRes.data || [])
      renderInquiryStatus(inqDistRes.data || [])
      renderMonthlyTrend(trendRes.data || [])
      renderSupplierRank(rankRes.data || [])
      renderAmountCompare()
    })
  }).catch(() => {
    loading.value = false
  })
}

/** 监听暗色模式切换 */
watch(isDark, () => {
  if (!loading.value) {
    nextTick(() => {
      getContractStatusDist().then(res => renderContractStatus(res.data || []))
      getOrderStatusDist().then(res => renderOrderStatus(res.data || []))
      getInquiryStatusDist().then(res => renderInquiryStatus(res.data || []))
      getMonthlyTrend().then(res => renderMonthlyTrend(res.data || []))
      getSupplierRanking().then(res => renderSupplierRank(res.data || []))
      renderAmountCompare()
    })
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  contractStatusInstance && contractStatusInstance.dispose()
  orderStatusInstance && orderStatusInstance.dispose()
  monthlyTrendInstance && monthlyTrendInstance.dispose()
  supplierRankInstance && supplierRankInstance.dispose()
  inquiryStatusInstance && inquiryStatusInstance.dispose()
  amountCompareInstance && amountCompareInstance.dispose()
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
.kpi-card__sub {
  font-size: 12px;
  opacity: 0.75;
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
.amount-card__sub {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
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
