<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="检查编号" prop="checkNo"><el-input v-model="queryParams.checkNo" placeholder="请输入检查编号" clearable style="width: 180px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="守则名称" prop="ruleName"><el-input v-model="queryParams.ruleName" placeholder="请输入守则名称" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="检查结果" prop="checkResult"><el-select v-model="queryParams.checkResult" placeholder="请选择" clearable style="width: 120px"><el-option v-for="d in resultOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="checkStatus"><el-select v-model="queryParams.checkStatus" placeholder="请选择" clearable style="width: 120px"><el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:ruleCheck:add']">新增检查</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:ruleCheck:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="检查编号" prop="checkNo" width="170" show-overflow-tooltip />
      <el-table-column label="检查日期" prop="checkDate" width="110" align="center"><template #default="scope"><span>{{ parseTime(scope.row.checkDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
      <el-table-column label="检查人" prop="checker" width="100" />
      <el-table-column label="被检查部门/岗位" prop="deptName" min-width="130" show-overflow-tooltip />
      <el-table-column label="关联守则" prop="ruleName" min-width="150" show-overflow-tooltip />
      <el-table-column label="检查结果" prop="checkResult" width="100" align="center"><template #default="scope"><dict-tag :options="resultOptions" :value="scope.row.checkResult" /></template></el-table-column>
      <el-table-column label="整改责任人" prop="rectifyUser" width="110" />
      <el-table-column label="整改期限" prop="rectifyDeadline" width="110" align="center"><template #default="scope"><span>{{ scope.row.rectifyDeadline ? parseTime(scope.row.rectifyDeadline, '{y}-{m}-{d}') : '-' }}</span></template></el-table-column>
      <el-table-column label="状态" prop="checkStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.checkStatus)">{{ statusLabel(scope.row.checkStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:ruleCheck:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:ruleCheck:edit']" v-if="scope.row.checkStatus !== '3'">修改</el-button>
          <el-button link type="success" icon="CircleCheck" @click="handleReview(scope.row)" v-hasPermi="['safety:ruleCheck:review']" v-if="scope.row.checkResult === '2' && scope.row.checkStatus !== '3'">整改闭环</el-button>
          <el-button link type="info" icon="FolderChecked" @click="handleArchive(scope.row)" v-hasPermi="['safety:ruleCheck:archive']" v-if="scope.row.checkStatus === '2' || (scope.row.checkResult !== '2' && scope.row.checkStatus === '2')">归档</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:ruleCheck:remove']" v-if="scope.row.checkStatus !== '3'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="checkRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="检查编号"><el-input v-model="form.checkNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检查日期" prop="checkDate"><el-date-picker v-model="form.checkDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="检查人" prop="checker"><el-input v-model="form.checker" placeholder="请输入检查人" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="被检查部门" prop="deptName"><el-input v-model="form.deptName" placeholder="请输入被检查部门/岗位" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="关联守则" prop="ruleId">
            <el-select v-model="form.ruleId" filterable clearable placeholder="请选择守则" style="width: 100%" @change="onRuleChange">
              <el-option v-for="r in ruleOptions" :key="r.ruleId" :label="r.ruleName + '（v' + r.versionNo + '）'" :value="r.ruleId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检查结果" prop="checkResult">
            <el-select v-model="form.checkResult" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in resultOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="不符合描述" prop="problemDesc" v-if="form.checkResult === '2'"><el-input v-model="form.problemDesc" type="textarea" :rows="2" placeholder="请描述不符合项" /></el-form-item>
        <template v-if="form.checkResult === '2'">
          <el-form-item label="整改要求" prop="rectifyRequirement"><el-input v-model="form.rectifyRequirement" type="textarea" :rows="2" placeholder="请输入整改要求" /></el-form-item>
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="整改责任人" prop="rectifyUser"><el-input v-model="form.rectifyUser" placeholder="SR-04 必填" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="整改期限" prop="rectifyDeadline"><el-date-picker v-model="form.rectifyDeadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          </el-row>
          <el-form-item label="整改结果" prop="rectifyResult"><el-input v-model="form.rectifyResult" type="textarea" :rows="2" placeholder="整改完成后填写（闭环复查时必填）" /></el-form-item>
        </template>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="open = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 整改闭环 -->
    <el-dialog title="整改闭环复查（SR-04）" v-model="reviewOpen" width="560px" append-to-body>
      <el-form ref="reviewRef" :model="reviewForm" :rules="reviewRules" label-width="110px">
        <el-form-item label="整改结果" prop="rectifyResult"><el-input v-model="reviewForm.rectifyResult" type="textarea" :rows="3" placeholder="SR-04：不符合项必须填写整改结果后才能闭环" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitReview">复查闭环</el-button><el-button @click="reviewOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 查看 -->
    <el-dialog title="检查记录详情" v-model="viewOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border v-if="viewData">
        <el-descriptions-item label="检查编号">{{ viewData.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="检查日期">{{ viewData.checkDate ? parseTime(viewData.checkDate, '{y}-{m}-{d}') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查人">{{ viewData.checker || '-' }}</el-descriptions-item>
        <el-descriptions-item label="被检查部门/岗位">{{ viewData.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联守则">{{ viewData.ruleName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检查结果"><dict-tag :options="resultOptions" :value="viewData.checkResult" /></el-descriptions-item>
        <el-descriptions-item label="不符合描述" :span="2">{{ viewData.problemDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改要求" :span="2">{{ viewData.rectifyRequirement || '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改责任人">{{ viewData.rectifyUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改期限">{{ viewData.rectifyDeadline ? parseTime(viewData.rectifyDeadline, '{y}-{m}-{d}') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改结果" :span="2">{{ viewData.rectifyResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="复查人">{{ viewData.reviewer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="复查时间">{{ viewData.reviewTime ? parseTime(viewData.reviewTime) : '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyRuleCheck">
import { listRuleCheck, getRuleCheck, addRuleCheck, updateRuleCheck, reviewRuleCheck, archiveRuleCheck, delRuleCheck, ruleCheckStats } from '@/api/safety/ruleCheck'
import { listRule } from '@/api/safety/rule'

const { proxy } = getCurrentInstance()
const { safety_rule_check_result: resultOptions } = proxy.useDict('safety_rule_check_result')

const statusMap = { '0': { label: '待整改', type: 'warning' }, '1': { label: '待复查', type: 'warning' }, '2': { label: '已闭环', type: 'success' }, '3': { label: '已归档', type: 'info' } }
const statusOptions = ref(Object.keys(statusMap).map(k => ({ label: statusMap[k].label, value: k })))

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref(null)
const reviewOpen = ref(false)
const reviewForm = ref({})
const ruleOptions = ref([])
const title = ref('')
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } })
const { queryParams, form } = toRefs(data)
const rules = {
  checkDate: [{ required: true, message: '请选择检查日期', trigger: 'change' }],
  checker: [{ required: true, message: '请输入检查人', trigger: 'blur' }],
  checkResult: [{ required: true, message: '请选择检查结果', trigger: 'change' }],
  rectifyUser: [{ required: true, message: '不符合项必须填写整改责任人（SR-04）', trigger: 'blur' }],
  rectifyDeadline: [{ required: true, message: '不符合项必须填写整改期限（SR-04）', trigger: 'change' }]
}
const reviewRules = { rectifyResult: [{ required: true, message: 'SR-04：不符合项必须填写整改结果后才能闭环', trigger: 'blur' }] }

function statusLabel(v) { return statusMap[v] ? statusMap[v].label : v }
function statusType(v) { return statusMap[v] ? statusMap[v].type : 'info' }

function getList() { loading.value = true; listRuleCheck(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function loadRuleOptions() { listRule({ pageNum: 1, pageSize: 200 }).then(res => { ruleOptions.value = res.rows || [] }) }
function onRuleChange(ruleId) { const r = ruleOptions.value.find(x => x.ruleId === ruleId); if (r) form.value.ruleName = r.ruleName }
function reset() {
  form.value = { checkId: null, checkNo: undefined, checkDate: new Date().toISOString().slice(0, 10), checker: undefined, deptId: undefined, deptName: undefined, ruleId: undefined, ruleName: undefined, checkResult: undefined, problemDesc: undefined, rectifyRequirement: undefined, rectifyUser: undefined, rectifyDeadline: undefined, rectifyResult: undefined, remark: undefined }
  proxy.resetForm('checkRef')
}
function handleAdd() { reset(); loadRuleOptions(); open.value = true; title.value = '新增守则执行检查' }
function handleUpdate(row) { reset(); getRuleCheck(row.checkId).then(res => { form.value = res.data; loadRuleOptions(); open.value = true; title.value = '修改守则执行检查' }) }
function handleView(row) { getRuleCheck(row.checkId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleReview(row) { reviewForm.value = { checkId: row.checkId, rectifyResult: row.rectifyResult }; reviewOpen.value = true }
function submitReview() {
  proxy.$refs['reviewRef'].validate(valid => {
    if (valid) reviewRuleCheck(reviewForm.value).then(() => { proxy.$modal.msgSuccess('复查闭环成功'); reviewOpen.value = false; getList() })
  })
}
function handleArchive(row) {
  // SR-04：不符合项必须整改闭环后才能归档
  proxy.$modal.confirm('确认归档该检查记录？不符合项必须整改闭环后才能归档（SR-04）。').then(() => archiveRuleCheck(row.checkId)).then(() => { getList(); proxy.$modal.msgSuccess('归档成功') }).catch(() => {})
}
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该检查记录？').then(() => delRuleCheck(row.checkId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('safety/ruleCheck/export', { ...queryParams.value }, `safety_rule_check_${new Date().getTime()}.xlsx`) }
function submitForm() {
  proxy.$refs['checkRef'].validate(valid => {
    if (valid) {
      if (form.value.checkId) { updateRuleCheck(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addRuleCheck(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
getList()
</script>
