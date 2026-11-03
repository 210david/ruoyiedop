<template>
  <div class="app-container safety-rulelearn-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>任务名称</label>
          <div class="control">
            <el-input v-model="queryParams.taskName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>守则名称</label>
          <div class="control">
            <el-input v-model="queryParams.ruleName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in taskStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in taskStatusOptions" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:ruleLearn:add']">发起学习任务</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_ruleLearn_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="任务编号" prop="taskNo" key="taskNo" :width="colWidth('taskNo', 170)" resizable show-overflow-tooltip v-if="columns.taskNo.visible" />
          <el-table-column label="任务名称" prop="taskName" key="taskName" :min-width="colWidth('taskName', 170)" resizable show-overflow-tooltip v-if="columns.taskName.visible" />
          <el-table-column label="守则" prop="ruleName" key="ruleName" :min-width="colWidth('ruleName', 160)" resizable show-overflow-tooltip v-if="columns.ruleName.visible" />
          <el-table-column label="学习版本" prop="ruleVersionNo" key="ruleVersionNo" :width="colWidth('ruleVersionNo', 90)" resizable align="center" v-if="columns.ruleVersionNo.visible"><template #default="scope"><span>v{{ scope.row.ruleVersionNo || 1 }}</span></template></el-table-column>
          <el-table-column label="学习对象" prop="targetDesc" key="targetDesc" :min-width="colWidth('targetDesc', 150)" resizable show-overflow-tooltip v-if="columns.targetDesc.visible" />
          <el-table-column label="完成期限" prop="deadline" key="deadline" :width="colWidth('deadline', 110)" resizable align="center" v-if="columns.deadline.visible"><template #default="scope"><span>{{ parseTime(scope.row.deadline, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="状态" prop="taskStatus" key="taskStatus" :width="colWidth('taskStatus', 100)" resizable align="center" v-if="columns.taskStatus.visible"><template #default="scope"><span v-if="dictLabel(taskStatusOptions, scope.row.taskStatus)" class="badge" :class="statusClass(scope.row.taskStatus)"><span class="dot"></span>{{ dictLabel(taskStatusOptions, scope.row.taskStatus) }}</span></template></el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 220)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleLearns(scope.row)" v-hasPermi="['safety:ruleLearn:query']">签阅名单</el-button>
                <el-button link type="success" icon="Plus" @click="handleAppend(scope.row)" v-hasPermi="['safety:ruleLearn:add']" v-if="scope.row.taskStatus !== '1'">追加人员</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- 发起学习任务 Dialog -->
    <el-dialog v-model="open" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">SR-03：名单发起后只可追加，不可减少</span></div>
          </div>
        </div>
      </template>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>任务信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec0">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"><template #title>SR-03：学习任务发起后名单不可减少只可追加；仅「生效」状态的守则可发起学习任务</template></el-alert>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" placeholder="请输入任务名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="选择守则" prop="ruleId">
                  <el-select v-model="form.ruleId" filterable placeholder="仅可选择「生效」守则" style="width: 100%">
                    <el-option v-for="r in ruleOptions" :key="r.ruleId" :label="r.ruleName + '（v' + r.versionNo + '）'" :value="r.ruleId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="学习对象" prop="targetDesc">
                  <el-input v-model="form.targetDesc" readonly placeholder="请选择学习人员（可多选）" @click="openTargetPicker">
                    <template #append><el-button icon="Search" @click="openTargetPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="完成期限" prop="deadline"><el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20" v-if="form.learns && form.learns.length">
                <el-col :span="24"><el-form-item label="已选人员">
                  <el-tag v-for="u in form.learns" :key="u.userId" closable style="margin-right:6px" @close="removeLearner(u)">{{ u.nickName }}</el-tag>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec1">
              <el-form-item label="备注" label-width="110px"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- 选择器 -->
    <user-picker ref="targetPickerRef" title="选择学习对象（可多选）" multiple @confirm="onTargetPickerConfirm" />
    <user-picker ref="appendPickerRef" title="选择追加人员（可多选）" multiple @confirm="onAppendPickerConfirm" />
    <!-- 签阅名单 Dialog -->
    <el-dialog v-model="learnOpen" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">签阅名单</div>
            <div class="rd-detail-header-sub" v-if="currentTask.taskName"><span class="rd-detail-header-no">任务：{{ currentTask.taskName }}（守则：{{ currentTask.ruleName }} v{{ currentTask.ruleVersionNo }}）</span></div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg></span>追加人员（SR-03 名单只可追加，已存在人员自动跳过）</div>
          </div>
          <div class="rd-card-body">
            <div style="display:flex; gap:8px; align-items:center;">
              <el-input :model-value="appendTargetNames" readonly placeholder="请选择追加人员（可多选）" @click="openAppendPicker" style="flex:1">
                <template #append><el-button icon="Search" @click="openAppendPicker" /></template>
              </el-input>
              <el-button type="primary" plain icon="Plus" @click="submitAppend" v-hasPermi="['safety:ruleLearn:add']">追加</el-button>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>签阅记录（签阅后记录签阅人/时间/IP/终端）</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="learns" size="small" border>
              <el-table-column label="签阅人" prop="userName" width="120" />
              <el-table-column label="部门" prop="deptName" min-width="140" />
              <el-table-column label="状态" prop="signStatus" width="110" align="center"><template #default="scope"><span v-if="dictLabel(learnStatusOptions, scope.row.signStatus)" class="badge" :class="statusClass(scope.row.signStatus)"><span class="dot"></span>{{ dictLabel(learnStatusOptions, scope.row.signStatus) }}</span></template></el-table-column>
              <el-table-column label="签阅时间" width="160"><template #default="scope"><span>{{ scope.row.signTime ? parseTime(scope.row.signTime) : '-' }}</span></template></el-table-column>
              <el-table-column label="签阅终端" prop="signTerminal" width="90" align="center" />
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button link type="success" icon="Pointer" @click="handleSign(scope.row)" v-hasPermi="['safety:ruleLearn:sign']" v-if="scope.row.signStatus === '0'">签阅</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="learnOpen = false">关 闭</el-button></template>
    </el-dialog>
    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="守则学习任务业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、学习任务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是守则学习任务？</div>
          <div class="highlight-card-body">
            <strong>守则学习任务</strong>是安全守则发布后面向部门/岗位/人员的执行层学习载体：指定学习对象与完成期限，员工完成阅读后签阅确认，系统记录签阅人、签阅时间、IP 与终端（本期固定 PC，字段预留移动端）。<br/><br/>
            SR-03：学习任务发起后，名单不可减少只可追加，保证追溯完整；逾期未签阅由定时任务（每日 8:30）自动置为逾期并逐级上报提醒。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
          <div class="flow-item" style="margin-left: 16px;">
            <el-tag type="warning">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已逾期</el-tag>
          </div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="进行中">任务发起后的初始状态。可追加名单、查看签阅进度。名单内全部签阅后自动进入已完成</el-descriptions-item>
          <el-descriptions-item label="已完成">名单内人员全部完成签阅，任务闭环。学习完成率计入守则覆盖率统计</el-descriptions-item>
          <el-descriptions-item label="已逾期">完成期限已过仍有未签阅人员，系统自动标记并逐级上报提醒（SR-03 配套）。仍可追加人员与签阅</el-descriptions-item>
        </el-descriptions>
        <h4>四、业务规则与字段指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">业务规则与字段填写</div>
          <div class="highlight-card-body">
            <p>• <strong>任务名称/选择守则/完成期限：</strong>必填项<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>选择守则：</strong>仅可选择「生效」状态的守则，学习版本取发起时守则版本号</p>
            <p>• <strong>学习对象：</strong>说明本次学习的部门/岗位/人员范围</p>
            <p>• <strong>追加人员：</strong>SR-03 名单只可追加不可减少，已存在人员自动跳过</p>
            <p>• <strong>签阅：</strong>记录签阅人、签阅时间、IP 与终端；测试成绩联动 AI 安全培训试题（预留）</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyRuleLearn">
