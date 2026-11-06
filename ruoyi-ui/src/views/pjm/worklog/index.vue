<template>
  <div class="app-container pjm-list-page pjm-worklog-page">
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
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>WBS任务</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskId" placeholder="全部任务" clearable filterable @change="getList">
              <el-option v-for="t in taskOptions" :key="t.taskId" :label="t.taskCode + ' ' + t.taskName" :value="t.taskId" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>填报人</label>
          <div class="control is-select">
            <el-select v-model="queryParams.userId" placeholder="全部" clearable filterable @change="getList">
              <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，未选择项目时不加载数据
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" :disabled="!queryParams.projectId" @click="handleAdd" v-hasPermi="['pjm:worklog:add']">录入工时</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_worklog_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="worklogList" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="工单号" prop="worklogNo" key="worklogNo" :width="colWidth('worklogNo', 170)" resizable v-if="columns.worklogNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.worklogNo }}</span></template>
          </el-table-column>
          <el-table-column label="WBS任务" prop="taskName" key="taskName" :min-width="colWidth('taskName', 220)" resizable show-overflow-tooltip v-if="columns.taskName.visible">
            <template #default="scope">{{ scope.row.taskName || scope.row.taskId || '-' }}</template>
          </el-table-column>
          <el-table-column label="填报人" prop="userName" key="userName" :width="colWidth('userName', 120)" resizable align="center" v-if="columns.userName.visible">
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="工时日期" prop="workDate" key="workDate" :width="colWidth('workDate', 120)" resizable align="center" v-if="columns.workDate.visible">
            <template #default="scope">{{ scope.row.workDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="工时(H)" prop="workHours" key="workHours" :width="colWidth('workHours', 110)" resizable align="center" v-if="columns.workHours.visible">
            <template #default="scope">
              <span class="badge blue">{{ scope.row.workHours != null ? scope.row.workHours : 0 }}H</span>
            </template>
          </el-table-column>
          <el-table-column label="工作内容" prop="workContent" key="workContent" :min-width="colWidth('workContent', 240)" resizable show-overflow-tooltip v-if="columns.workContent.visible">
            <template #default="scope">{{ scope.row.workContent || '-' }}</template>
          </el-table-column>
          <el-table-column label="录入时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope">{{ scope.row.createTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['pjm:worklog:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:worklog:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <div class="empty-block" v-if="!queryParams.projectId">
        <el-empty description="请先选择项目" />
      </div>
    </div>

    <!-- 新增/修改弹框 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Clock /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <template v-if="form.worklogNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.worklogNo }}</span>
          </template>
        </div>
      </template>
      <el-form ref="worklogRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>工时信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="WBS任务" prop="taskId">
                    <el-select v-model="form.taskId" filterable placeholder="选择WBS任务" style="width:100%" @change="onTaskChange">
                      <el-option v-for="t in taskOptions" :key="t.taskId" :label="t.taskCode + ' ' + t.taskName" :value="t.taskId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工时日期" prop="workDate">
                    <el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工时(H)" prop="workHours">
                    <el-input-number v-model="form.workHours" :min="0.1" :max="24" :step="0.5" controls-position="right" style="width:100%" :precision="1" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><User /></el-icon></span>填报信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="填报人" prop="userId">
                    <el-input v-model="form.userName" readonly placeholder="请从组织架构选择填报人" @click="openUserPicker">
                      <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                      <template #suffix>
                        <el-icon v-if="form.userName" style="cursor:pointer" @click.stop="clearWorklogUser"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="工作内容" prop="workContent">
                    <el-input v-model="form.workContent" type="textarea" :rows="3" placeholder="描述当日工作内容" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
    <!-- 填报人选择（组织机构） -->
    <UserPicker ref="userPickerRef" title="从组织架构选择填报人" @confirm="onUserConfirm" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmWorklog">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import UserPicker from '@/components/UserPicker/index.vue'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import { listWorklog, getWorklog, addWorklog, updateWorklog, delWorklog } from '@/api/pjm/worklog'
import { listProject } from '@/api/pjm/project'
import { listTask } from '@/api/pjm/task'
import { listAllUsers } from '@/api/pjm/member'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_worklog_index')

const loading = ref(false)
const worklogList = ref([]); const total = ref(0)
const projectOptions = ref([]); const taskOptions = ref([]); const userOptions = ref([])
const open = ref(false); const title = ref(''); const worklogRef = ref()

const showSearch = ref(true)
const showAdvanced = ref(false)

// 列显隐配置
const columns = ref({
  worklogNo: { label: '工单号', visible: true },
  taskName: { label: 'WBS任务', visible: true },
  userName: { label: '填报人', visible: true },
  workDate: { label: '工时日期', visible: true },
  workHours: { label: '工时(H)', visible: true },
  workContent: { label: '工作内容', visible: true },
  createTime: { label: '录入时间', visible: true }
})

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, taskId: null, userId: null })
const projectPickerRef = ref()
const selectedProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(queryParams.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openProjectPicker() { projectPickerRef.value?.open(queryParams.projectId) }
function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  queryParams.projectId = p.projectId
  onProjectChange()
}
const form = ref({})
const rules = {
  taskId: [{ required: true, message: '请选择WBS任务', trigger: 'change' }],
  workDate: [{ required: true, message: '请选择工时日期', trigger: 'change' }],
  userId: [{ required: true, message: '请选择填报人', trigger: 'change' }],
  workHours: [{ required: true, message: '请输入工时', trigger: 'blur' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.taskId) count++
  if (queryParams.userId) count++
  return count
})

function loadProjects() { listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] }) }
function loadUsers() { listAllUsers().then(res => { userOptions.value = res.rows || [] }) }

