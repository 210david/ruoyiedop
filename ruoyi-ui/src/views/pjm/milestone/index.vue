<template>
  <div class="app-container pjm-list-page pjm-milestone-page">
    <!-- ===== 筛选卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field" style="grid-column: span 2">
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="getList">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>里程碑名称</label>
          <div class="control">
            <el-input v-model="queryParams.milestoneName" placeholder="请输入" clearable @keyup.enter="getList">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 里程碑时间线 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pjm:milestone:add']">新增里程碑</el-button>
          <button type="button" class="btn-soft is-outline" @click="getList">
            <el-icon><Refresh /></el-icon> 刷新
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="ms-body" v-loading="loading">
        <el-timeline v-if="milestoneList.length > 0" class="ms-timeline">
          <el-timeline-item v-for="m in milestoneList" :key="m.milestoneId"
                            :type="m.status === '1' ? 'success' : m.status === '2' ? 'danger' : 'primary'"
                            :hollow="m.status !== '1'" :timestamp="'计划 ' + (m.planDate || '-')">
            <div class="ms-card" :class="'is-' + (m.status || '0')">
              <div class="ms-card-head">
                <span class="ms-name">{{ m.milestoneName }}</span>
                <span class="badge" :class="statusBadge(m.status)">
                  <span class="dot"></span>{{ statusLabel(m.status) }}
                </span>
              </div>
              <div class="ms-card-meta">
                <span>负责人：{{ m.ownerName || '-' }}</span>
                <span>实际达成：{{ m.actualDate || '-' }}</span>
                <span v-if="m.description" class="ms-desc">{{ m.description }}</span>
              </div>
              <div class="ms-card-actions">
                <el-button link type="success" icon="Check" v-if="m.status !== '1'" @click="handleAchieve(m)" v-hasPermi="['pjm:milestone:edit']">达成</el-button>
                <el-button link type="primary" icon="Edit" @click="handleEdit(m)" v-hasPermi="['pjm:milestone:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(m)" v-hasPermi="['pjm:milestone:remove']">删除</el-button>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无里程碑，请选择项目或新增" />
      </div>
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="640px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Calendar /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <template v-if="form.milestoneName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.milestoneName }}</span>
          </template>
        </div>
      </template>
      <el-form ref="milestoneRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Document /></el-icon></span>里程碑信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="所属项目" prop="projectId">
                    <el-input :model-value="formProjectLabel" placeholder="点击选择项目" readonly style="width:100%;cursor:pointer" @click="openFormProjectPicker">
                      <template #append><el-button icon="Search" @click.stop="openFormProjectPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="里程碑名称" prop="milestoneName">
                    <el-input v-model="form.milestoneName" placeholder="如：主体结构封顶、竣工验收" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="计划日期" prop="planDate">
                    <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>责任与状态
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="负责人">
                    <el-input v-model="form.ownerName" readonly placeholder="请选择负责人" @click="openOwnerPicker">
                      <template #append><el-button icon="Search" @click="openOwnerPicker" /></template>
                      <template #suffix>
                        <el-icon v-if="form.ownerName" style="cursor:pointer" @click.stop="clearOwner"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-select v-model="form.status" style="width:100%">
                      <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>描述
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="描述">
                <el-input v-model="form.description" type="textarea" :rows="2" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 负责人选择（组织机构） -->
    <UserPicker ref="ownerPickerRef" title="选择里程碑负责人" @confirm="onOwnerConfirm" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <ProjectPicker ref="formProjectPickerRef" @confirm="onFormProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmMilestone">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import UserPicker from '@/components/UserPicker/index.vue'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import { listMilestone, getMilestone, addMilestone, updateMilestone, delMilestone } from '@/api/pjm/milestone'
import { listProject } from '@/api/pjm/project'
import { useDetailCard } from '@/composables/useDetailCard'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3'])

const loading = ref(false)
const showSearch = ref(true)
const showAdvanced = ref(false)
const milestoneList = ref([])
const projectOptions = ref([])
const open = ref(false)
const title = ref('')
const milestoneRef = ref()

