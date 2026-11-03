<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="考核方案" name="scheme" />
      <el-tab-pane label="考核记录" name="record" />
    </el-tabs>

    <!-- 方案 -->
    <div v-show="activeTab === 'scheme'">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddScheme" v-hasPermi="['hr:kpi:add']">新增方案</el-button></el-col>
      </el-row>
      <el-table v-loading="loading" :data="schemeList">
        <el-table-column label="方案名称" prop="schemeName" min-width="180" show-overflow-tooltip />
        <el-table-column label="考核周期" prop="periodType" width="100" align="center"><template #default="scope"><dict-tag :options="periodTypeOptions" :value="scope.row.periodType" /></template></el-table-column>
        <el-table-column label="状态" prop="schemeStatus" width="90" align="center"><template #default="scope"><el-tag :type="scope.row.schemeStatus === '1' ? 'success' : 'info'">{{ scope.row.schemeStatus === '1' ? '启用' : scope.row.schemeStatus === '2' ? '停用' : '草稿' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="260" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEditScheme(scope.row)" v-hasPermi="['hr:kpi:add']">编辑</el-button>
            <el-button link :type="scope.row.schemeStatus === '1' ? 'warning' : 'success'" @click="toggleScheme(scope.row)" v-hasPermi="['hr:kpi:add']">{{ scope.row.schemeStatus === '1' ? '停用' : '启用' }}</el-button>
            <el-button link type="success" icon="Position" @click="handleInitiate(scope.row)" v-hasPermi="['hr:kpi:initiate']">发起考核</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelScheme(scope.row)" v-hasPermi="['hr:kpi:add']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 考核记录 -->
    <div v-show="activeTab === 'record'">
      <el-form :inline="true">
        <el-form-item label="考核周期"><el-date-picker v-model="recordQuery.period" type="month" value-format="YYYY-MM" placeholder="选择月份" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="Search" @click="getRecordList">搜索</el-button></el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="recordList">
        <el-table-column label="考核方案" prop="schemeName" min-width="150" show-overflow-tooltip />
        <el-table-column label="周期" prop="period" width="90" align="center" />
        <el-table-column label="员工" prop="employeeName" width="100" />
        <el-table-column label="自评分" prop="selfScore" width="80" align="center" />
        <el-table-column label="上级评分" prop="superiorScore" width="90" align="center" />
        <el-table-column label="最终得分" prop="finalScore" width="90" align="center" />
        <el-table-column label="状态" prop="kpiStatus" width="90" align="center"><template #default="scope"><dict-tag :options="kpiStatusOptions" :value="scope.row.kpiStatus" /></template></el-table-column>
        <el-table-column label="操作" width="300" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openScore(scope.row, 'self')" v-hasPermi="['hr:kpi:score']" v-if="scope.row.kpiStatus === '0'">自评</el-button>
            <el-button link type="primary" @click="openScore(scope.row, 'superior')" v-hasPermi="['hr:kpi:score']" v-if="scope.row.kpiStatus === '1'">评分</el-button>
            <el-button link type="success" @click="handleConfirm(scope.row)" v-hasPermi="['hr:kpi:confirm']" v-if="scope.row.kpiStatus === '2'">确认</el-button>
            <el-button link type="warning" @click="openAppeal(scope.row)" v-hasPermi="['hr:kpi:appeal']" v-if="scope.row.kpiStatus === '3'">申诉</el-button>
            <el-button link type="info" @click="openAppealHandle(scope.row)" v-hasPermi="['hr:kpi:confirm']" v-if="scope.row.kpiStatus === '4'">处理申诉</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="recordTotal > 0" :total="recordTotal" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" @pagination="getRecordList" />
    </div>

    <!-- 方案编辑 -->
    <el-dialog :title="schemeTitle" v-model="schemeOpen" width="820px" append-to-body>
      <el-form ref="schemeRef" :model="schemeForm" :rules="schemeRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="方案名称" prop="schemeName"><el-input v-model="schemeForm.schemeName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="考核周期" prop="periodType"><el-select v-model="schemeForm.periodType" style="width: 100%"><el-option v-for="d in periodTypeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="考核指标">
          <el-button type="primary" plain icon="Plus" size="small" @click="addItem">添加指标</el-button>
          <span style="margin-left:12px;color:#909399;font-size:12px">权重合计须=100</span>
        </el-form-item>
        <el-table :data="schemeForm.items" size="small">
          <el-table-column label="指标名称" min-width="160"><template #default="scope"><el-input v-model="scope.row.indicatorName" placeholder="如：销售额/回款率/新客户数" /></template></el-table-column>
          <el-table-column label="权重(%)" width="110"><template #default="scope"><el-input-number v-model="scope.row.weight" :min="0" :max="100" :precision="2" style="width: 100%" /></template></el-table-column>
          <el-table-column label="目标值" width="130"><template #default="scope"><el-input v-model="scope.row.targetValue" /></template></el-table-column>
          <el-table-column label="评分规则" min-width="160"><template #default="scope"><el-input v-model="scope.row.scoreRule" /></template></el-table-column>
          <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" @click="schemeForm.items.splice(scope.$index, 1)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitScheme">确 定</el-button><el-button @click="schemeOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 发起考核 -->
    <el-dialog title="发起考核" v-model="initiateOpen" width="560px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="考核周期"><el-date-picker v-model="initiateForm.period" type="month" value-format="YYYY-MM" style="width: 100%" /></el-form-item>
        <el-form-item label="考核对象"><el-input v-model="initiateForm.employeeIdsText" placeholder="员工ID列表，逗号分隔（可在营销档案页查询）" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitInitiate">发 起</el-button><el-button @click="initiateOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 评分 -->
    <el-dialog :title="scoreType === 'self' ? '员工自评' : '上级评分'" v-model="scoreOpen" width="520px" append-to-body>
      <el-form label-width="100px">
        <el-form-item :label="scoreType === 'self' ? '自评分' : '上级评分'"><el-input-number v-model="scoreForm.score" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item :label="scoreType === 'self' ? '自评说明' : '上级评语'"><el-input v-model="scoreForm.comment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitScore">提 交</el-button><el-button @click="scoreOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 申诉 -->
    <el-dialog title="考核申诉" v-model="appealOpen" width="520px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="申诉理由"><el-input v-model="appealForm.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitAppeal">提 交</el-button><el-button @click="appealOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 申诉处理 -->
    <el-dialog title="申诉处理" v-model="appealHandleOpen" width="520px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="处理结果"><el-input v-model="appealHandleForm.result" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="调整后得分"><el-input-number v-model="appealHandleForm.finalScore" :min="0" :max="100" :precision="2" style="width: 100%" placeholder="留空则维持原得分" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitAppealHandle">完 成</el-button><el-button @click="appealHandleOpen = false">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="HrKpi">
