<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="月度业绩归集" name="perf" />
      <el-tab-pane label="线下业绩补录" name="manual" />
    </el-tabs>

    <!-- 月度归集 -->
    <div v-show="activeTab === 'perf'">
      <el-form :inline="true" v-show="showSearch">
        <el-form-item label="归集周期"><el-date-picker v-model="queryPeriod" type="month" value-format="YYYY-MM" placeholder="选择月份" clearable /></el-form-item>
        <el-form-item label="员工姓名"><el-input v-model="perfQuery.employeeName" placeholder="请输入员工姓名" clearable style="width: 180px" @keyup.enter="getPerfList" /></el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getPerfList">搜索</el-button>
          <el-button type="success" plain icon="Refresh" @click="handleAggregate" v-hasPermi="['hr:perf:aggregate']">手动归集</el-button>
          <el-button icon="Trophy" plain @click="showRank">排名</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="perfList">
        <el-table-column label="归集周期" prop="period" width="100" align="center" />
        <el-table-column label="员工姓名" prop="employeeName" width="120" />
        <el-table-column label="成交金额" prop="dealAmount" align="right" width="140"><template #default="scope"><span>￥{{ formatAmount(scope.row.dealAmount) }}</span></template></el-table-column>
        <el-table-column label="回款金额" prop="paymentAmount" align="right" width="140"><template #default="scope"><span>￥{{ formatAmount(scope.row.paymentAmount) }}</span></template></el-table-column>
        <el-table-column label="新开客户数" prop="newCustomerCount" width="110" align="center" />
        <el-table-column label="补录业绩" prop="manualAmount" align="right" width="130"><template #default="scope"><span>￥{{ formatAmount(scope.row.manualAmount) }}</span></template></el-table-column>
        <el-table-column label="合计业绩" prop="totalAmount" align="right" width="140"><template #default="scope"><span class="amount-strong">￥{{ formatAmount(scope.row.totalAmount) }}</span></template></el-table-column>
        <el-table-column label="归集时间" prop="aggTime" width="160" align="center"><template #default="scope"><span>{{ parseTime(scope.row.aggTime) }}</span></template></el-table-column>
      </el-table>
      <pagination v-show="perfTotal > 0" :total="perfTotal" v-model:page="perfQuery.pageNum" v-model:limit="perfQuery.pageSize" @pagination="getPerfList" />
    </div>

    <!-- 线下补录 -->
    <div v-show="activeTab === 'manual'">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="manualOpen = true" v-hasPermi="['hr:perf:manualAdd']">发起补录</el-button></el-col>
      </el-row>
      <el-table v-loading="loading" :data="manualList">
        <el-table-column label="归集周期" prop="period" width="100" align="center" />
        <el-table-column label="员工姓名" prop="employeeName" width="120" />
        <el-table-column label="补录金额" prop="manualAmount" align="right" width="140"><template #default="scope"><span>￥{{ formatAmount(scope.row.manualAmount) }}</span></template></el-table-column>
        <el-table-column label="补录原因" prop="manualReason" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" prop="perfStatus" width="100" align="center"><template #default="scope"><dict-tag :options="manualStatusOptions" :value="scope.row.perfStatus" /></template></el-table-column>
        <el-table-column label="审批人" prop="approver" width="100" />
        <el-table-column label="审批意见" prop="approveOpinion" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <template v-if="scope.row.perfStatus === '0'">
              <el-button link type="success" icon="CircleCheck" @click="handleApprove(scope.row, true)" v-hasPermi="['hr:perf:approve']">通过</el-button>
              <el-button link type="danger" icon="CircleClose" @click="handleApprove(scope.row, false)" v-hasPermi="['hr:perf:approve']">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="manualTotal > 0" :total="manualTotal" v-model:page="manualQuery.pageNum" v-model:limit="manualQuery.pageSize" @pagination="getManualList" />
    </div>

    <!-- 排名 -->
    <el-dialog title="业绩排名（合计业绩降序）" v-model="rankOpen" width="560px" append-to-body>
      <el-table :data="rankList" size="small">
        <el-table-column type="index" label="名次" width="70" align="center" />
        <el-table-column label="员工姓名" prop="employeeName" />
        <el-table-column label="合计业绩" prop="totalAmount" align="right"><template #default="scope"><span>￥{{ formatAmount(scope.row.totalAmount) }}</span></template></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 发起补录 -->
    <el-dialog title="发起线下业绩补录" v-model="manualOpen" width="560px" append-to-body>
      <el-form ref="manualRef" :model="manualForm" :rules="manualRules" label-width="110px">
        <el-form-item label="归集周期" prop="period"><el-date-picker v-model="manualForm.period" type="month" value-format="YYYY-MM" style="width: 100%" /></el-form-item>
        <el-form-item label="员工姓名" prop="employeeName"><el-input v-model="manualForm.employeeName" placeholder="请输入员工姓名（如已建营销档案可直接选择）" /></el-form-item>
        <el-form-item label="补录金额" prop="manualAmount"><el-input-number v-model="manualForm.manualAmount" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="补录原因" prop="manualReason"><el-input v-model="manualForm.manualReason" type="textarea" :rows="2" placeholder="请说明补录原因（MR-02：须走审批）" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitManual">提 交</el-button><el-button @click="manualOpen = false">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="HrPerf">
