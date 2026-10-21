<template>
  <div class="app-container hr-statistics-page">
    <!-- 概览卡片行 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :span="4" v-for="card in overviewCards" :key="card.label">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" :style="{ background: card.color }">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-label">{{ card.label }}</div>
            <div class="overview-value">{{ card.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 报表页签 -->
    <el-tabs v-model="activeTab" type="border-card" class="stat-tabs" @tab-change="onTabChange">
      <!-- 在册人员报表 -->
      <el-tab-pane label="在册人员报表" name="headcount">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-card shadow="never" class="chart-card">
              <template #header><span>各部门在册人数分布</span></template>
              <div ref="headcountChart" style="height: 350px" v-loading="loading.headcount" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="chart-card">
              <template #header><span>在册人员性别构成</span></template>
              <div ref="genderChart" style="height: 350px" v-loading="loading.headcount" />
            </el-card>
          </el-col>
        </el-row>
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>在册人员明细（按部门）</span>
              <el-button type="warning" plain icon="Download" size="small" @click="handleExport('headcountOrg')">导出Excel</el-button>
            </div>
          </template>
          <el-table v-loading="loading.headcount" :data="headcountList" border stripe>
            <el-table-column label="部门" prop="name" min-width="160" show-overflow-tooltip />
            <el-table-column label="在册人数" prop="count" align="center" width="110" />
            <el-table-column label="男性人数" prop="maleCount" align="center" width="110" />
            <el-table-column label="女性人数" prop="femaleCount" align="center" width="110" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 人员流动报表 -->
      <el-tab-pane label="人员流动报表" name="flow">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ flowYear }} 年入职/离职月度趋势</span>
              <el-date-picker v-model="flowYear" type="year" placeholder="选择年份" value-format="YYYY" style="width: 120px" @change="loadFlow" :clearable="false" />
            </div>
          </template>
          <div ref="flowChart" style="height: 350px" v-loading="loading.flow" />
        </el-card>
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>人员流动明细（按月）</span>
              <el-button type="warning" plain icon="Download" size="small" @click="handleExport('flow')">导出Excel</el-button>
            </div>
          </template>
          <el-table v-loading="loading.flow" :data="flowList" border stripe show-summary>
            <el-table-column label="月份" align="center" width="120">
              <template #default="scope">{{ flowYear }}-{{ String(scope.row.yearMonth).padStart(2, '0') }}</template>
            </el-table-column>
            <el-table-column label="入职人数" prop="entryCount" align="center" />
            <el-table-column label="离职人数" prop="leaveCount" align="center" />
            <el-table-column label="净增人数" prop="count" align="center" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 出勤月报 -->
      <el-tab-pane label="出勤月报" name="attendance">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>考勤数据</span>
              <div class="header-actions">
                <el-date-picker v-model="attendanceMonth" type="month" placeholder="选择月份" value-format="YYYY-MM" style="width: 140px" @change="loadAttendance" :clearable="false" />
                <el-button type="warning" plain icon="Download" size="small" @click="handleExport('attendance')">导出Excel</el-button>
              </div>
            </div>
          </template>
          <el-table v-loading="loading.attendance" :data="attendanceList" border stripe show-summary>
            <el-table-column label="部门" prop="name" min-width="150" show-overflow-tooltip />
            <el-table-column label="考勤人数" prop="requiredPeople" align="center" width="100" />
            <el-table-column label="出勤天数" prop="attendDays" align="center" width="100" />
            <el-table-column label="实际出勤" prop="count" align="center" width="100" />
            <el-table-column label="迟到次数" prop="lateCount" align="center" width="100" />
            <el-table-column label="早退次数" prop="earlyCount" align="center" width="100" />
            <el-table-column label="缺勤天数" prop="absentDays" align="center" width="100" />
            <el-table-column label="请假天数" prop="leaveDays" align="center" width="100" />
            <el-table-column label="加班工时" prop="overtimeHours" align="center" width="100" />
            <el-table-column label="夜班次数" prop="nightShiftCount" align="center" width="100" />
          </el-table>
        </el-card>
        <el-card shadow="never" class="chart-card">
          <template #header><span>{{ attendanceYear }} 年出勤/加班月度趋势</span></template>
          <div ref="attendanceTrendChart" style="height: 320px" v-loading="loading.attendance" />
        </el-card>
      </el-tab-pane>

      <!-- 人工成本月报 -->
      <el-tab-pane label="人工成本月报" name="cost">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ costYear }} 年人工成本月度趋势</span>
              <el-date-picker v-model="costYear" type="year" placeholder="选择年份" value-format="YYYY" style="width: 120px" @change="loadCost" :clearable="false" />
            </div>
          </template>
          <div ref="costChart" style="height: 350px" v-loading="loading.cost" />
        </el-card>
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>{{ costMonth }} 人工成本（按部门）</span>
              <div class="header-actions">
                <el-date-picker v-model="costMonth" type="month" placeholder="选择月份" value-format="YYYY-MM" style="width: 140px" @change="loadCostByOrg" :clearable="false" />
                <el-button type="warning" plain icon="Download" size="small" @click="handleExport('cost')">导出Excel</el-button>
              </div>
            </div>
          </template>
          <el-table v-loading="loading.cost" :data="costOrgList" border stripe show-summary>
            <el-table-column label="部门" prop="name" min-width="150" show-overflow-tooltip />
            <el-table-column label="人数" prop="count" align="center" width="90" />
            <el-table-column label="应发工资" prop="grossAmount" align="right" width="130" />
            <el-table-column label="个人社保" prop="socialAmount" align="right" width="120" />
            <el-table-column label="个人公积金" prop="fundAmount" align="right" width="120" />
            <el-table-column label="个人所得税" prop="taxAmount" align="right" width="120" />
            <el-table-column label="实发工资" prop="netAmount" align="right" width="130" />
            <el-table-column label="人工成本合计" prop="costAmount" align="right" width="140" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 薪酬发放台账 -->
      <el-tab-pane label="薪酬发放台账" name="ledger">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>工资发放批次台账</span>
              <div class="header-actions">
                <el-date-picker v-model="ledgerMonth" type="month" placeholder="全部月份" value-format="YYYY-MM" style="width: 140px" @change="loadLedger" clearable />
                <el-button type="warning" plain icon="Download" size="small" @click="handleExport('ledger')">导出Excel</el-button>
              </div>
            </div>
          </template>
          <el-table v-loading="loading.ledger" :data="ledgerList" border stripe>
            <el-table-column label="批次号" prop="batchNo" min-width="140" show-overflow-tooltip />
            <el-table-column label="批次名称" prop="batchName" min-width="160" show-overflow-tooltip />
            <el-table-column label="工资月份" prop="yearMonth" align="center" width="110" />
            <el-table-column label="发放日期" prop="payDate" align="center" width="120">
              <template #default="scope">{{ scope.row.payDate || '-' }}</template>
            </el-table-column>
            <el-table-column label="发放银行" prop="bankName" min-width="130" show-overflow-tooltip />
            <el-table-column label="发放人数" prop="totalCount" align="center" width="100" />
            <el-table-column label="批次金额" prop="totalAmount" align="right" width="140" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="HrStatistics">
