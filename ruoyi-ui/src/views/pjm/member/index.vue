<template>
  <div class="app-container pjm-list-page pjm-member-page">
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
        <div class="field" v-show="showAdvanced">
          <label>角色</label>
          <div class="control is-select">
            <el-select v-model="queryParams.roleType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in roleOptions" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" :disabled="!queryParams.projectId" @click="handleAdd" v-hasPermi="['pjm:member:add']">邀请成员</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_member_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="memberList" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="姓名" prop="userName" key="userName" :width="colWidth('userName', 120)" resizable v-if="columns.userName.visible">
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" :min-width="colWidth('deptName', 180)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="角色" prop="roleType" key="roleType" :width="colWidth('roleType', 120)" resizable align="center" v-if="columns.roleType.visible">
            <template #default="scope">
              <span class="badge" :class="roleBadge(scope.row.roleType)">
                <span class="dot"></span>{{ roleLabel(scope.row.roleType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="加入日期" prop="joinDate" key="joinDate" :width="colWidth('joinDate', 120)" resizable align="center" v-if="columns.joinDate.visible">
            <template #default="scope">{{ scope.row.joinDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="工时负载(H)" prop="workload" key="workload" :width="colWidth('workload', 130)" resizable align="right" class-name="col-num" v-if="columns.workload.visible">
            <template #default="scope"><span class="col-amt">{{ scope.row.workload != null ? scope.row.workload : '0' }}</span></template>
          </el-table-column>
          <el-table-column label="任务数" prop="taskCount" key="taskCount" :width="colWidth('taskCount', 90)" resizable align="right" class-name="col-num" v-if="columns.taskCount.visible">
            <template #default="scope">{{ scope.row.taskCount != null ? scope.row.taskCount : 0 }}</template>
          </el-table-column>
          <el-table-column label="离开日期" prop="leaveDate" key="leaveDate" :width="colWidth('leaveDate', 120)" resizable align="center" v-if="columns.leaveDate.visible">
            <template #default="scope">{{ scope.row.leaveDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['pjm:member:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleRemove(scope.row)" v-hasPermi="['pjm:member:remove']">移除</el-button>
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

    <!-- 邀请成员 / 修改成员 -->
    <el-dialog v-model="open" width="640px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><UserFilled /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <template v-if="form.userName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.userName }}</span>
          </template>
        </div>
      </template>
      <el-form ref="memberRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><User /></el-icon></span>成员信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="选择用户" prop="userId">
                    <el-input v-model="form.userName" readonly placeholder="请从组织架构选择成员" @click="openUserPicker">
                      <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                      <template #suffix>
                        <el-icon v-if="form.userName" style="cursor:pointer" @click.stop="clearMemberUser"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属部门">
                    <el-input v-model="form.deptName" readonly placeholder="选择用户后自动带出" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="角色" prop="roleType">
                    <el-select v-model="form.roleType" style="width:100%">
                      <el-option v-for="d in roleOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Calendar /></el-icon></span>加入信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="加入日期">
                    <el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
    <!-- 成员选择（组织机构） -->
    <UserPicker ref="userPickerRef" title="从组织架构选择项目成员" @confirm="onUserConfirm" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmMember">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import UserPicker from '@/components/UserPicker/index.vue'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import { listMember, getMember, addMember, updateMember, delMember } from '@/api/pjm/member'
import { listProject } from '@/api/pjm/project'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_member_index')

const loading = ref(false)
const memberList = ref([])
const total = ref(0)
const projectOptions = ref([])
const open = ref(false)
const title = ref('')
const memberRef = ref()

const showSearch = ref(true)
const showAdvanced = ref(false)

// 列显隐配置
const columns = ref({
  userName: { label: '姓名', visible: true },
  deptName: { label: '部门', visible: true },
  roleType: { label: '角色', visible: true },
  joinDate: { label: '加入日期', visible: true },
  workload: { label: '工时负载(H)', visible: true },
  taskCount: { label: '任务数', visible: true },
  leaveDate: { label: '离开日期', visible: true }
})

const roleOptions = [
  { value: '1', label: '项目经理' }, { value: '2', label: '技术负责人' },
  { value: '3', label: '施工员' }, { value: '4', label: '安全员' },
  { value: '5', label: '质量员' }, { value: '6', label: '资料员' },
  { value: '7', label: '监理' }
]

const roleLabel = (v) => roleOptions.find(x => x.value === v)?.label || '普通成员'
const roleBadge = (v) => ({ '1': 'red', '2': 'amber', '3': 'blue', '4': 'red', '5': 'green', '6': 'gray', '7': 'violet' }[v] || 'gray')

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, roleType: null })
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
  handleQuery()
}
const form = ref({})
const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.roleType) count++
  return count
})

function loadProjects() {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
}

function getList() {
  if (!queryParams.projectId) { memberList.value = []; total.value = 0; return }
  loading.value = true
  listMember(queryParams).then(res => {
    memberList.value = res.rows || []
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
  queryParams.roleType = null
  handleQuery()
}

function reset() {
  form.value = { memberId: null, projectId: queryParams.projectId, userId: null, userName: null, deptId: null, deptName: null, roleType: '3', joinDate: null }
}

function handleAdd() {
  reset(); title.value = '邀请成员'; open.value = true
}

function handleEdit(row) {
  getMember(row.memberId).then(res => { form.value = res.data; title.value = '修改成员'; open.value = true })
}

/** 成员选择（组织架构） */
const userPickerRef = ref()
function openUserPicker() { userPickerRef.value.open(form.value.userId) }
function onUserConfirm(u) {
  form.value.userId = u.userId
  form.value.userName = u.nickName
  form.value.deptId = u.deptId
  form.value.deptName = u.deptName
}
function clearMemberUser() {
  form.value.userId = null
  form.value.userName = null
  form.value.deptId = null
  form.value.deptName = null
}

function submitForm() {
  memberRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.memberId ? updateMember(form.value) : addMember(form.value)
    action.then(() => {
      ElMessage.success(form.value.memberId ? '修改成功' : '邀请成功')
      open.value = false
      getList()
    })
  })
}

function handleRemove(row) {
  ElMessageBox.confirm(`确认将 ${row.userName} 移出项目？`, '提示', { type: 'warning' }).then(() => {
    delMember(row.memberId).then(() => { ElMessage.success('已移出'); getList() })
  })
}

onMounted(() => { loadProjects(); getList() })
</script>

<style scoped>
.pjm-member-page .empty-block { padding: 4px 0 16px; }
.pjm-member-page .empty-block :deep(.el-empty) { padding: 24px 0; }
</style>