import { listScheme, getScheme, addScheme, updateScheme, delScheme, listKpiRecord, initiateRecords, selfScore, superiorScore, confirmRecord, appealRecord, handleAppeal } from '@/api/hr/kpi'

const { proxy } = getCurrentInstance()
const { hr_kpi_period_type: periodTypeOptions, hr_kpi_status: kpiStatusOptions } = proxy.useDict('hr_kpi_period_type', 'hr_kpi_status')

const activeTab = ref('scheme')
const loading = ref(false)
const schemeList = ref([])
const recordList = ref([])
const recordTotal = ref(0)
const recordQuery = ref({ pageNum: 1, pageSize: 10, period: undefined })
const schemeOpen = ref(false)
const schemeTitle = ref('')
const schemeForm = ref({ items: [] })
const initiateOpen = ref(false)
const initiateForm = ref({})
const scoreOpen = ref(false)
const scoreType = ref('self')
const scoreForm = ref({})
const appealOpen = ref(false)
const appealForm = ref({})
const appealHandleOpen = ref(false)
const appealHandleForm = ref({})
const schemeRules = {
  schemeName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  periodType: [{ required: true, message: '请选择考核周期', trigger: 'change' }]
}

function getSchemeList() { loading.value = true; listScheme({ pageNum: 1, pageSize: 100 }).then(res => { schemeList.value = res.rows; loading.value = false }) }
function getRecordList() { loading.value = true; listKpiRecord(recordQuery.value).then(res => { recordList.value = res.rows; recordTotal.value = res.total; loading.value = false }) }
function handleAddScheme() { schemeForm.value = { schemeName: undefined, periodType: undefined, items: [{}] }; schemeTitle.value = '新增考核方案'; schemeOpen.value = true }
function handleEditScheme(row) { getScheme(row.schemeId).then(res => { schemeForm.value = res.data; schemeTitle.value = '修改考核方案'; schemeOpen.value = true }) }
function addItem() { schemeForm.value.items.push({ indicatorName: undefined, weight: undefined, targetValue: undefined, scoreRule: undefined }) }
function submitScheme() {
  proxy.$refs['schemeRef'].validate(valid => {
    if (valid) {
      if (schemeForm.value.schemeId) updateScheme(schemeForm.value).then(() => { proxy.$modal.msgSuccess('修改成功'); schemeOpen.value = false; getSchemeList() })
      else addScheme(schemeForm.value).then(() => { proxy.$modal.msgSuccess('新增成功'); schemeOpen.value = false; getSchemeList() })
    }
  })
}
function toggleScheme(row) {
  const target = row.schemeStatus === '1' ? '2' : '1'
  updateScheme({ schemeId: row.schemeId, schemeStatus: target }).then(() => { proxy.$modal.msgSuccess(target === '1' ? '已启用' : '已停用'); getSchemeList() })
}
function handleDelScheme(row) {
  proxy.$modal.confirm('确认删除方案 "' + row.schemeName + '"？').then(() => delScheme(row.schemeId)).then(() => { proxy.$modal.msgSuccess('删除成功'); getSchemeList() }).catch(() => {})
}
function handleInitiate(row) { initiateForm.value = { schemeId: row.schemeId, period: undefined, employeeIdsText: '' }; initiateOpen.value = true }
function submitInitiate() {
  const ids = (initiateForm.value.employeeIdsText || '').split(/[,，]/).map(s => s.trim()).filter(s => s).map(Number)
  if (!initiateForm.value.period || ids.length === 0) { proxy.$modal.msgError('请填写考核周期与考核对象'); return }
  initiateRecords({ schemeId: initiateForm.value.schemeId, period: initiateForm.value.period, employeeIds: ids }).then(res => {
    proxy.$modal.msgSuccess('已发起 ' + res.data + ' 条考核记录')
    initiateOpen.value = false
    activeTab.value = 'record'
    getRecordList()
  })
}
function openScore(row, type) { scoreType.value = type; scoreForm.value = { recordId: row.recordId, score: type === 'self' ? row.selfScore : row.superiorScore, comment: type === 'self' ? row.selfComment : row.superiorComment }; scoreOpen.value = true }
function submitScore() {
  const payload = scoreType.value === 'self'
    ? { recordId: scoreForm.value.recordId, selfScore: scoreForm.value.score, selfComment: scoreForm.value.comment }
    : { recordId: scoreForm.value.recordId, superiorScore: scoreForm.value.score, superiorComment: scoreForm.value.comment }
  const fn = scoreType.value === 'self' ? selfScore : superiorScore
  fn(payload).then(() => { proxy.$modal.msgSuccess('提交成功'); scoreOpen.value = false; getRecordList() })
}
function handleConfirm(row) {
  proxy.$modal.confirm('确认该考核结果？').then(() => confirmRecord(row.recordId)).then(() => { proxy.$modal.msgSuccess('已确认'); getRecordList() }).catch(() => {})
}
function openAppeal(row) { appealForm.value = { recordId: row.recordId, reason: '' }; appealOpen.value = true }
function submitAppeal() {
  if (!appealForm.value.reason) { proxy.$modal.msgError('请填写申诉理由'); return }
  appealRecord(appealForm.value).then(() => { proxy.$modal.msgSuccess('申诉已提交'); appealOpen.value = false; getRecordList() })
}
function openAppealHandle(row) { appealHandleForm.value = { recordId: row.recordId, result: '', finalScore: row.finalScore }; appealHandleOpen.value = true }
function submitAppealHandle() {
  if (!appealHandleForm.value.result) { proxy.$modal.msgError('请填写处理结果'); return }
  handleAppeal(appealHandleForm.value).then(() => { proxy.$modal.msgSuccess('申诉处理完成'); appealHandleOpen.value = false; getRecordList() })
}
getSchemeList()
getRecordList()
</script>