import { ref, reactive, nextTick, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getHeadcountByOrg, getFlowTrend, getAttendanceByOrg, getAttendanceTrend, getCostByMonth, getCostByOrg, getPayrollLedger } from '@/api/hr/statistics'

const { proxy } = getCurrentInstance()

const loading = reactive({ headcount: false, flow: false, attendance: false, cost: false, ledger: false })
const activeTab = ref('headcount')

const overview = ref({})
const headcountList = ref([])
const flowList = ref([])
const attendanceList = ref([])
const attendanceTrendList = ref([])
const costMonthList = ref([])
const costOrgList = ref([])
const ledgerList = ref([])

const now = new Date()
const flowYear = ref(String(now.getFullYear()))
const attendanceYear = ref(String(now.getFullYear()))
const attendanceMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const costYear = ref(String(now.getFullYear()))
const costMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const ledgerMonth = ref(null)

const overviewCards = computed(() => [
  { label: '在册人数', value: overview.value.headcount ?? 0, icon: 'User', color: '#409eff' },
  { label: '本年入职', value: overview.value.entryCount ?? 0, icon: 'CirclePlus', color: '#67c23a' },
  { label: '本年离职', value: overview.value.leaveCount ?? 0, icon: 'CircleClose', color: '#f56c6c' },
  { label: `最近月人工成本(${overview.value.latestMonth || '-'})`, value: fmtAmount(overview.value.costAmount), icon: 'Money', color: '#e6a23c' },
  { label: '本年加班工时', value: overview.value.overtimeHours ?? 0, icon: 'Timer', color: '#909399' },
  { label: '证书过期数', value: overview.value.certExpired ?? 0, icon: 'Warning', color: '#c0392b' }
])

