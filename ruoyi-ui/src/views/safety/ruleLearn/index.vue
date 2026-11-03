<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="任务名称" prop="taskName"><el-input v-model="queryParams.taskName" placeholder="请输入任务名称" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="守则名称" prop="ruleName"><el-input v-model="queryParams.ruleName" placeholder="请输入守则名称" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="taskStatus"><el-select v-model="queryParams.taskStatus" placeholder="请选择" clearable style="width: 120px"><el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:ruleLearn:add']">发起学习任务</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="任务编号" prop="taskNo" width="170" show-overflow-tooltip />
      <el-table-column label="任务名称" prop="taskName" min-width="160" show-overflow-tooltip />
      <el-table-column label="守则" prop="ruleName" min-width="160" show-overflow-tooltip />
      <el-table-column label="学习版本" prop="ruleVersionNo" width="90" align="center" />
      <el-table-column label="学习对象" prop="targetDesc" min-width="140" show-overflow-tooltip />
      <el-table-column label="完成期限" prop="deadline" width="110" align="center"><template #default="scope"><span>{{ parseTime(scope.row.deadline, '{y}-{m}-{d}') }}</span></template></el-table-column>
      <el-table-column label="状态" prop="taskStatus" width="100" align="center"><template #default="scope"><dict-tag :options="statusOptions" :value="scope.row.taskStatus" /></template></el-table-column>
      <el-table-column label="操作" width="230" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleLearns(scope.row)" v-hasPermi="['safety:ruleLearn:query']">签阅名单</el-button>
          <el-button link type="success" icon="Plus" @click="handleAppend(scope.row)" v-hasPermi="['safety:ruleLearn:add']" v-if="scope.row.taskStatus !== '1'">追加人员</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 发起任务 -->
    <el-dialog title="发起学习任务" v-model="open" width="640px" append-to-body>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" placeholder="请输入任务名称" /></el-form-item>
        <el-form-item label="选择守则" prop="ruleId">
          <el-select v-model="form.ruleId" filterable placeholder="仅可选择「生效」守则" style="width: 100%">
            <el-option v-for="r in ruleOptions" :key="r.ruleId" :label="r.ruleName + '（v' + r.versionNo + '）'" :value="r.ruleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="学习对象" prop="targetDesc"><el-input v-model="form.targetDesc" placeholder="如：生产一部全体、包装岗" /></el-form-item>
        <el-form-item label="完成期限" prop="deadline"><el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="open = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 签阅名单 -->
    <el-dialog :title="'签阅名单 - ' + currentTask.taskName" v-model="learnOpen" width="820px" append-to-body>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"><template #title>SR-03：名单发起后只可追加不可减少；逾期未签阅由系统自动置为逾期并提醒</template></el-alert>
      <div style="margin-bottom:10px">
        <el-input v-model="appendUsers" placeholder="追加人员姓名，多个用逗号分隔（SR-03 只可追加）" style="width: 420px; margin-right: 8px" />
        <el-button type="primary" plain icon="Plus" @click="submitAppend" v-hasPermi="['safety:ruleLearn:add']">追加</el-button>
      </div>
      <el-table :data="learns" size="small" max-height="360">
        <el-table-column label="签阅人" prop="userName" width="120" />
        <el-table-column label="部门" prop="deptName" min-width="140" />
        <el-table-column label="状态" prop="signStatus" width="100" align="center"><template #default="scope"><dict-tag :options="learnStatusOptions" :value="scope.row.signStatus" /></template></el-table-column>
        <el-table-column label="签阅时间" width="160"><template #default="scope"><span>{{ scope.row.signTime ? parseTime(scope.row.signTime) : '-' }}</span></template></el-table-column>
        <el-table-column label="签阅终端" prop="signTerminal" width="90" align="center" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button link type="success" icon="Pointer" @click="handleSign(scope.row)" v-hasPermi="['safety:ruleLearn:sign']" v-if="scope.row.signStatus === '0'">签阅</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer><el-button @click="learnOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyRuleLearn">
import { listLearnTask, addLearnTask, listLearns, appendLearns, signLearn } from '@/api/safety/ruleLearn'
import { listRule } from '@/api/safety/rule'

const { proxy } = getCurrentInstance()
const { safety_rule_learn_status: statusOptions } = proxy.useDict('safety_rule_learn_status')
const learnStatusOptions = statusOptions

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const learnOpen = ref(false)
const currentTask = ref({})
const learns = ref([])
const appendUsers = ref('')
const ruleOptions = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } })
const { queryParams, form } = toRefs(data)
const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  ruleId: [{ required: true, message: '请选择守则', trigger: 'change' }],
  deadline: [{ required: true, message: '请设置完成期限', trigger: 'change' }]
}

function getList() { loading.value = true; listLearnTask(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function loadRuleOptions() { listRule({ pageNum: 1, pageSize: 200, ruleStatus: '1' }).then(res => { ruleOptions.value = res.rows || [] }) }
function handleAdd() {
  form.value = { taskName: undefined, ruleId: undefined, targetDesc: undefined, deadline: undefined, remark: undefined }
  loadRuleOptions()
  proxy.resetForm('taskRef')
  open.value = true
}
function submitForm() {
  proxy.$refs['taskRef'].validate(valid => {
    if (valid) addLearnTask(form.value).then(() => { proxy.$modal.msgSuccess('发起成功'); open.value = false; getList() })
  })
}
function handleLearns(row) { currentTask.value = row; appendUsers.value = ''; listLearns(row.taskId).then(res => { learns.value = res.data || []; learnOpen.value = true }) }
function submitAppend() {
  if (!appendUsers.value || !appendUsers.value.trim()) { proxy.$modal.msgError('请输入追加人员姓名'); return }
  const names = appendUsers.value.split(/[,，]/).map(s => s.trim()).filter(s => s)
  const payload = names.map(n => ({ userName: n }))
  appendLearns(currentTask.value.taskId, payload).then(res => {
    proxy.$modal.msgSuccess('追加成功（已存在人员自动跳过）')
    appendUsers.value = ''
    listLearns(currentTask.value.taskId).then(r => { learns.value = r.data || [] })
    getList()
  })
}
function handleSign(row) {
  proxy.$modal.confirm('确认为 "' + row.userName + '" 完成签阅？签阅后将记录签阅人、时间、IP 与终端。').then(() => signLearn({ learnId: row.learnId })).then(() => {
    proxy.$modal.msgSuccess('签阅成功')
    listLearns(currentTask.value.taskId).then(r => { learns.value = r.data || [] })
    getList()
  }).catch(() => {})
}
getList()
</script>
