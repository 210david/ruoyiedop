<template>
  <div class="app-container">
    <!-- KPI 卡片行 -->
    <el-row :gutter="16" class="kpi-row">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="kpi-card kpi-card--blue">
          <div class="kpi-card__icon"><el-icon :size="32"><Document /></el-icon></div>
          <div class="kpi-card__body">
            <div class="kpi-card__label">检验任务总数</div>
            <div class="kpi-card__value">{{ summary.inspTotal || 0 }}</div>
            <div class="kpi-card__sub">待检 {{ summary.inspPending || 0 }} · 检验中 {{ summary.inspInProgress || 0 }} · 已完成 {{ summary.inspCompleted || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="kpi-card kpi-card--green">
          <div class="kpi-card__icon"><el-icon :size="32"><CircleCheck /></el-icon></div>
          <div class="kpi-card__body">
            <div class="kpi-card__label">一次交检合格率</div>
            <div class="kpi-card__value">{{ summary.passRate || 0 }}%</div>
            <div class="kpi-card__sub">合格 {{ summary.inspPass || 0 }} · 不合格 {{ summary.inspFail || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="kpi-card kpi-card--orange">
          <div class="kpi-card__icon"><el-icon :size="32"><Warning /></el-icon></div>
          <div class="kpi-card__body">
            <div class="kpi-card__label">NCR总数</div>
            <div class="kpi-card__value">{{ summary.ncrTotal || 0 }}</div>
            <div class="kpi-card__sub">待评审 {{ summary.ncrOpen || 0 }} · 评审中 {{ summary.ncrReviewing || 0 }} · 已关闭 {{ summary.ncrClosed || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="kpi-card kpi-card--red">
          <div class="kpi-card__icon"><el-icon :size="32"><CircleClose /></el-icon></div>
          <div class="kpi-card__body">
            <div class="kpi-card__label">处置中/待验证</div>
            <div class="kpi-card__value">{{ (summary.ncrDisposing || 0) + (summary.ncrVerifying || 0) }}</div>
            <div class="kpi-card__sub">处置中 {{ summary.ncrDisposing || 0 }} · 待验证 {{ summary.ncrVerifying || 0 }} · 已驳回 {{ summary.ncrRejected || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="chart-panel__head">
            <span class="chart-panel__title">检验类型分布</span>
            <span class="chart-panel__desc">IQC / IPQC / FQC / OQC 检验任务量</span>
          </div>
          <div class="chart-panel__body">
            <div ref="inspTypeChartRef" style="width: 100%; height: 320px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="chart-panel__head">
            <span class="chart-panel__title">缺陷等级分布</span>
            <span class="chart-panel__desc">致命 / 严重 / 一般 / 轻微</span>
          </div>
          <div class="chart-panel__body">
            <div ref="defectLevelChartRef" style="width: 100%; height: 320px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近记录行 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="chart-panel__head">
            <span class="chart-panel__title">最近检验任务</span>
            <span class="chart-panel__desc">最近10条检验记录</span>
          </div>
          <div class="chart-panel__body" style="padding: 0;">
            <el-table :data="recentInspTasks" border size="small">
              <el-table-column label="任务编号" prop="taskNo" min-width="140" />
              <el-table-column label="类型" prop="taskType" width="80" align="center">
                <template #default="scope"><dict-tag :options="qms_insp_type" :value="scope.row.taskType" /></template>
              </el-table-column>
              <el-table-column label="物料" prop="materialName" min-width="160" show-overflow-tooltip />
              <el-table-column label="状态" prop="taskStatus" width="80" align="center">
                <template #default="scope"><dict-tag :options="qms_insp_task_status" :value="scope.row.taskStatus" /></template>
              </el-table-column>
              <el-table-column label="结果" prop="inspectResult" width="80" align="center">
                <template #default="scope"><dict-tag :options="qms_insp_result" :value="scope.row.inspectResult" /></template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="chart-panel">
          <div class="chart-panel__head">
            <span class="chart-panel__title">最近NCR记录</span>
            <span class="chart-panel__desc">最近10条不合格品报告</span>
          </div>
          <div class="chart-panel__body" style="padding: 0;">
            <el-table :data="recentNcrs" border size="small">
              <el-table-column label="NCR编号" prop="ncrNo" min-width="140" />
              <el-table-column label="物料" prop="materialName" min-width="160" show-overflow-tooltip />
              <el-table-column label="缺陷等级" prop="defectLevel" width="90" align="center">
                <template #default="scope"><dict-tag :options="qms_defect_level" :value="scope.row.defectLevel" /></template>
              </el-table-column>
              <el-table-column label="状态" prop="ncrStatus" width="90" align="center">
                <template #default="scope"><dict-tag :options="qms_ncr_status" :value="scope.row.ncrStatus" /></template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="QmsDashboard">
import * as echarts from 'echarts'
import { Document, CircleCheck, Warning, CircleClose } from '@element-plus/icons-vue'
import { getDashboardSummary, getInspTypeDist, getDefectLevelDist, getRecentInspTasks, getRecentNcrs } from '@/api/qms/dashboard'

const { proxy } = getCurrentInstance()
const { qms_insp_type, qms_insp_task_status, qms_insp_result, qms_defect_level, qms_ncr_status } = proxy.useDict('qms_insp_type', 'qms_insp_task_status', 'qms_insp_result', 'qms_defect_level', 'qms_ncr_status')

const loading = ref(true)
const summary = ref({})
const recentInspTasks = ref([])
const recentNcrs = ref([])

// 图表引用
const inspTypeChartRef = ref(null)
const defectLevelChartRef = ref(null)
let inspTypeChart = null
let defectLevelChart = null

/** 加载数据 */
function loadData() {
  loading.value = true
  Promise.all([
    getDashboardSummary(),
    getInspTypeDist(),
    getDefectLevelDist(),
    getRecentInspTasks(),
    getRecentNcrs()
  ]).then(([summaryRes, inspTypeRes, defectLevelRes, recentInspRes, recentNcrRes]) => {
    summary.value = summaryRes.data
    recentInspTasks.value = recentInspRes.data
    recentNcrs.value = recentNcrRes.data
    // 渲染图表
    nextTick(() => {
      renderInspTypeChart(inspTypeRes.data)
      renderDefectLevelChart(defectLevelRes.data)
    })
    loading.value = false
  })
}

/** 检验类型分布 - 柱状图 */
function renderInspTypeChart(data) {
  if (!inspTypeChartRef.value) return
  if (!inspTypeChart) {
    inspTypeChart = echarts.init(inspTypeChartRef.value)
  }
  const names = data.map(d => d.name)
  const totalCounts = data.map(d => d.count)
  const passCounts = data.map(d => d.passCount)
  const failCounts = data.map(d => d.failCount)
  inspTypeChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['总数', '合格', '不合格'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value' },
    series: [
      { name: '总数', type: 'bar', data: totalCounts, itemStyle: { color: '#409EFF' } },
      { name: '合格', type: 'bar', data: passCounts, itemStyle: { color: '#67C23A' } },
      { name: '不合格', type: 'bar', data: failCounts, itemStyle: { color: '#F56C6C' } }
    ]
  })
}

/** 缺陷等级分布 - 饼图 */
function renderDefectLevelChart(data) {
  if (!defectLevelChartRef.value) return
  if (!defectLevelChart) {
    defectLevelChart = echarts.init(defectLevelChartRef.value)
  }
  const pieData = data.map(d => ({ name: d.name, value: d.count }))
  defectLevelChart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center' },
    series: [
      {
        name: '缺陷等级',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: { show: true, formatter: '{b}: {c}' },
        data: pieData,
        itemStyle: {
          color: function (params) {
            const colors = ['#F56C6C', '#E6A23C', '#409EFF', '#909399']
            return colors[params.dataIndex] || '#409EFF'
          }
        }
      }
    ]
  })
}

/** 窗口resize */
function handleResize() {
  inspTypeChart && inspTypeChart.resize()
  defectLevelChart && defectLevelChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  inspTypeChart && inspTypeChart.dispose()
  defectLevelChart && defectLevelChart.dispose()
})
</script>

<style scoped>
.kpi-row { margin-bottom: 0; }
.kpi-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  color: #fff;
  min-height: 100px;
}
.kpi-card--blue { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.kpi-card--green { background: linear-gradient(135deg, #67C23A, #85ce61); }
.kpi-card--orange { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.kpi-card--red { background: linear-gradient(135deg, #F56C6C, #f78989); }
.kpi-card__icon { margin-right: 16px; opacity: 0.8; }
.kpi-card__body { flex: 1; }
.kpi-card__label { font-size: 13px; opacity: 0.9; margin-bottom: 4px; }
.kpi-card__value { font-size: 28px; font-weight: 700; line-height: 1.2; }
.kpi-card__sub { font-size: 11px; opacity: 0.8; margin-top: 4px; }

.chart-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
.chart-panel__head {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}
.chart-panel__title { font-size: 15px; font-weight: 600; color: #303133; }
.chart-panel__desc { font-size: 12px; color: #909399; margin-left: 8px; }
.chart-panel__body { padding: 16px 20px; }
</style>
