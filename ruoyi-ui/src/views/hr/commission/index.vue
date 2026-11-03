<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="提成方案" name="plan" />
      <el-tab-pane label="提成核算表" name="bill" />
    </el-tabs>

    <!-- 方案 -->
    <div v-show="activeTab === 'plan'">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddPlan" v-hasPermi="['hr:commission:add']">新增方案</el-button></el-col>
      </el-row>
      <el-table v-loading="loading" :data="planList">
        <el-table-column label="方案名称" prop="planName" min-width="170" show-overflow-tooltip />
        <el-table-column label="计算口径" prop="calcBase" width="100" align="center"><template #default="scope"><dict-tag :options="baseOptions" :value="scope.row.calcBase" /></template></el-table-column>
        <el-table-column label="类型" prop="planType" width="100" align="center"><template #default="scope"><dict-tag :options="typeOptions" :value="scope.row.planType" /></template></el-table-column>
        <el-table-column label="状态" prop="planStatus" width="90" align="center"><template #default="scope"><el-tag :type="scope.row.planStatus === '1' ? 'success' : 'info'">{{ scope.row.planStatus === '1' ? '启用' : scope.row.planStatus === '2' ? '停用' : '草稿' }}</el-tag></template></el-table-column>
        <el-table-column label="锁定" prop="locked" width="80" align="center"><template #default="scope"><el-tag v-if="scope.row.locked === '1'" type="danger">已锁定</el-tag><span v-else>-</span></template></el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEditPlan(scope.row)" v-hasPermi="['hr:commission:edit']" v-if="scope.row.locked !== '1'">编辑</el-button>
            <el-button link :type="scope.row.planStatus === '1' ? 'warning' : 'success'" @click="togglePlan(scope.row)" v-hasPermi="['hr:commission:edit']">{{ scope.row.planStatus === '1' ? '停用' : '启用' }}</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelPlan(scope.row)" v-hasPermi="['hr:commission:remove']" v-if="scope.row.locked !== '1'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 核算表 -->
    <div v-show="activeTab === 'bill'">
      <el-form :inline="true">
        <el-form-item label="核算周期"><el-date-picker v-model="billQuery.period" type="month" value-format="YYYY-MM" placeholder="选择月份" clearable /></el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getBillList">搜索</el-button>
          <el-button type="success" plain icon="Cpu" @click="handleGenerate" v-hasPermi="['hr:commission:generate']">生成核算表</el-button>
          <el-button type="warning" plain icon="Lock" @click="handleLock" v-hasPermi="['hr:commission:confirm']">锁定周期</el-button>
          <el-button type="danger" plain icon="Delete" @click="handleDelBills" v-hasPermi="['hr:commission:remove']">删除待确认</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"><template #title>MR-01：核算表生成后方案按生成时快照计算；锁定周期后关联方案自动锁定不可追溯修改；MR-03：核算表确认/锁定后保留历史版本供审计</template></el-alert>
      <el-table v-loading="loading" :data="billList">
        <el-table-column label="核算周期" prop="period" width="100" align="center" />
        <el-table-column label="员工姓名" prop="employeeName" width="110" />
        <el-table-column label="提成方案" prop="planName" min-width="150" show-overflow-tooltip />
        <el-table-column label="计算口径" prop="calcBase" width="100" align="center"><template #default="scope"><dict-tag :options="baseOptions" :value="scope.row.calcBase" /></template></el-table-column>
        <el-table-column label="计算基数" prop="baseAmount" align="right" width="130"><template #default="scope"><span>￥{{ formatAmount(scope.row.baseAmount) }}</span></template></el-table-column>
        <el-table-column label="提成金额" prop="commissionAmount" align="right" width="130"><template #default="scope"><span class="amount-strong">￥{{ formatAmount(scope.row.commissionAmount) }}</span></template></el-table-column>
        <el-table-column label="状态" prop="billStatus" width="100" align="center"><template #default="scope"><dict-tag :options="billStatusOptions" :value="scope.row.billStatus" /></template></el-table-column>
        <el-table-column label="确认人" prop="confirmer" width="100" />
        <el-table-column label="操作" width="110" align="center">
          <template #default="scope">
            <el-button link type="success" icon="CircleCheck" @click="handleConfirm(scope.row)" v-hasPermi="['hr:commission:confirm']" v-if="scope.row.billStatus === '0'">确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="billTotal > 0" :total="billTotal" v-model:page="billQuery.pageNum" v-model:limit="billQuery.pageSize" @pagination="getBillList" />
    </div>

    <!-- 方案编辑 -->
    <el-dialog :title="planTitle" v-model="planOpen" width="820px" append-to-body>
      <el-form ref="planRef" :model="planForm" :rules="planRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="方案名称" prop="planName"><el-input v-model="planForm.planName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="计算口径" prop="calcBase"><el-select v-model="planForm.calcBase" style="width: 100%"><el-option v-for="d in baseOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="方案类型" prop="planType"><el-select v-model="planForm.planType" style="width: 100%"><el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="固定比例(%)" prop="baseRatio"><el-input-number v-model="planForm.baseRatio" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <template v-if="planForm.planType === '2'">
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="超额奖励起点"><el-input-number v-model="planForm.bonusThreshold" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="超额奖励比例(%)"><el-input-number v-model="planForm.bonusRatio" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
          </el-row>
        </template>
        <el-form-item v-if="planForm.planType === '1'" label="阶梯明细">
          <el-button type="primary" plain icon="Plus" size="small" @click="addTier">添加阶梯</el-button>
        </el-form-item>
        <el-table v-if="planForm.planType === '1'" :data="planForm.tiers" size="small">
          <el-table-column label="区间下限" width="150"><template #default="scope"><el-input-number v-model="scope.row.minAmount" :min="0" :precision="2" style="width: 100%" /></template></el-table-column>
          <el-table-column label="区间上限(空=不限)" width="170"><template #default="scope"><el-input-number v-model="scope.row.maxAmount" :min="0" :precision="2" style="width: 100%" /></template></el-table-column>
          <el-table-column label="比例(%)" width="130"><template #default="scope"><el-input-number v-model="scope.row.ratio" :min="0" :max="100" :precision="2" style="width: 100%" /></template></el-table-column>
          <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" @click="planForm.tiers.splice(scope.$index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-form-item label="备注"><el-input v-model="planForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitPlan">确 定</el-button><el-button @click="planOpen = false">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="HrCommission">
import { listPlan, getPlan, addPlan, updatePlan, delPlan, listBill, generateBills, confirmBill, lockBills, delBills } from '@/api/hr/commission'

const { proxy } = getCurrentInstance()
const { hr_commission_base: baseOptions, hr_commission_type: typeOptions, hr_bill_status: billStatusOptions } = proxy.useDict('hr_commission_base', 'hr_commission_type', 'hr_bill_status')

const activeTab = ref('plan')
const loading = ref(false)
const planList = ref([])
const billList = ref([])
const billTotal = ref(0)
const billQuery = ref({ pageNum: 1, pageSize: 10, period: undefined })
const planOpen = ref(false)
const planTitle = ref('')
const planForm = ref({ tiers: [] })
const planRules = {
  planName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  calcBase: [{ required: true, message: '请选择计算口径', trigger: 'change' }],
  planType: [{ required: true, message: '请选择方案类型', trigger: 'change' }]
}

function formatAmount(v) { return v != null ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00' }
function getPlanList() { loading.value = true; listPlan({ pageNum: 1, pageSize: 100 }).then(res => { planList.value = res.rows; loading.value = false }) }
function getBillList() { loading.value = true; listBill(billQuery.value).then(res => { billList.value = res.rows; billTotal.value = res.total; loading.value = false }) }
function handleAddPlan() { planForm.value = { planName: undefined, calcBase: undefined, planType: '0', baseRatio: undefined, bonusThreshold: undefined, bonusRatio: undefined, tiers: [] }; planTitle.value = '新增提成方案'; planOpen.value = true }
function handleEditPlan(row) { getPlan(row.planId).then(res => { planForm.value = res.data; planTitle.value = '修改提成方案'; planOpen.value = true }) }
function addTier() { planForm.value.tiers.push({ minAmount: 0, maxAmount: undefined, ratio: undefined }) }
function submitPlan() {
  proxy.$refs['planRef'].validate(valid => {
    if (valid) {
      if (planForm.value.planId) updatePlan(planForm.value).then(() => { proxy.$modal.msgSuccess('修改成功'); planOpen.value = false; getPlanList() })
      else addPlan(planForm.value).then(() => { proxy.$modal.msgSuccess('新增成功'); planOpen.value = false; getPlanList() })
    }
  })
}
function togglePlan(row) {
  const target = row.planStatus === '1' ? '2' : '1'
  updatePlan({ planId: row.planId, planStatus: target }).then(() => { proxy.$modal.msgSuccess(target === '1' ? '已启用' : '已停用'); getPlanList() })
}
function handleDelPlan(row) {
  proxy.$modal.confirm('确认删除方案 "' + row.planName + '"？').then(() => delPlan(row.planId)).then(() => { proxy.$modal.msgSuccess('删除成功'); getPlanList() }).catch(() => {})
}
function handleGenerate() {
  const period = billQuery.value.period || new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 7)
  proxy.$modal.confirm('确认对 ' + period + ' 周期生成提成核算表？将依据月度归集快照与各员工提成方案计算（MR-01 口径快照）。').then(() => generateBills(period)).then(res => {
    proxy.$modal.msgSuccess('已生成 ' + res.data + ' 条核算记录')
    getBillList()
  }).catch(() => {})
}
function handleConfirm(row) {
  proxy.$modal.confirm('确认该核算记录？').then(() => confirmBill(row.billId)).then(() => { proxy.$modal.msgSuccess('已确认'); getBillList() }).catch(() => {})
}
function handleLock() {
  const period = billQuery.value.period
  if (!period) { proxy.$modal.msgError('请先选择核算周期'); return }
  proxy.$modal.confirm('确认锁定 ' + period + ' 周期核算表？锁定后关联提成方案自动锁定不可追溯修改（MR-01/MR-03）。').then(() => lockBills(period)).then(() => { proxy.$modal.msgSuccess('已锁定'); getBillList() }).catch(() => {})
}
function handleDelBills() {
  const period = billQuery.value.period
  if (!period) { proxy.$modal.msgError('请先选择核算周期'); return }
  proxy.$modal.confirm('确认删除 ' + period + ' 周期待确认核算表？').then(() => delBills(period)).then(() => { proxy.$modal.msgSuccess('删除成功'); getBillList() }).catch(() => {})
}
getPlanList()
getBillList()
</script>
