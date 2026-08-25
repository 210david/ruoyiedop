<template>
  <div class="app-container safety-training-attendee-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
        <div v-if="queryParams.recordId" class="filter-context">
          <el-tag closable @close="clearRecordFilter">关联课程：{{ contextCourseName || '全部' }}</el-tag>
        </div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>参训人员</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>是否合格</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isPass" placeholder="全部" clearable @change="handleQuery">
              <el-option label="合格" value="1" />
              <el-option label="不合格" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>所属部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>关联课程</label>
          <div class="control">
            <el-input v-model="queryParams.courseName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>培训日期</label>
          <div class="control is-select">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>签到状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.signInStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option label="已签到" value="1" />
              <el-option label="未签到" value="0" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:training:attendee:add']">新增</el-button>
          <el-button type="warning" plain icon="Edit" :disabled="multiple" @click="handleBatchScore" v-hasPermi="['safety:training:attendee:edit']">批量维护成绩</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:training:attendee:remove']">删除</el-button>
          <el-button type="success" plain icon="Upload" @click="proxy.$refs.importRef.open()" v-hasPermi="['safety:training:attendee:add']">导入</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:training:attendee:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_training_attendee_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="attendeeList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="参训人员" prop="userName" key="userName" :width="colWidth('userName', 144)" resizable v-if="columns.userName.visible" />
          <el-table-column label="所属部门" prop="deptName" key="deptName" :width="colWidth('deptName', 168)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="关联课程" prop="courseName" key="courseName" :width="colWidth('courseName', 216)" resizable show-overflow-tooltip v-if="columns.courseName.visible" />
          <el-table-column label="培训日期" prop="trainingDate" key="trainingDate" :width="colWidth('trainingDate', 132)" resizable sortable="custom" align="center" v-if="columns.trainingDate.visible" />
          <el-table-column label="考试分数" prop="examScore" key="examScore" :width="colWidth('examScore', 108)" resizable align="center" v-if="columns.examScore.visible">
            <template #default="scope">
              <span v-if="scope.row.examScore != null" :class="scope.row.examScore >= 60 ? 'score-pass' : 'score-fail'">{{ scope.row.examScore }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="是否合格" prop="isPass" key="isPass" :width="colWidth('isPass', 108)" resizable align="center" v-if="columns.isPass.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isPass === '1' ? 'green' : 'red'">
                <span class="dot"></span>{{ scope.row.isPass === '1' ? '合格' : '不合格' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="216" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:training:attendee:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:training:attendee:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:training:attendee:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="attendeeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>人员信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-form-item label="关联培训" prop="recordId">
                <el-select v-model="form.recordId" placeholder="请选择培训记录" filterable clearable style="width: 100%" @change="onRecordChange">
                  <el-option v-for="item in recordOptions" :key="item.recordId" :label="`${item.courseName}（${item.trainingDate || ''}）`" :value="item.recordId" />
                </el-select>
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="参训人员" prop="userId">
                  <el-input v-model="form.userName" readonly placeholder="请选择参训人员" style="width: 100%" @click="openUserPicker">
                    <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                    <template #suffix><el-icon v-if="form.userName" class="clear-icon" @click.stop="clearUser"><CircleClose /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属部门" prop="deptName"><el-input v-model="form.deptName" placeholder="选择人员后自动回填" disabled /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>考试成绩</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="考试分数" prop="examScore"><el-input-number v-model="form.examScore" :precision="2" :min="0" :max="100" style="width: 100%" @change="onScoreChange" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="是否合格" prop="isPass"><el-radio-group v-model="form.isPass"><el-radio value="1">合格</el-radio><el-radio value="0">不合格</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
              <el-form-item label="考试时间" prop="examTime"><el-date-picker v-model="form.examTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">参训人员详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>人员信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">参训人员</span><div class="rd-value">{{ viewData.userName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">关联课程</span><div class="rd-value">{{ viewData.courseName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">培训日期</span><div class="rd-value">{{ viewData.trainingDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.examScore != null || viewData.isPass || viewData.examTime || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>考试成绩</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">考试分数</span><div class="rd-value">{{ viewData.examScore != null ? viewData.examScore + ' 分' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">是否合格</span><div class="rd-value">{{ viewData.isPass || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">考试时间</span><div class="rd-value">{{ viewData.examTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>


    <!-- 参训人员选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择参训人员" @confirm="onUserPickerConfirm" />

    <!-- ===== 批量维护成绩对话框 ===== -->
    <el-dialog v-model="batchScoreOpen" width="520px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">批量维护考试成绩</span>
        </div>
      </template>
      <el-form ref="batchScoreRef" :model="batchScoreForm" label-width="100px">
        <el-form-item label="已选人数">
          <span class="batch-count">{{ selectedAttendeeCount }} 人</span>
        </el-form-item>
        <el-form-item label="是否合格">
          <el-radio-group v-model="batchScoreForm.isPass">
            <el-radio value="1">合格</el-radio>
            <el-radio value="0">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="考试分数">
          <el-input-number v-model="batchScoreForm.examScore" :precision="2" :min="0" :max="100" style="width: 100%" placeholder="选填" @change="onBatchScoreChange" />
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker v-model="batchScoreForm.examTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="batchScoreLoading" @click="submitBatchScore">确 定</el-button>
          <el-button @click="batchScoreOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="参训人员导入"
      action="/safety/training/attendee/importData"
      template-action="/safety/training/attendee/importTemplate"
      template-file-name="attendee_template"
      update-support-label="是否更新已存在的参训人员数据"
      :tips="importTips"
      @success="getList"
    />
  </div>
</template>

<script setup name="SafetyTrainingAttendee">
import { listTrainingAttendee, getTrainingAttendee, addTrainingAttendee, updateTrainingAttendee, delTrainingAttendee, batchUpdateExamScore } from '@/api/safety/trainingAttendee'
import { listTrainingRecord } from '@/api/safety/trainingRecord'
import UserPicker from '@/components/UserPicker/index.vue'
import ExcelImportDialog from '@/components/ExcelImportDialog/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, Upload, Download, ArrowDown } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_training_attendee_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])

const attendeeList = ref([])
const recordOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const contextCourseName = ref('')
const viewOpen = ref(false)
const viewData = ref({})
const dateRange = ref([])

const importTips = [
  '请先选择关联的培训记录，再导入参训人员',
  'Excel中需包含：参训人员姓名、所属部门、考试分数等字段',
  '同一培训记录下不会重复添加已存在的人员'
]

const default_columns = {
  userName: { label: '参训人员', visible: true },
  deptName: { label: '所属部门', visible: true },
  courseName: { label: '关联课程', visible: true },
  trainingDate: { label: '培训日期', visible: true },
  examScore: { label: '考试分数', visible: true },
  isPass: { label: '是否合格', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_training_attendee_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, userName: undefined, isPass: undefined, signInStatus: undefined, deptName: undefined, courseName: undefined, recordId: undefined, params: {} },
  rules: {
    recordId: [{ required: true, message: '请选择关联培训记录', trigger: 'change' }],
    userName: [{ required: true, message: '参训人员不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.userName) count++
  if (queryParams.value.isPass) count++
  if (queryParams.value.deptName) count++
  if (queryParams.value.courseName) count++
  if (queryParams.value.signInStatus) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

/** 加载培训记录选项 */
function loadRecordOptions() {
  listTrainingRecord({ pageSize: 1000 }).then(response => {
    recordOptions.value = response.rows || []
  })
}

function getList() {
  loading.value = true
  proxy.addDateRange(queryParams.value, dateRange.value)
  listTrainingAttendee(queryParams.value).then(response => { attendeeList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.userName = undefined; queryParams.value.isPass = undefined; queryParams.value.signInStatus = undefined; queryParams.value.deptName = undefined; queryParams.value.courseName = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.attendeeId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; open.value = true; title.value = '添加参训人员' }
function handleUpdate(row) { reset(); getTrainingAttendee(row.attendeeId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.examScore && !response.data.examTime && !response.data.remark; open.value = true; title.value = '修改参训人员' }) }
function handleView(row) { const id = row.attendeeId || ids.value[0]; getTrainingAttendee(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['attendeeRef'].validate(valid => {
    if (valid) {
      if (form.value.attendeeId != undefined) { updateTrainingAttendee(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addTrainingAttendee(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const attendeeIds = row.attendeeId || ids.value; proxy.$modal.confirm('是否确认删除参训人员记录？').then(function() { return delTrainingAttendee(attendeeIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/training/attendee/export', { ...queryParams.value }, `attendee_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { attendeeId: undefined, recordId: queryParams.value.recordId, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, examScore: undefined, isPass: '1', examTime: undefined, remark: undefined }
  proxy.resetForm('attendeeRef')
}

/** 选择培训记录后回填课程名称 */
function onRecordChange(recordId) {
  if (recordId) {
    const record = recordOptions.value.find(r => r.recordId === recordId)
    if (record) {
      contextCourseName.value = record.courseName
    }
  }
}

/** 考试分数变更时自动判断合格 */
function onScoreChange(val) {
  if (val != null) {
    form.value.isPass = val >= 60 ? '1' : '0'
  }
}

/** 清除培训记录筛选 */
function clearRecordFilter() {
  queryParams.value.recordId = undefined
  contextCourseName.value = ''
  handleQuery()
}

/** 打开参训人员选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.userId) }
/** 参训人员选择确认回调 */
function onUserPickerConfirm(user) { form.value.userId = user.userId; form.value.userName = user.nickName; form.value.deptId = user.deptId; form.value.deptName = user.dept?.deptName || '' }
/** 清除参训人员 */
function clearUser() { form.value.userId = undefined; form.value.userName = undefined; form.value.deptId = undefined; form.value.deptName = undefined }

/** ===== 批量维护考试成绩 ===== */
const batchScoreOpen = ref(false)
const batchScoreLoading = ref(false)
const selectedAttendeeCount = ref(0)
const batchScoreForm = reactive({ isPass: '1', examScore: undefined, examTime: undefined })

/** 打开批量维护成绩对话框 */
function handleBatchScore() {
  if (ids.value.length === 0) { proxy.$modal.msgWarning('请先选择需要维护成绩的参训人员'); return }
  selectedAttendeeCount.value = ids.value.length
  batchScoreForm.isPass = '1'
  batchScoreForm.examScore = undefined
  batchScoreForm.examTime = undefined
  batchScoreOpen.value = true
}

/** 批量考试分数变更时自动同步合格状态 */
function onBatchScoreChange(val) {
  if (val != null) {
    batchScoreForm.isPass = val >= 60 ? '1' : '0'
  }
}

/** 提交批量维护成绩 */
function submitBatchScore() {
  batchScoreLoading.value = true
  const payload = {
    attendeeIds: ids.value,
    isPass: batchScoreForm.isPass,
    examScore: batchScoreForm.examScore,
    examTime: batchScoreForm.examTime
  }
  batchUpdateExamScore(payload).then(() => {
    proxy.$modal.msgSuccess('批量维护成绩成功'); batchScoreOpen.value = false; getList()
  }).finally(() => { batchScoreLoading.value = false })
}

// 从路由参数获取关联的培训记录ID
if (route.query.recordId) {
  queryParams.value.recordId = parseInt(route.query.recordId)
  contextCourseName.value = route.query.courseName || ''
}
loadRecordOptions()
getList()
</script>

<style scoped>
.safety-training-attendee-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-training-attendee-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-training-attendee-page .filter-card { padding:14px 20px 16px; }
.safety-training-attendee-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-training-attendee-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-attendee-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-training-attendee-page .filter-card .filter-context { display:flex; align-items:center; gap:8px; }
.safety-training-attendee-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-training-attendee-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-training-attendee-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-training-attendee-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-training-attendee-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-training-attendee-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-training-attendee-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-training-attendee-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-training-attendee-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-training-attendee-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-training-attendee-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-training-attendee-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-training-attendee-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-training-attendee-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-training-attendee-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-training-attendee-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-training-attendee-page .field .control :deep(.el-select) { width:100%; }
.safety-training-attendee-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-training-attendee-page .field .control.is-select { padding:0; }
.safety-training-attendee-page .field .control.is-select :deep(.el-select) { width:100%; }
.safety-training-attendee-page .field .control.is-select :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0 12px; min-height:34px; height:34px; }
.safety-training-attendee-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-training-attendee-page .toolbar .left, .safety-training-attendee-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-training-attendee-page .table-wrap { overflow-x:auto; }
.safety-training-attendee-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-training-attendee-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-training-attendee-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-training-attendee-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-training-attendee-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-training-attendee-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-training-attendee-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-training-attendee-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-training-attendee-page .badge.green .dot { background:var(--green-500); }
.safety-training-attendee-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-training-attendee-page .badge.red .dot { background:var(--red-500); }
.safety-training-attendee-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-training-attendee-page .badge.amber .dot { background:var(--amber-500); }
.safety-training-attendee-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-training-attendee-page .badge.gray .dot { background:var(--ink-400); }
..safety-training-attendee-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } ..safety-training-attendee-page .badge.blue .dot { background:var(--blue-500); }
..safety-training-attendee-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-training-attendee-page .score-pass { color: var(--green-700); font-weight: 600; }
.safety-training-attendee-page .score-fail { color: var(--red-700); font-weight: 600; }
.safety-training-attendee-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-training-attendee-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-training-attendee-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
.batch-count { font-size: 16px; font-weight: 600; color: var(--brand-600, #4f46e5); }
</style>