function fmtAmount(v) {
  if (v == null) return '0.00'
  const n = Number(v)
  return n >= 10000 ? (n / 10000).toFixed(2) + ' 万' : n.toFixed(2)
}

let charts = []
function chart(id) {
  const el = proxy.$refs[id]
  if (!el) return null
  let c = charts.find(x => x.id === id)
  if (!c) { c = { id, inst: echarts.init(el) }; charts.push(c) }
  return c.inst
}
function disposeCharts() { charts.forEach(c => c.inst.dispose()); charts = [] }
function resizeCharts() { charts.forEach(c => c.inst.resize()) }
window.addEventListener('resize', resizeCharts)
onBeforeUnmount(() => { window.removeEventListener('resize', resizeCharts); disposeCharts() })

function loadOverview() {
  getOverview().then(res => { overview.value = res.data || {} })
}

function loadHeadcount() {
  loading.headcount = true
  getHeadcountByOrg().then(res => {
    headcountList.value = res.data || []
    nextTick(() => {
      const names = headcountList.value.map(x => x.name)
      const cnts = headcountList.value.map(x => Number(x.count) || 0)
      chart('headcountChart')?.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, bottom: 60, top: 30 },
        xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, interval: 0 } },
        yAxis: { type: 'value' },
        series: [{ name: '在册人数', type: 'bar', data: cnts, barMaxWidth: 40, itemStyle: { color: '#409eff' } }]
      }, true)
      const male = headcountList.value.reduce((s, x) => s + (Number(x.maleCount) || 0), 0)
      const female = headcountList.value.reduce((s, x) => s + (Number(x.femaleCount) || 0), 0)
      chart('genderChart')?.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 10 },
        series: [{ name: '性别构成', type: 'pie', radius: ['40%', '65%'],
          data: [{ name: '男', value: male }, { name: '女', value: female }],
          label: { formatter: '{b}: {c}' } }]
      }, true)
    })
  }).finally(() => { loading.headcount = false })
}

function loadFlow() {
  loading.flow = true
  getFlowTrend({ params: { year: flowYear.value } }).then(res => {
    flowList.value = res.data || []
    nextTick(() => {
      const months = flowList.value.map(x => `${x.yearMonth}月`)
      chart('flowChart')?.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['入职人数', '离职人数', '净增人数'] },
        grid: { left: 40, right: 20, bottom: 40, top: 50 },
        xAxis: { type: 'category', data: months },
        yAxis: { type: 'value' },
        series: [
          { name: '入职人数', type: 'bar', data: flowList.value.map(x => Number(x.entryCount) || 0), itemStyle: { color: '#67c23a' } },
          { name: '离职人数', type: 'bar', data: flowList.value.map(x => Number(x.leaveCount) || 0), itemStyle: { color: '#f56c6c' } },
          { name: '净增人数', type: 'line', data: flowList.value.map(x => Number(x.count) || 0), itemStyle: { color: '#409eff' } }
        ]
      }, true)
    })
  }).finally(() => { loading.flow = false })
}

function loadAttendance() {
  loading.attendance = true
  Promise.all([
    getAttendanceByOrg({ yearMonth: attendanceMonth.value }),
    getAttendanceTrend({ params: { year: attendanceYear.value } })
  ]).then(([orgRes, trendRes]) => {
    attendanceList.value = orgRes.data || []
    attendanceTrendList.value = trendRes.data || []
    nextTick(() => {
      chart('attendanceTrendChart')?.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['出勤天数', '加班工时', '请假天数', '缺勤天数'] },
        grid: { left: 50, right: 20, bottom: 40, top: 50 },
        xAxis: { type: 'category', data: attendanceTrendList.value.map(x => x.yearMonth) },
        yAxis: { type: 'value' },
        series: [
          { name: '出勤天数', type: 'bar', stack: 'a', data: attendanceTrendList.value.map(x => Number(x.attendDays) || 0), itemStyle: { color: '#409eff' } },
          { name: '加班工时', type: 'bar', stack: 'a', data: attendanceTrendList.value.map(x => Number(x.overtimeHours) || 0), itemStyle: { color: '#e6a23c' } },
          { name: '请假天数', type: 'line', data: attendanceTrendList.value.map(x => Number(x.leaveDays) || 0), itemStyle: { color: '#67c23a' } },
          { name: '缺勤天数', type: 'line', data: attendanceTrendList.value.map(x => Number(x.absentDays) || 0), itemStyle: { color: '#f56c6c' } }
        ]
      }, true)
    })
  }).finally(() => { loading.attendance = false })
}

