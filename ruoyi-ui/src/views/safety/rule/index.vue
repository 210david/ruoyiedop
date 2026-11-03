<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="守则名称" prop="ruleName"><el-input v-model="queryParams.ruleName" placeholder="请输入守则名称" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="适用部门" prop="deptName"><el-input v-model="queryParams.deptName" placeholder="请输入适用部门" clearable style="width: 160px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="ruleStatus"><el-select v-model="queryParams.ruleStatus" placeholder="请选择" clearable style="width: 120px"><el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:rule:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:rule:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="守则编号" prop="ruleNo" width="170" show-overflow-tooltip />
      <el-table-column label="守则名称" prop="ruleName" min-width="180" show-overflow-tooltip />
      <el-table-column label="适用部门" prop="deptName" width="140" show-overflow-tooltip />
      <el-table-column label="适用岗位" prop="postName" width="120" show-overflow-tooltip />
      <el-table-column label="作业场景" prop="workScene" width="140" show-overflow-tooltip />
      <el-table-column label="版本" prop="versionNo" width="70" align="center" />
      <el-table-column label="状态" prop="ruleStatus" width="90" align="center">
        <template #default="scope"><dict-tag :options="statusOptions" :value="scope.row.ruleStatus" /></template>
      </el-table-column>
      <el-table-column label="生效日期" prop="effectiveDate" width="110" align="center"><template #default="scope"><span>{{ parseTime(scope.row.effectiveDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
      <el-table-column label="关联制度（SR-01 弱关联）" prop="regulationName" min-width="150" show-overflow-tooltip><template #default="scope"><span>{{ scope.row.regulationName || '-' }}</span></template></el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:rule:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:rule:edit']" v-if="scope.row.ruleStatus !== '2'">修改</el-button>
          <el-button link type="success" icon="Position" @click="handlePublish(scope.row)" v-hasPermi="['safety:rule:publish']" v-if="scope.row.ruleStatus === '0'">发布</el-button>
          <el-button link type="warning" icon="TurnOff" @click="handleAbolish(scope.row)" v-hasPermi="['safety:rule:abolish']" v-if="scope.row.ruleStatus === '1'">废止</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:rule:remove']" v-if="scope.row.ruleStatus === '0'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改 -->
    <el-dialog :title="title" v-model="open" width="820px" append-to-body>
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="守则编号"><el-input v-model="form.ruleNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="守则名称" prop="ruleName"><el-input v-model="form.ruleName" placeholder="请输入守则名称" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="适用部门" prop="deptName"><el-input v-model="form.deptName" placeholder="请输入适用部门" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用岗位" prop="postName"><el-input v-model="form.postName" placeholder="请输入适用岗位" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="适用作业场景" prop="workScene"><el-input v-model="form.workScene" placeholder="请输入适用作业场景" /></el-form-item>
        <el-form-item label="守则内容" prop="ruleContent"><el-input v-model="form.ruleContent" type="textarea" :rows="6" placeholder="请输入守则内容" /></el-form-item>
        <el-form-item label="附件" prop="attachment"><el-input v-model="form.attachment" placeholder="附件地址（图片/文件）" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="关联制度名称" prop="regulationName"><el-input v-model="form.regulationName" placeholder="SR-01 弱关联，选填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联制度附件" prop="regulationAttachment"><el-input v-model="form.regulationAttachment" placeholder="选填" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="失效日期" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="编制人" prop="author"><el-input v-model="form.author" placeholder="请输入编制人" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="审批人" prop="approver"><el-input v-model="form.approver" placeholder="请输入审批人" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- 查看（含历史版本） -->
    <el-dialog title="守则详情与历史版本" v-model="viewOpen" width="860px" append-to-body>
      <el-descriptions :column="2" border v-if="viewData">
        <el-descriptions-item label="守则编号">{{ viewData.ruleNo }}</el-descriptions-item>
        <el-descriptions-item label="守则名称">{{ viewData.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="适用部门">{{ viewData.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="适用岗位">{{ viewData.postName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作业场景">{{ viewData.workScene || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ viewData.versionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="statusOptions" :value="viewData.ruleStatus" /></el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ viewData.effectiveDate ? parseTime(viewData.effectiveDate, '{y}-{m}-{d}') : '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联制度">{{ viewData.regulationName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="覆盖率（应签/已签）">{{ learnStat.total }} / {{ learnStat.signed }}</el-descriptions-item>
        <el-descriptions-item label="守则内容" :span="2"><div style="white-space: pre-wrap">{{ viewData.ruleContent || '-' }}</div></el-descriptions-item>
      </el-descriptions>
      <h4 style="margin:16px 0 8px">历史版本（变更留痕，可回溯）</h4>
      <el-table :data="versions" size="small">
        <el-table-column label="版本" prop="versionNo" width="70" align="center" />
        <el-table-column label="守则名称" prop="ruleName" show-overflow-tooltip />
        <el-table-column label="当时状态" prop="ruleStatus" width="90" align="center"><template #default="scope"><dict-tag :options="statusOptions" :value="scope.row.ruleStatus" /></template></el-table-column>
        <el-table-column label="变更说明" prop="changeNote" show-overflow-tooltip />
        <el-table-column label="变更人" prop="changeBy" width="100" />
        <el-table-column label="变更时间" width="160"><template #default="scope"><span>{{ parseTime(scope.row.changeTime) }}</span></template></el-table-column>
      </el-table>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyRule">
import { listRule, getRule, addRule, updateRule, delRule, publishRule, abolishRule, listRuleVersions, ruleLearnStats } from '@/api/safety/rule'

const { proxy } = getCurrentInstance()
const { safety_rule_status: statusOptions } = proxy.useDict('safety_rule_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref(null)
const versions = ref([])
const learnStat = ref({ total: 0, signed: 0 })
const title = ref('')
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } })
const { queryParams, form } = toRefs(data)
const rules = {
  ruleName: [{ required: true, message: '请输入守则名称', trigger: 'blur' }],
  ruleContent: [{ required: true, message: '请输入守则内容', trigger: 'blur' }]
}

function getList() { loading.value = true; listRule(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function reset() { form.value = { ruleId: null, ruleNo: undefined, ruleName: undefined, deptName: undefined, postName: undefined, workScene: undefined, ruleContent: undefined, attachment: undefined, regulationName: undefined, regulationAttachment: undefined, effectiveDate: undefined, expireDate: undefined, author: undefined, approver: undefined, remark: undefined }; proxy.resetForm('ruleRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增安全守则' }
function handleUpdate(row) { reset(); getRule(row.ruleId).then(res => { form.value = res.data; open.value = true; title.value = '修改安全守则' }) }
function handleView(row) {
  getRule(row.ruleId).then(res => { viewData.value = res.data; viewOpen.value = true })
  listRuleVersions(row.ruleId).then(res => { versions.value = res.data || [] })
  ruleLearnStats(row.ruleId).then(res => { learnStat.value = res.data || { total: 0, signed: 0 } })
}
function handlePublish(row) {
  proxy.$modal.confirm('确认发布守则 "' + row.ruleName + '"？发布后将生成新版本并留痕，且可发起学习任务。').then(() => publishRule(row.ruleId)).then(() => { getList(); proxy.$modal.msgSuccess('发布成功') }).catch(() => {})
}
function handleAbolish(row) {
  // SR-02：生效守则不允许删除，只可废止并留痕
  proxy.$modal.confirm('确认废止守则 "' + row.ruleName + '"？废止后守则保留留痕，不可删除。').then(() => abolishRule(row.ruleId)).then(() => { getList(); proxy.$modal.msgSuccess('已废止') }).catch(() => {})
}
function handleDelete(row) {
  proxy.$modal.confirm('确认删除草稿守则 "' + row.ruleName + '"？生效守则不允许删除。').then(() => delRule(row.ruleId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('safety/rule/export', { ...queryParams.value }, `safety_rule_${new Date().getTime()}.xlsx`) }
function submitForm() {
  proxy.$refs['ruleRef'].validate(valid => {
    if (valid) {
      if (form.value.ruleId) { updateRule(form.value).then(() => { proxy.$modal.msgSuccess('修改成功，内容变化自动生成新版本'); open.value = false; getList() }) }
      else { addRule(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function cancel() { open.value = false; reset() }
getList()
</script>
