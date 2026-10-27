<template>
  <div class="app-container">
    <!-- 日期筛选 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="queryParams.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="queryParams.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadSummary">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 质量看板汇总 -->
    <el-card shadow="never">
      <template #header><span>质量看板汇总</span></template>
      <el-row :gutter="16">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ summary.totalInsp || 0 }}</div>
            <div class="stat-label">检验总数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #67c23a">{{ summary.passInsp || 0 }}</div>
            <div class="stat-label">合格数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #f56c6c">{{ summary.failInsp || 0 }}</div>
            <div class="stat-label">不合格数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #409eff">{{ summary.passRate || 0 }}%</div>
            <div class="stat-label">合格率</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #e6a23c">{{ summary.openNcr || 0 }}</div>
            <div class="stat-label">未关闭NCR</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #e6a23c">{{ summary.openCapa || 0 }}</div>
            <div class="stat-label">未关闭CAPA</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #f56c6c">{{ summary.openComplaint || 0 }}</div>
            <div class="stat-label">未关闭客诉</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ summary.totalNcr || 0 }}</div>
            <div class="stat-label">NCR总数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ summary.totalCapa || 0 }}</div>
            <div class="stat-label">CAPA总数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #67c23a">{{ summary.capaCloseRate || 0 }}%</div>
            <div class="stat-label">CAPA关闭率</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ summary.totalComplaint || 0 }}</div>
            <div class="stat-label">客诉总数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ summary.totalEval || 0 }}</div>
            <div class="stat-label">供应商评价数</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 供应商等级分布 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header><span>供应商等级分布</span></template>
      <el-row :gutter="20">
        <el-col :span="6"><el-tag type="success" size="large">A级: {{ summary.gradeA || 0 }}</el-tag></el-col>
        <el-col :span="6"><el-tag type="primary" size="large">B级: {{ summary.gradeB || 0 }}</el-tag></el-col>
        <el-col :span="6"><el-tag type="warning" size="large">C级: {{ summary.gradeC || 0 }}</el-tag></el-col>
        <el-col :span="6"><el-tag type="danger" size="large">D级: {{ summary.gradeD || 0 }}</el-tag></el-col>
      </el-row>
    </el-card>

    <!-- 质量目标达成概况 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>质量目标达成概况</span>
          <div>
            <el-date-picker
              v-model="targetYear"
              type="year"
              value-format="YYYY"
              placeholder="选择年份"
              style="width: 120px; margin-right: 8px"
              @change="loadTargetSummary"
            />
            <el-button type="primary" plain icon="Download" @click="handleExportTarget">导出报表</el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="16" style="margin-bottom: 16px">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ targetSummary.total || 0 }}</div>
            <div class="stat-label">目标总数</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #67c23a">{{ targetSummary.achieved || 0 }}</div>
            <div class="stat-label">已达成</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #f56c6c">{{ targetSummary.notAchieved || 0 }}</div>
            <div class="stat-label">未达成</div>
          </el-card>
        </el-col>
      </el-row>
      <el-table :data="targetSummary.targets" border size="small" v-if="targetSummary.targets && targetSummary.targets.length > 0">
        <el-table-column label="年份" prop="targetYear" width="80" align="center" />
        <el-table-column label="月份" prop="targetMonth" width="80" align="center" />
        <el-table-column label="维度" prop="dimension" width="100" />
        <el-table-column label="维度值" prop="dimensionVal" min-width="120" show-overflow-tooltip />
        <el-table-column label="目标合格率(%)" prop="targetPassRate" width="120" align="center" />
        <el-table-column label="实际合格率(%)" prop="actualPassRate" width="120" align="center">
          <template #default="scope">
            <span :style="{ color: scope.row.actualPassRate >= scope.row.targetPassRate ? '#67c23a' : '#f56c6c' }">
              {{ scope.row.actualPassRate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="目标PPM" prop="targetPpm" width="90" align="center" />
        <el-table-column label="实际PPM" prop="actualPpm" width="90" align="center" />
        <el-table-column label="达成" prop="achievement" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.achievement === '1' ? 'success' : 'danger'" size="small">
              {{ scope.row.achievement === '1' ? '已达成' : '未达成' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无质量目标数据" />
    </el-card>

    <!-- 审核计划概况 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>审核计划概况</span>
          <div>
            <el-date-picker
              v-model="auditYear"
              type="year"
              value-format="YYYY"
              placeholder="选择年份"
              style="width: 120px; margin-right: 8px"
              @change="loadAuditSummary"
            />
            <el-button type="primary" plain icon="Download" @click="handleExportAudit">导出报表</el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="16" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ auditSummary.total || 0 }}</div>
            <div class="stat-label">审核计划总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #909399">{{ auditSummary.planned || 0 }}</div>
            <div class="stat-label">待执行</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #e6a23c">{{ auditSummary.inProgress || 0 }}</div>
            <div class="stat-label">执行中</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #67c23a">{{ auditSummary.completed || 0 }}</div>
            <div class="stat-label">已完成</div>
          </el-card>
        </el-col>
      </el-row>
      <el-table :data="auditSummary.auditPlans" border size="small" v-if="auditSummary.auditPlans && auditSummary.auditPlans.length > 0">
        <el-table-column label="审核编号" prop="auditPlanNo" min-width="140" />
        <el-table-column label="审核标题" prop="auditTitle" min-width="180" show-overflow-tooltip />
        <el-table-column label="年度" prop="auditYear" width="80" align="center" />
        <el-table-column label="主审" prop="leadAuditor" width="120" />
        <el-table-column label="状态" prop="planStatus" width="100" align="center">
          <template #default="scope">
            <el-tag :type="auditStatusType(scope.row.planStatus)" size="small">
              {{ auditStatusText(scope.row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无审核计划数据" />
    </el-card>

    <!-- 报表导出 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header><span>报表导出</span></template>
      <el-table :data="reportList" border>
        <el-table-column label="报表名称" prop="name" min-width="160" />
        <el-table-column label="周期类型" prop="periodType" width="120" align="center" />
        <el-table-column label="选择日期" width="200">
          <template #default="scope">
            <el-date-picker
              v-model="scope.row.date"
              :type="scope.row.type"
              :value-format="scope.row.format"
              style="width: 160px"
            />
          </template>
        </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button type="primary" plain icon="Download" @click="handleExport(scope.row)">导出</el-button>
              </div>
            </template>
          </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="QmsReport">
import request from '@/utils/request'
const { proxy } = getCurrentInstance()

const summary = ref({})
const targetSummary = ref({})
const auditSummary = ref({})
const targetYear = ref(new Date().getFullYear().toString())
const auditYear = ref(new Date().getFullYear().toString())

const queryParams = ref({
  startDate: '',
  endDate: ''
})

const reportList = ref([
  { name: '检验日报', periodType: '日报', url: '/qms/report/exportInspDaily', date: new Date().toISOString().slice(0, 10), type: 'date', format: 'YYYY-MM-DD' },
  { name: '检验月报', periodType: '月报', url: '/qms/report/exportInspMonthly', date: new Date().toISOString().slice(0, 7), type: 'month', format: 'YYYY-MM' },
  { name: 'NCR月报', periodType: '月报', url: '/qms/report/exportNcrMonthly', date: new Date().toISOString().slice(0, 7), type: 'month', format: 'YYYY-MM' },
  { name: '供应商质量月报', periodType: '月报', url: '/qms/report/exportSupplierMonthly', date: new Date().toISOString().slice(0, 7), type: 'month', format: 'YYYY-MM' },
  { name: '客诉月报', periodType: '月报', url: '/qms/report/exportComplaintMonthly', date: new Date().toISOString().slice(0, 7), type: 'month', format: 'YYYY-MM' },
  { name: 'CAPA月报', periodType: '月报', url: '/qms/report/exportCapaMonthly', date: new Date().toISOString().slice(0, 7), type: 'month', format: 'YYYY-MM' },
  { name: '管理评审月报', periodType: '月报', url: '/qms/report/exportMrMonthly', date: new Date().toISOString().slice(0, 7), type: 'month', format: 'YYYY-MM' }
])

function loadSummary() {
  request({
    url: '/qms/report/summary',
    method: 'get',
    params: { startDate: queryParams.value.startDate, endDate: queryParams.value.endDate }
  }).then(res => { summary.value = res.data })
}

function loadTargetSummary() {
  request({
    url: '/qms/report/targetSummary',
    method: 'get',
    params: { year: targetYear.value }
  }).then(res => { targetSummary.value = res.data })
}

function loadAuditSummary() {
  request({
    url: '/qms/report/auditSummary',
    method: 'get',
    params: { year: auditYear.value }
  }).then(res => { auditSummary.value = res.data })
}

function handleExport(row) {
  let params = {}
  if (row.type === 'date') {
    params = { date: row.date }
  } else if (row.type === 'month') {
    params = { month: row.date }
  }
  proxy.download(row.url, params, `${row.name}_${row.date}.xlsx`)
}

function handleExportTarget() {
  proxy.download('/qms/report/exportQualityTarget', { year: targetYear.value }, `质量目标达成报表_${targetYear.value}.xlsx`)
}

function handleExportAudit() {
  proxy.download('/qms/report/exportAuditPlan', { year: auditYear.value }, `审核计划报表_${auditYear.value}.xlsx`)
}

function resetQuery() {
  queryParams.value = { startDate: '', endDate: '' }
  loadSummary()
}

function auditStatusText(status) {
  const map = { '0': '待执行', '1': '执行中', '2': '已完成', '3': '已取消' }
  return map[status] || status
}

function auditStatusType(status) {
  const map = { '0': 'info', '1': 'warning', '2': 'success', '3': 'danger' }
  return map[status] || 'info'
}

// 初始化加载数据
loadSummary()
loadTargetSummary()
loadAuditSummary()
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 10px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