import { listPerf, perfRank, aggregatePerf, listManual, addManual, approveManual } from '@/api/hr/salesPerf'

const { proxy } = getCurrentInstance()
const { hr_manual_status: manualStatusOptions } = proxy.useDict('hr_manual_status')

const activeTab = ref('perf')
const showSearch = ref(true)
const loading = ref(false)
const perfList = ref([])
const perfTotal = ref(0)
const manualList = ref([])
const manualTotal = ref(0)
const rankOpen = ref(false)
const rankList = ref([])
const manualOpen = ref(false)
const queryPeriod = ref('')
const perfQuery = ref({ pageNum: 1, pageSize: 10, period: undefined, employeeName: undefined })
const manualQuery = ref({ pageNum: 1, pageSize: 10 })
const manualForm = ref({})
const manualRules = {
  period: [{ required: true, message: '请选择归集周期', trigger: 'change' }],
  employeeName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  manualAmount: [{ required: true, message: '请输入补录金额', trigger: 'blur' }],
  manualReason: [{ required: true, message: '请输入补录原因（MR-02）', trigger: 'blur' }]
}

function formatAmount(v) { return v != null ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00' }
function getPerfList() {
  loading.value = true
  perfQuery.value.period = queryPeriod.value || undefined
  listPerf(perfQuery.value).then(res => { perfList.value = res.rows; perfTotal.value = res.total; loading.value = false })
}
function getManualList() { listManual(manualQuery.value).then(res => { manualList.value = res.rows; manualTotal.value = res.total }) }
function handleAggregate() {
  const period = queryPeriod.value || new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 7)
  proxy.$modal.confirm('确认对 ' + period + ' 周期重新归集？将清理该周期快照后重算（幂等）。').then(() => aggregatePerf(period)).then(res => {
    proxy.$modal.msgSuccess('归集完成，共 ' + res.data + ' 条记录')
    getPerfList()
  }).catch(() => {})
}
function showRank() {
  const period = queryPeriod.value || perfList.value[0]?.period
  if (!period) { proxy.$modal.msgError('请先选择归集周期'); return }
  perfRank(period).then(res => { rankList.value = res.data || []; rankOpen.value = true })
}
function handleApprove(row, approved) {
  proxy.$modal.prompt(approved ? '确认通过该补录？可填写审批意见' : '请填写驳回原因').then(({ value }) => {
    approveManual({ manualId: row.manualId, perfStatus: approved ? '1' : '2', approveOpinion: value }).then(() => {
      proxy.$modal.msgSuccess(approved ? '已通过' : '已驳回')
      getManualList()
    })
  }).catch(() => {})
}
function submitManual() {
  proxy.$refs['manualRef'].validate(valid => {
    if (valid) addManual(manualForm.value).then(() => { proxy.$modal.msgSuccess('补录已提交，等待审批'); manualOpen.value = false; getManualList() })
  })
}
getPerfList()
getManualList()
</script>
