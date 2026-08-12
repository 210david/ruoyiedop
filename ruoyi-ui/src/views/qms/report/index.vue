<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>质量看板汇总</span></template>
      <el-row :gutter="20">
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
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value" style="color: #f56c6c">{{ summary.openComplaint || 0 }}</div>
            <div class="stat-label">未关闭客诉</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span>供应商等级分布</span></template>
      <el-row :gutter="20">
        <el-col :span="6"><el-tag type="success" size="large">A级: {{ summary.gradeA || 0 }}</el-tag></el-col>
        <el-col :span="6"><el-tag type="primary" size="large">B级: {{ summary.gradeB || 0 }}</el-tag></el-col>
        <el-col :span="6"><el-tag type="warning" size="large">C级: {{ summary.gradeC || 0 }}</el-tag></el-col>
        <el-col :span="6"><el-tag type="danger" size="large">D级: {{ summary.gradeD || 0 }}</el-tag></el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span>报表导出</span></template>
      <el-table :data="reportList" border>
        <el-table-column label="报表名称" prop="name" />
        <el-table-column label="周期" width="200">
          <template #default="scope">
            <el-date-picker v-model="scope.row.date" :type="scope.row.type" :value-format="scope.row.format" style="width: 160px" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="primary" plain icon="Download" @click="handleExport(scope.row)">导出</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import request from '@/utils/request'
const { proxy } = getCurrentInstance()
const summary = ref({})
const reportList = ref([
  { name: '检验日报', url: '/qms/report/exportInspDaily', date: new Date().toISOString().slice(0,10), type: 'date', format: 'YYYY-MM-DD' },
  { name: '检验月报', url: '/qms/report/exportInspMonthly', date: new Date().toISOString().slice(0,7), type: 'month', format: 'YYYY-MM' },
  { name: 'NCR月报', url: '/qms/report/exportNcrMonthly', date: new Date().toISOString().slice(0,7), type: 'month', format: 'YYYY-MM' },
  { name: '供应商质量月报', url: '/qms/report/exportSupplierMonthly', date: new Date().toISOString().slice(0,7), type: 'month', format: 'YYYY-MM' },
  { name: '客诉月报', url: '/qms/report/exportComplaintMonthly', date: new Date().toISOString().slice(0,7), type: 'month', format: 'YYYY-MM' },
  { name: 'CAPA月报', url: '/qms/report/exportCapaMonthly', date: new Date().toISOString().slice(0,7), type: 'month', format: 'YYYY-MM' }
])

function loadSummary() {
  request({ url: '/qms/report/summary', method: 'get' }).then(res => { summary.value = res.data })
}
function handleExport(row) {
  proxy.download(row.url, { date: row.date, month: row.date }, `${row.name}_${row.date}.xlsx`)
}
loadSummary()
</script>

<style scoped>
.stat-card { text-align: center; padding: 10px 0; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
</style>
