<template>
  <div class="app-container pjm-list-page pjm-change-page">
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
          <label>变更类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.changeType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>变更标题</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery">
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
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- 状态标签 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button type="button" class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="d in statusOptions" :key="d.value" type="button" class="status-tab"
                  :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]"
                  @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pjm:change:add']">新建变更</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_change_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="changeList" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="单号" prop="changeNo" key="changeNo" :width="colWidth('changeNo', 170)" resizable v-if="columns.changeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.changeNo }}</span></template>
          </el-table-column>
          <el-table-column label="所属项目" prop="projectName" key="projectName" :min-width="colWidth('projectName', 200)" resizable show-overflow-tooltip v-if="columns.projectName.visible">
            <template #default="scope">{{ scope.row.projectName || '-' }}</template>
          </el-table-column>
          <el-table-column label="类型" prop="changeType" key="changeType" :width="colWidth('changeType', 100)" resizable align="center" v-if="columns.changeType.visible">
            <template #default="scope">
              <span class="badge" :class="typeBadge(scope.row.changeType)">{{ typeLabel(scope.row.changeType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="标题" prop="title" key="title" :min-width="colWidth('title', 220)" resizable show-overflow-tooltip v-if="columns.title.visible" />
          <el-table-column label="进度影响(天)" prop="scheduleImpact" key="scheduleImpact" :width="colWidth('scheduleImpact', 120)" resizable align="right" class-name="col-num" v-if="columns.scheduleImpact.visible">
            <template #default="scope"><span class="col-amt">{{ Number(scope.row.scheduleImpact || 0) }}</span></template>
          </el-table-column>
          <el-table-column label="成本影响(¥)" prop="costImpact" key="costImpact" :width="colWidth('costImpact', 140)" resizable align="right" class-name="col-num" v-if="columns.costImpact.visible">
            <template #default="scope"><span class="col-amt">¥{{ Number(scope.row.costImpact || 0).toLocaleString() }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="申请人" prop="requesterName" key="requesterName" :width="colWidth('requesterName', 110)" resizable align="center" v-if="columns.requesterName.visible">
            <template #default="scope">{{ scope.row.requesterName || '-' }}</template>
          </el-table-column>
          <el-table-column label="申请日期" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope">{{ scope.row.createTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="250" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button link type="success" icon="Upload" v-if="scope.row.status === '0'" @click="handleSubmit(scope.row)" v-hasPermi="['pjm:change:edit']">提交</el-button>
                <el-button link type="warning" icon="Check" v-if="scope.row.status === '1'" @click="handleAudit(scope.row)" v-hasPermi="['pjm:change:audit']">审核</el-button>
                <el-button link type="primary" icon="Edit" v-if="scope.row.status === '0'" @click="handleEdit(scope.row)" v-hasPermi="['pjm:change:edit']">编辑</el-button>
                <el-button link type="danger" icon="Delete" v-if="scope.row.status === '0'" @click="handleDelete(scope.row)" v-hasPermi="['pjm:change:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/编辑弹框 -->
    <el-dialog v-model="open" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Document /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <template v-if="form.changeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.changeNo }}</span>
          </template>
        </div>
      </template>
      <el-form ref="changeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>基本信息
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属项目" prop="projectId">
                    <el-input :model-value="formProjectLabel" placeholder="点击选择项目" readonly style="width:100%;cursor:pointer" @click="openFormProjectPicker">
                      <template #append><el-button icon="Search" @click.stop="openFormProjectPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="变更类型" prop="changeType">
                    <el-select v-model="form.changeType" style="width:100%">
                      <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="变更标题" prop="title">
                    <el-input v-model="form.title" placeholder="如：新增XX功能导致工期延长" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><EditPen /></el-icon></span>变更说明
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="变更原因" prop="reason">
                    <el-input v-model="form.reason" type="textarea" :rows="2" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="影响分析">
                    <el-input v-model="form.impactAnalysis" type="textarea" :rows="2" placeholder="对范围/进度/成本/质量的影响评估" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><DataLine /></el-icon></span>影响评估
              </div>
              <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="进度影响(天)">
                    <el-input-number v-model="form.scheduleImpact" :min="0" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="成本影响(¥)">
                    <el-input-number v-model="form.costImpact" :min="0" controls-position="right" style="width:100%" :precision="2" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">保存（草稿）</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog v-model="detailOpen" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><View /></el-icon></div>
          <span class="rd-detail-header-title">变更详情</span>
          <template v-if="detailData && detailData.changeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ detailData.changeNo }}</span>
          </template>
        </div>
      </template>
      <div class="rd-page" v-if="detailData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d1')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>基本信息
            </div>
            <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d1 }" aria-label="折叠">
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">单号</span><div class="rd-value">{{ detailData.changeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ statusLabel(detailData.status) }}</div></div>
              <div class="rd-item"><span class="rd-label">类型</span><div class="rd-value">{{ typeLabel(detailData.changeType) }}</div></div>
              <div class="rd-item"><span class="rd-label">所属项目</span><div class="rd-value">{{ detailData.projectName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ detailData.requesterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请日期</span><div class="rd-value">{{ detailData.createTime || '-' }}</div></div>
            </div>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d2')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><EditPen /></el-icon></span>变更内容
            </div>
            <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d2 }" aria-label="折叠">
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d2">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">变更标题</span><div class="rd-value">{{ detailData.title || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">变更原因</span><div class="rd-value">{{ detailData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">影响分析</span><div class="rd-value">{{ detailData.impactAnalysis || '-' }}</div></div>
            </div>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d3')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><DataLine /></el-icon></span>影响与审核
            </div>
            <button type="button" class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d3 }" aria-label="折叠">
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.d3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">进度影响</span><div class="rd-value">{{ detailData.scheduleImpact != null ? detailData.scheduleImpact + ' 天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">成本影响</span><div class="rd-value rd-amount">¥{{ Number(detailData.costImpact || 0).toLocaleString() }}</div></div>
              <div class="rd-item" v-if="detailData.auditBy"><span class="rd-label">审核人</span><div class="rd-value">{{ detailData.auditBy }}</div></div>
              <div class="rd-item" v-if="detailData.auditTime"><span class="rd-label">审核时间</span><div class="rd-value">{{ detailData.auditTime }}</div></div>
              <div class="rd-item rd-item--full" v-if="detailData.auditOpinion"><span class="rd-label">审核意见</span><div class="rd-value">{{ detailData.auditOpinion }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹框 -->
    <el-dialog v-model="auditOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Stamp /></el-icon></div>
          <span class="rd-detail-header-title">变更审核</span>
          <template v-if="auditNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ auditNo }}</span>
          </template>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-body">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审核结果" prop="status">
                <el-radio-group v-model="auditForm.status">
                  <el-radio value="2">通过</el-radio>
                  <el-radio value="3">驳回</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="审核意见" prop="auditOpinion">
                <el-input v-model="auditForm.auditOpinion" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="auditOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <ProjectPicker ref="formProjectPickerRef" @confirm="onFormProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmChange">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listChange, getChange, addChange, updateChange, delChange, submitChange, auditChange } from '@/api/pjm/change'
import { listProject } from '@/api/pjm/project'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ProjectPicker from '@/components/ProjectPicker/index.vue'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'd1', 'd2', 'd3'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_change_index')

const loading = ref(false)
const changeList = ref([]); const total = ref(0); const projectOptions = ref([])
const open = ref(false); const title = ref(''); const changeRef = ref()
const detailOpen = ref(false); const detailData = ref(null)
const auditOpen = ref(false); const auditRef = ref(); const auditNo = ref('')

const showSearch = ref(true)
const showAdvanced = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({})

// 列显隐配置
const columns = ref({
  changeNo: { label: '单号', visible: true },
  projectName: { label: '所属项目', visible: true },
  changeType: { label: '类型', visible: true },
  title: { label: '标题', visible: true },
  scheduleImpact: { label: '进度影响(天)', visible: true },
  costImpact: { label: '成本影响(¥)', visible: true },
  status: { label: '状态', visible: true },
  requesterName: { label: '申请人', visible: true },
  createTime: { label: '申请日期', visible: true }
})

const typeOptions = [
  { value: '1', label: '范围' }, { value: '2', label: '进度' },
  { value: '3', label: '成本' }, { value: '4', label: '质量' }, { value: '5', label: '其他' }
]
const statusOptions = [
  { value: '0', label: '草稿' }, { value: '1', label: '待审' },
  { value: '2', label: '已通过' }, { value: '3', label: '已驳回' }, { value: '4', label: '已实施' }
]
const typeLabel = (v) => typeOptions.find(x => x.value === v)?.label || v
const typeBadge = (v) => ({ '1': 'violet', '2': 'blue', '3': 'amber', '4': 'green', '5': 'gray' }[v] || 'gray')
const statusLabel = (v) => statusOptions.find(x => x.value === v)?.label || v
const statusBadge = (v) => ({ '0': 'amber', '1': 'blue', '2': 'green', '3': 'red', '4': 'green' }[v] || 'gray')
const statusTabClass = (v) => ({ '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-reject', '4': 'tab-done' }[v] || '')

const queryParams = reactive({ pageNum: 1, pageSize: 10, projectId: null, changeType: null, status: null, title: null })
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
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入变更标题', trigger: 'blur' }],
  reason: [{ required: true, message: '请说明变更原因', trigger: 'blur' }]
}
const auditForm = reactive({ changeId: null, status: null, auditOpinion: '' })
const auditRules = {
  status: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.projectId) count++
  if (queryParams.changeType) count++
  if (queryParams.status) count++
  if (queryParams.title) count++
  return count
})

function getList() {
  loading.value = true
  listChange(queryParams).then(res => {
    changeList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

/** 状态标签数字：统计全部变更单 */
function loadStatusCounts() {
  listChange({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    statusOptions.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.projectId = null
  queryParams.changeType = null
  queryParams.status = null
  queryParams.title = null
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.status = status === 'all' ? null : status
  handleQuery()
}

function reset() { form.value = { changeId: null, projectId: null, changeType: '1', title: null, reason: null, impactAnalysis: null, scheduleImpact: 0, costImpact: 0 } }
function handleAdd() { reset(); title.value = '新建变更申请'; open.value = true }
function handleEdit(row) { getChange(row.changeId).then(res => { form.value = res.data; title.value = '编辑变更'; open.value = true }) }
function handleDetail(row) { getChange(row.changeId).then(res => { detailData.value = res.data; detailOpen.value = true }) }

function submitForm() {
  changeRef.value.validate(valid => {
    if (!valid) return
    const action = form.value.changeId ? updateChange(form.value) : addChange(form.value)
    action.then(() => { ElMessage.success('保存成功'); open.value = false; handleQuery() })
  })
}

function handleSubmit(row) {
  ElMessageBox.confirm('提交审核后状态将变更为"待审"，确认？', '提交审核', { type: 'warning' }).then(() => {
    submitChange(row.changeId).then(() => { ElMessage.success('已提交'); handleQuery() })
  })
}

function handleAudit(row) {
  Object.assign(auditForm, { changeId: row.changeId, status: null, auditOpinion: '' })
  auditNo.value = row.changeNo
  auditOpen.value = true
}

function submitAudit() {
  auditRef.value.validate(valid => {
    if (!valid) return
    auditChange({ changeId: auditForm.changeId, status: auditForm.status, auditOpinion: auditForm.auditOpinion }).then(() => {
      ElMessage.success('审核完成'); auditOpen.value = false; handleQuery()
    })
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除该变更申请？', '提示', { type: 'warning' }).then(() => {
    delChange(row.changeId).then(() => { ElMessage.success('删除成功'); handleQuery() })
  })
}

onMounted(() => {
  listProject({ pageNum: 1, pageSize: 200 }).then(res => { projectOptions.value = res.rows || [] })
  getList()
  loadStatusCounts()
})
</script>