function loadCost() {
  loading.cost = true
  getCostByMonth({ params: { year: costYear.value } }).then(res => {
    costMonthList.value = res.data || []
    nextTick(() => {
      chart('costChart')?.setOption({
        tooltip: { trigger: 'axis', valueFormatter: v => Number(v || 0).toLocaleString() },
        legend: { data: ['应发工资', '人工成本合计', '实发工资'] },
        grid: { left: 70, right: 20, bottom: 40, top: 50 },
        xAxis: { type: 'category', data: costMonthList.value.map(x => x.yearMonth) },
        yAxis: { type: 'value', axisLabel: { formatter: v => (v / 10000).toFixed(0) + '万' } },
        series: [
          { name: '应发工资', type: 'bar', data: costMonthList.value.map(x => Number(x.grossAmount) || 0), itemStyle: { color: '#409eff' } },
          { name: '人工成本合计', type: 'bar', data: costMonthList.value.map(x => Number(x.costAmount) || 0), itemStyle: { color: '#e6a23c' } },
          { name: '实发工资', type: 'line', data: costMonthList.value.map(x => Number(x.netAmount) || 0), itemStyle: { color: '#67c23a' } }
        ]
      }, true)
    })
  }).finally(() => { loading.cost = false })
}

function loadCostByOrg() {
  loading.cost = true
  getCostByOrg({ yearMonth: costMonth.value }).then(res => {
    costOrgList.value = res.data || []
  }).finally(() => { loading.cost = false })
}

function loadLedger() {
  loading.ledger = true
  getPayrollLedger({ yearMonth: ledgerMonth.value || '' }).then(res => {
    ledgerList.value = res.data || []
  }).finally(() => { loading.ledger = false })
}

function onTabChange(tab) {
  nextTick(() => {
    resizeCharts()
    if (tab === 'headcount') { if (!headcountList.value.length) loadHeadcount() }
    if (tab === 'flow') { if (!flowList.value.length) loadFlow() }
    if (tab === 'attendance') { if (!attendanceList.value.length) loadAttendance() }
    if (tab === 'cost') { if (!costMonthList.value.length) { loadCost(); loadCostByOrg() } }
    if (tab === 'ledger') { if (!ledgerList.value.length) loadLedger() }
  })
}

function handleExport(reportType) {
  const query = { reportType }
  if (reportType === 'flow') query.params = { year: flowYear.value }
  if (reportType === 'attendance') query.yearMonth = attendanceMonth.value
  if (reportType === 'cost') query.yearMonth = costMonth.value
  proxy.download('/hr/statistics/export', query, `${reportType}_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  loadOverview()
  loadHeadcount()
})
</script>

<style lang="scss" scoped>
@import '../shared-hr-page.css';

.hr-statistics-page {
  .overview-row { margin-bottom: 16px; }
  .overview-card {
    :deep(.el-card__body) { display: flex; align-items: center; padding: 16px; }
    .overview-icon {
      width: 48px; height: 48px; border-radius: 8px;
      display: flex; align-items: center; justify-content: center;
      color: #fff; font-size: 24px; margin-right: 12px; flex-shrink: 0;
    }
    .overview-label { font-size: 13px; color: #909399; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .overview-value { font-size: 22px; font-weight: 600; color: #303133; line-height: 1.3; }
  }
  .chart-card, .table-card { margin-bottom: 16px; }
  .card-header { display: flex; align-items: center; justify-content: space-between; }
  .header-actions { display: flex; align-items: center; gap: 8px; }
  .stat-tabs { :deep(.el-tabs__content) { padding: 12px; } }
}
</style>