const statusOptions = [
  { value: '0', label: '未达成' }, { value: '1', label: '已达成' }, { value: '2', label: '已延期' }
]

const queryParams = reactive({ pageNum: 1, pageSize: 50, projectId: null, milestoneName: null, status: null })
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
  getList()
}
const form = ref({})
// 表单内项目浏览框
const formProjectPickerRef = ref()
const formProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(form.value.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openFormProjectPicker() { formProjectPickerRef.value?.open(form.value.projectId) }
function onFormProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  form.value.projectId = p.projectId
}
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  milestoneName: [{ required: true, message: '请输入里程碑名称', trigger: 'blur' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
}

const statusLabel = (v) => statusOptions.find(x => x.value === v)?.label || v
const statusBadge = (v) => ({ '0': 'amber', '1': 'green', '2': 'red' }[v] || 'gray')

/** 负责人选择（组织机构） */
const ownerPickerRef = ref()
function openOwnerPicker() { ownerPickerRef.value.open(form.value.ownerId) }
function onOwnerConfirm(u) {
  form.value.ownerId = u.userId
  form.value.ownerName = u.nickName
}
function clearOwner() {
  form.value.ownerId = null
  form.value.ownerName = null
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.status) count++
  if (queryParams.milestoneName) count++
  return count
})

function loadProjects() {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
}

function getList() {
  loading.value = true
  listMilestone(queryParams).then(res => {
    milestoneList.value = res.rows || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

function resetQuery() {
  queryParams.projectId = null
  queryParams.milestoneName = null
  queryParams.status = null
  getList()
}

function reset() {
  form.value = { milestoneId: null, projectId: queryParams.projectId, milestoneName: null, planDate: null, actualDate: null, status: '0', ownerId: null, ownerName: null, description: null }
}

function handleAdd() {
  reset()
  title.value = '新增里程碑'
  open.value = true
}

function handleEdit(row) {
  getMilestone(row.milestoneId).then(res => {
    form.value = res.data
    title.value = '修改里程碑'
    open.value = true
  })
}

/** 一键达成 */
function handleAchieve(row) {
  ElMessageBox.confirm(`确认将里程碑「${row.milestoneName}」标记为已达成？`, '提示', { type: 'success' }).then(() => {
    updateMilestone({ milestoneId: row.milestoneId, status: '1' }).then(() => {
      ElMessage.success('已达成')
      getList()
    })
  })
}

function submitForm() {
  milestoneRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.milestoneId ? updateMilestone(form.value) : addMilestone(form.value)
    action.then(() => {
      ElMessage.success(form.value.milestoneId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除该里程碑吗？', '提示', { type: 'warning' }).then(() => {
    delMilestone(row.milestoneId).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  })
}

onMounted(() => {
  loadProjects()
  getList()
})
</script>

<style scoped>
.pjm-milestone-page .ms-body { padding: 18px 20px; }
.pjm-milestone-page .ms-timeline { padding-left: 4px; margin: 0; }
.pjm-milestone-page .ms-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid var(--ink-200);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-card);
  transition: border-color .15s var(--ease-out), box-shadow .15s var(--ease-out);
}
.pjm-milestone-page .ms-card:hover { border-color: var(--brand-200); box-shadow: 0 4px 12px -4px rgba(99, 102, 241, .25); }
.pjm-milestone-page .ms-card.is-0 { border-left: 3px solid var(--amber-500); }
.pjm-milestone-page .ms-card.is-1 { border-left: 3px solid var(--green-500); }
.pjm-milestone-page .ms-card.is-2 { border-left: 3px solid var(--red-500); }
.pjm-milestone-page .ms-card-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; flex-wrap: wrap; }
.pjm-milestone-page .ms-name { font-size: 14px; font-weight: 600; color: var(--ink-900); }
.pjm-milestone-page .ms-card-meta { display: flex; flex-wrap: wrap; gap: 4px 16px; font-size: 12px; color: var(--ink-500); }
.pjm-milestone-page .ms-desc { color: var(--ink-400); }
.pjm-milestone-page .ms-card-actions { display: flex; justify-content: flex-end; }
</style>