import { listLearnTask, addLearnTask, listLearns, appendLearns, signLearn } from '@/api/safety/ruleLearn'
import { listRule } from '@/api/safety/rule'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_ruleLearn_index')
const { collapsedCards, toggleCard } = useDetailCard(['ec0','ec1'])
const { proxy } = getCurrentInstance()
const { safety_rule_learn_status: learnStatusOptions } = proxy.useDict('safety_rule_learn_status')

// 任务状态为本地枚举（进行中/已完成/已逾期）；签阅记录状态使用字典 safety_rule_learn_status（待签阅/已签阅/逾期）
const taskStatusOptions = ref([
  { label: '进行中', value: '0' },
  { label: '已完成', value: '1' },
  { label: '已逾期', value: '2' }
])

const defaultColumns = {
  taskNo: { label: '任务编号', visible: true },
  taskName: { label: '任务名称', visible: true },
  ruleName: { label: '守则', visible: true },
  ruleVersionNo: { label: '学习版本', visible: true },
  targetDesc: { label: '学习对象', visible: true },
  deadline: { label: '完成期限', visible: true },
  taskStatus: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_ruleLearn_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const open = ref(false)
const learnOpen = ref(false)
const currentTask = ref({})
const learns = ref([])
const appendUsers = ref('')
const appendTargetUsers = ref([])
const ruleOptions = ref([])
const showStatusHelp = ref(false)
const title = ref('')
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, taskName: undefined, ruleName: undefined, taskStatus: undefined } })
const { queryParams, form } = toRefs(data)
const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  ruleId: [{ required: true, message: '请选择守则', trigger: 'change' }],
  deadline: [{ required: true, message: '请设置完成期限', trigger: 'change' }]
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.taskName) count++
  if (queryParams.value.ruleName) count++
  if (queryParams.value.taskStatus) count++
  return count
})
function getList() {
  loading.value = true
  listLearnTask(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.taskName = undefined; queryParams.value.ruleName = undefined; queryParams.value.taskStatus = undefined; activeStatusTab.value = 'all'; handleQuery() }
function loadStatusCounts() {
  listLearnTask({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.taskStatus] !== undefined) counts[r.taskStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.taskStatus = status === 'all' ? undefined : status
  handleQuery()
}
function statusTabClass(value) {
  const map = { '0': 'tab-progress', '1': 'tab-done', '2': 'tab-overdue' }
  return map[value] || ''
}
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function statusClass(val) {
  const map = { '0': 'amber', '1': 'green', '2': 'red' }
  return map[val] || 'gray'
}
function loadRuleOptions() { listRule({ pageNum: 1, pageSize: 200, ruleStatus: '1' }).then(res => { ruleOptions.value = res.rows || [] }) }
function handleAdd() {
  reset()
  loadRuleOptions()
  open.value = true
  title.value = '发起学习任务'
}
function submitForm() {
  proxy.$refs['taskRef'].validate(valid => {
    if (valid) addLearnTask(form.value).then(() => { proxy.$modal.msgSuccess('发起成功'); open.value = false; getList() })
  })
}
function handleLearns(row) { currentTask.value = row; appendTargetUsers.value = []; listLearns(row.taskId).then(res => { learns.value = res.data || []; learnOpen.value = true }) }
function handleAppend(row) { currentTask.value = row; appendTargetUsers.value = []; openAppendPicker() }
const appendTargetNames = computed(() => (appendTargetUsers.value || []).map(u => u.nickName).join('、'))
function openTargetPicker() { proxy.$refs.targetPickerRef.open((form.value.learns || []).map(u => u.userId)) }
function onTargetPickerConfirm(users) {
  form.value.learns = (users || []).map(u => ({ userId: u.userId, userName: u.nickName, deptId: u.deptId, deptName: u.deptName }))
  form.value.targetDesc = form.value.learns.map(u => u.userName).join('、')
}
function removeLearner(u) {
  form.value.learns = form.value.learns.filter(x => x.userId !== u.userId)
  form.value.targetDesc = form.value.learns.map(x => x.userName).join('、')
}
function openAppendPicker() { proxy.$refs.appendPickerRef.open((appendTargetUsers.value || []).map(u => u.userId)) }
function onAppendPickerConfirm(users) { appendTargetUsers.value = users || [] }
function submitAppend() {
  if (!appendTargetUsers.value || !appendTargetUsers.value.length) { proxy.$modal.msgError('请选择追加人员'); return }
  const payload = appendTargetUsers.value.map(u => ({ userId: u.userId, userName: u.nickName, deptId: u.deptId, deptName: u.deptName }))
  appendLearns(currentTask.value.taskId, payload).then(() => {
    proxy.$modal.msgSuccess('追加成功（已存在人员自动跳过）')
    appendTargetUsers.value = []
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
function reset() { form.value = { taskName: undefined, ruleId: undefined, targetDesc: undefined, deadline: undefined, remark: undefined, learns: [] }; proxy.resetForm('taskRef') }
function cancel() { open.value = false; reset() }
getList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.safety-rulelearn-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.safety-rulelearn-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-rulelearn-page .filter-card { padding:14px 20px 16px; }
.safety-rulelearn-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-rulelearn-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-rulelearn-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-rulelearn-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-rulelearn-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-rulelearn-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-rulelearn-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-rulelearn-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-rulelearn-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-rulelearn-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-rulelearn-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-rulelearn-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-rulelearn-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-rulelearn-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.safety-rulelearn-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-rulelearn-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-rulelearn-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-rulelearn-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-rulelearn-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-rulelearn-page .field .control :deep(.el-select) { width:100%; }
.safety-rulelearn-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-rulelearn-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.safety-rulelearn-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-rulelearn-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.safety-rulelearn-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-rulelearn-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-rulelearn-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.safety-rulelearn-page .btn-soft .el-icon { font-size:14px; }
.safety-rulelearn-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.safety-rulelearn-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.safety-rulelearn-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.safety-rulelearn-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.safety-rulelearn-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.safety-rulelearn-page .table-wrap { overflow-x:auto; }
.safety-rulelearn-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-rulelearn-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-rulelearn-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-rulelearn-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-rulelearn-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.safety-rulelearn-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-rulelearn-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .safety-rulelearn-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-rulelearn-page .filter-card .filter-bar { grid-template-columns:1fr; } .safety-rulelearn-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.safety-rulelearn-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.safety-rulelearn-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.safety-rulelearn-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.safety-rulelearn-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.safety-rulelearn-page .badge.green .dot { background:var(--green-500); }
.safety-rulelearn-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.safety-rulelearn-page .badge.red .dot { background:var(--red-500); }
.safety-rulelearn-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.safety-rulelearn-page .badge.gray .dot { background:var(--ink-400); }
/* ===== Status Tabs ===== */
.safety-rulelearn-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.safety-rulelearn-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.safety-rulelearn-page .tabs-track::-webkit-scrollbar { display:none; }
.safety-rulelearn-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.safety-rulelearn-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.safety-rulelearn-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.safety-rulelearn-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.safety-rulelearn-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.safety-rulelearn-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.safety-rulelearn-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 进行中 - amber */
.safety-rulelearn-page .status-tab.tab-progress .dot { background:#f59e0b; }
.safety-rulelearn-page .status-tab.tab-progress .count { background:#fffbeb; color:#b45309; }
.safety-rulelearn-page .status-tab.is-active.tab-progress .count { background:#f59e0b; color:#fff; }
/* 已完成 - green */
.safety-rulelearn-page .status-tab.tab-done .dot { background:var(--green-500); }
.safety-rulelearn-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.safety-rulelearn-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
/* 已逾期 - red */
.safety-rulelearn-page .status-tab.tab-overdue .dot { background:var(--red-500); }
.safety-rulelearn-page .status-tab.tab-overdue .count { background:var(--red-50); color:var(--red-700); }
.safety-rulelearn-page .status-tab.is-active.tab-overdue .count { background:var(--red-500); color:#fff; }
/* ===== Tip Pill ===== */
.safety-rulelearn-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.safety-rulelearn-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.status-help-content .flow-item { display: flex; align-items: center; gap: 8px; }
.status-help-content .flow-arrow { color: #909399; font-size: 16px; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