function onProjectChange() {
  queryParams.taskId = null
  if (queryParams.projectId) {
    listTask({ pageNum: 1, pageSize: 500, projectId: queryParams.projectId }).then(res => {
      taskOptions.value = res.rows || []
      queryParams.pageNum = 1; getList()
    })
  } else {
    taskOptions.value = []; worklogList.value = []; total.value = 0
  }
}

function getList() {
  if (!queryParams.projectId) return
  loading.value = true
  listWorklog(queryParams).then(res => {
    worklogList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.projectId = null
  queryParams.taskId = null
  queryParams.userId = null
  taskOptions.value = []
  worklogList.value = []
  total.value = 0
}

function reset() {
  form.value = { worklogId: null, taskId: null, projectId: queryParams.projectId, userId: null, userName: null, workDate: new Date().toISOString().slice(0,10), workHours: 8, workContent: null }
}

function handleAdd() {
  if (taskOptions.value.length === 0) onProjectChange()
  reset(); title.value = '录入工时'; open.value = true
}

function handleEdit(row) { getWorklog(row.worklogId).then(res => { form.value = res.data; title.value = '修改工时'; open.value = true }) }

function onTaskChange(taskId) {
  const t = taskOptions.value.find(x => x.taskId === taskId); if (t) form.value.projectId = t.projectId
}
function onUserChange(userId) {
  const u = userOptions.value.find(x => x.userId === userId); if (u) form.value.userName = u.nickName
}

/** 填报人选择（组织架构） */
const userPickerRef = ref()
function openUserPicker() { userPickerRef.value.open(form.value.userId) }
function onUserConfirm(u) {
  form.value.userId = u.userId
  form.value.userName = u.nickName
}
function clearWorklogUser() {
  form.value.userId = null
  form.value.userName = null
}

function submitForm() {
  worklogRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.worklogId ? updateWorklog(form.value) : addWorklog(form.value)
    action.then(() => { ElMessage.success(form.value.worklogId ? '修改成功' : '录入成功'); open.value = false; getList() })
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除该工时记录？', '提示', { type: 'warning' }).then(() => {
    delWorklog(row.worklogId).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

onMounted(() => { loadProjects(); loadUsers() })
</script>

<style scoped>
.pjm-worklog-page .empty-block { padding: 4px 0 16px; }
.pjm-worklog-page .empty-block :deep(.el-empty) { padding: 24px 0; }
</style>
