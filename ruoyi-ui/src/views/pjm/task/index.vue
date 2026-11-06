<template>
  <div class="app-container pjm-list-page pjm-task-page">
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
        <div class="field" v-show="showAdvanced">
          <label>项目编号</label>
          <div class="control"><span class="control-text">{{ currentProject?.projectNo || '-' }}</span></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>项目经理</label>
          <div class="control"><span class="control-text">{{ currentProject?.managerName || '-' }}</span></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="onProjectChange">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 采购汇总 ===== -->
    <div class="stat-grid" v-if="purchaseSummary">
      <div class="stat-card is-brand">
        <div class="stat-label">预算</div>
        <div class="stat-value">¥{{ money(purchaseSummary.budgetAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">计划采购</div>
        <div class="stat-value">¥{{ money(purchaseSummary.plannedPurchase) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">实际采购</div>
        <div class="stat-value">¥{{ money(purchaseSummary.actualPurchase) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已收货</div>
        <div class="stat-value">¥{{ money(purchaseSummary.receivedAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已开票</div>
        <div class="stat-value">¥{{ money(purchaseSummary.invoiceAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已付款</div>
        <div class="stat-value">¥{{ money(purchaseSummary.paidAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">采购申请数</div>
        <div class="stat-value">{{ purchaseSummary.purchaseCount || 0 }}</div>
      </div>
    </div>

    <!-- ===== WBS 任务分解树 ===== -->
    <div class="surface" v-if="selectedProjectId">
      <div class="status-tabs">
        <div class="tabs-track">
          <span class="tree-label">WBS 任务分解树</span>
        </div>
        <span class="badge" :class="projectStatusBadge(currentProject?.status)">
          <span class="dot"></span>{{ currentProject?.projectName || '-' }} · {{ projectStatusLabel(currentProject?.status) }}
        </span>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd(null)" v-hasPermi="['pjm:task:add']">新增顶级任务</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdateSelected" v-hasPermi="['pjm:task:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="single" @click="handleDeleteSelected" v-hasPermi="['pjm:task:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="refreshAll">
            <el-icon><Refresh /></el-icon> 刷新
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="refreshAll" :columns="columns" storageKey="pjm_task_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="treeData" row-key="taskId"
                  :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all border
                  class="app-table" @header-dragend="onHeaderDragEnd" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="任务编码" prop="taskCode" key="taskCode" :width="colWidth('taskCode', 130)" resizable v-if="columns.taskCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.taskCode || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="任务名称" prop="taskName" key="taskName" :min-width="colWidth('taskName', 220)" resizable show-overflow-tooltip v-if="columns.taskName.visible" />
          <el-table-column label="类型" prop="taskType" key="taskType" :width="colWidth('taskType', 100)" resizable align="center" v-if="columns.taskType.visible">
            <template #default="scope">
              <span class="badge violet">{{ taskTypeLabel(scope.row.taskType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="进度" prop="progress" key="progress" :width="colWidth('progress', 150)" resizable v-if="columns.progress.visible">
            <template #default="scope">
              <div class="pbar-wrap">
                <div class="pbar">
                  <div class="pbar-fill" :class="{ 'is-done': Number(scope.row.progress || 0) >= 100 }"
                       :style="{ width: Math.min(Number(scope.row.progress || 0), 100) + '%' }"></div>
                </div>
                <span class="pbar-txt">{{ Number(scope.row.progress || 0).toFixed(0) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="ownerName" key="ownerName" :width="colWidth('ownerName', 100)" resizable align="center" v-if="columns.ownerName.visible">
            <template #default="scope">{{ scope.row.ownerName || '-' }}</template>
          </el-table-column>
          <el-table-column label="计划开始" prop="planStartDate" key="planStartDate" :width="colWidth('planStartDate', 120)" resizable align="center" v-if="columns.planStartDate.visible">
            <template #default="scope">{{ scope.row.planStartDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="计划结束" prop="planEndDate" key="planEndDate" :width="colWidth('planEndDate', 120)" resizable align="center" v-if="columns.planEndDate.visible">
            <template #default="scope">{{ scope.row.planEndDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="采购" prop="needPurchase" key="needPurchase" :width="colWidth('needPurchase', 100)" resizable align="center" v-if="columns.needPurchase.visible">
            <template #default="scope">
              <span v-if="scope.row.purchaseRequestId" class="badge red"><span class="dot"></span>已发起</span>
              <span v-else-if="scope.row.needPurchase === '1'" class="badge amber"><span class="dot"></span>需采购</span>
              <span v-else class="muted-dash">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['pjm:task:add']">子任务</el-button>
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['pjm:task:edit']">修改</el-button>
                <el-button link type="success" icon="ShoppingCart" @click="handleBuildPurchase(scope.row)" v-if="!scope.row.purchaseRequestId" v-hasPermi="['pjm:project:purchase']">发起采购</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pjm:task:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="surface" v-else>
      <el-empty description="请先选择一个项目" class="tree-empty" />
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="740px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><Tickets /></el-icon></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <template v-if="form.taskCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ form.taskCode }}</span>
          </template>
        </div>
      </template>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Document /></el-icon></span>基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="任务编码">
                    <el-input v-model="form.taskCode" placeholder="如 1.2.3" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="任务类型">
                    <el-select v-model="form.taskType" style="width:100%">
                      <el-option v-for="d in taskTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="任务名称" prop="taskName">
                    <el-input v-model="form.taskName" placeholder="请输入任务名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="优先级">
                    <el-select v-model="form.priority" style="width:100%">
                      <el-option label="低" value="1" />
                      <el-option label="中" value="2" />
                      <el-option label="高" value="3" />
                      <el-option label="紧急" value="4" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Calendar /></el-icon></span>计划与进度
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-select v-model="form.status" style="width:100%">
                      <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
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
                  <el-form-item label="进度%">
                    <el-input-number v-model="form.progress" :min="0" :max="100" :precision="2" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计划开始">
                    <el-date-picker v-model="form.planStartDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计划结束">
                    <el-date-picker v-model="form.planEndDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>采购与描述
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" type="button">
                <el-icon><ArrowDown /></el-icon>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="需采购">
                    <el-radio-group v-model="form.needPurchase">
                      <el-radio value="1">是</el-radio>
                      <el-radio value="0">否</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="描述">
                    <el-input v-model="form.description" type="textarea" :rows="2" />
                  </el-form-item>
                </el-col>
              </el-row>
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

    <!-- ===== 发起采购弹框 ===== -->
    <el-dialog v-model="purchaseOpen" width="880px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="18"><ShoppingCart /></el-icon></div>
          <span class="rd-detail-header-title">发起采购申请</span>
          <template v-if="purchaseTask">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ purchaseTask.taskName }} ({{ purchaseTask.taskCode || '无编码' }})</span>
          </template>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('p1')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><Document /></el-icon></span>申请信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.p1 }" type="button">
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.p1">
            <el-form label-width="100px">
              <el-form-item label="关联任务">
                <span class="badge amber">
                  <span class="dot"></span>{{ purchaseTask?.taskName }} ({{ purchaseTask?.taskCode || '无编码' }})
                </span>
              </el-form-item>
              <el-form-item label="采购标题" required>
                <el-input v-model="purchaseForm.title" placeholder="如：XX项目-服务器采购" />
              </el-form-item>
              <el-form-item label="采购用途">
                <el-input v-model="purchaseForm.purpose" type="textarea" :rows="2" placeholder="采购用途/说明" />
              </el-form-item>
            </el-form>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('p2')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><Tickets /></el-icon></span>采购明细（至少一条）
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.p2 }" type="button">
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.p2">
            <el-table :data="purchaseForm.details" border class="app-table">
              <el-table-column label="物料" width="260">
                <template #default="scope">
                  <el-input
                    :model-value="scope.row.materialName ? (scope.row.materialCode ? scope.row.materialCode + ' - ' + scope.row.materialName : scope.row.materialName) : ''"
                    readonly size="small"
                    placeholder="点击浏览选择物料"
                    style="cursor:pointer"
                    @click="openPurchaseMaterialPicker(scope.$index)">
                    <template #append>
                      <el-button v-if="scope.row.materialId" icon="CircleClose" size="small" @click.stop="clearPurchaseMaterial(scope.$index)" />
                      <el-button v-else icon="Search" size="small" @click.stop="openPurchaseMaterialPicker(scope.$index)" />
                    </template>
                  </el-input>
                  <div v-if="!scope.row.materialName" style="margin-top:2px; font-size:12px; color:var(--ink-400);">
                    或手动填写下方字段 →
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="规格型号" width="160">
                <template #default="scope">
                  <el-input v-model="scope.row.specModel" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="数量" width="110">
                <template #default="scope">
                  <el-input-number v-model="scope.row.qty" :min="0.01" size="small" style="width:100%" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="单价" width="120">
                <template #default="scope">
                  <el-input-number v-model="scope.row.estimatedPrice" :min="0" :precision="2" size="small" style="width:100%" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="金额" width="130" align="right" class-name="col-num">
                <template #default="scope">
                  <span class="rd-amount">¥{{ Number((scope.row.qty || 0) * (scope.row.estimatedPrice || 0)).toLocaleString() }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="scope">
                  <el-button link type="danger" icon="Delete" @click="removePurchaseDetail(scope.$index)" />
                </template>
              </el-table-column>
            </el-table>
            <div class="purchase-foot">
              <el-button type="primary" plain icon="Plus" size="small" @click="addPurchaseDetail">添加明细</el-button>
              <span class="purchase-total">合计：<span class="rd-amount">¥{{ purchaseTotal.toLocaleString() }}</span></span>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :disabled="purchaseForm.details.length === 0" @click="submitPurchase">提交申请</el-button>
          <el-button @click="purchaseOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <UserPicker ref="ownerPickerRef" title="选择任务负责人" @confirm="onOwnerConfirm" />
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
    <MaterialPicker ref="purchaseMatPickerRef" title="选择物料" @confirm="onPurchaseMaterialConfirm" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProject, buildPurchase, getPurchaseSummary } from '@/api/pjm/project'
import { getTaskTree, getTask, addTask, updateTask, delTask } from '@/api/pjm/task'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
import ProjectPicker from '@/components/ProjectPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { CircleClose } from '@element-plus/icons-vue'

const { collapsedCards, toggleCard } = useDetailCard(['c1', 'c2', 'c3', 'p1', 'p2'])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_task_index')

const loading = ref(false)
const showSearch = ref(true)
const showAdvanced = ref(false)
const selectedProjectId = ref(null)
const projectOptions = ref([])
const treeData = ref([])
const selectedRows = ref([])
const single = ref(true)
const open = ref(false)
const title = ref('')
const purchaseSummary = ref(null)

const form = reactive({})
const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }]
}

/** 负责人选择（组织机构） */
const ownerPickerRef = ref()
function openOwnerPicker() { ownerPickerRef.value.open(form.ownerId) }
function onOwnerConfirm(u) { form.ownerId = u.userId; form.ownerName = u.nickName }
function clearOwner() { form.ownerId = null; form.ownerName = null }

const statusOptions = [
  { value: '0', label: '未开始' }, { value: '1', label: '进行中' },
  { value: '2', label: '完成' }, { value: '3', label: '取消' }, { value: '4', label: '延期' }
]
const taskTypeOptions = [
  { value: '1', label: '单项工程' }, { value: '2', label: '单位工程' },
  { value: '3', label: '分部工程' }, { value: '4', label: '分项工程' },
  { value: '5', label: '检验批' }
]

// 列显隐配置
const columns = ref({
  taskCode: { label: '任务编码', visible: true },
  taskName: { label: '任务名称', visible: true },
  taskType: { label: '类型', visible: true },
  status: { label: '状态', visible: true },
  progress: { label: '进度', visible: true },
  ownerName: { label: '负责人', visible: true },
  planStartDate: { label: '计划开始', visible: true },
  planEndDate: { label: '计划结束', visible: true },
  needPurchase: { label: '采购', visible: true }
})

const currentProject = computed(() => projectOptions.value.find(p => p.projectId === selectedProjectId.value))
const selectedProjectLabel = computed(() => currentProject.value ? currentProject.value.projectNo + ' ' + currentProject.value.projectName : '')
const projectPickerRef = ref()

function openProjectPicker() {
  projectPickerRef.value?.open(selectedProjectId.value)
}

function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => x.projectId === p.projectId)) projectOptions.value.push(p)
  selectedProjectId.value = p.projectId
  onProjectChange()
}
const activeFilterCount = computed(() => (selectedProjectId.value ? 1 : 0))

const taskTypeLabel = (v) => taskTypeOptions.find(x => x.value === v)?.label || v || '-'
const statusLabel = (v) => statusOptions.find(x => x.value === v)?.label || v
const statusBadge = (v) => ({ '0': 'gray', '1': 'blue', '2': 'green', '3': 'gray', '4': 'red' }[v] || 'gray')
const projectStatusLabel = (v) => ({ '0': '草稿', '1': '待审批', '2': '审批通过', '3': '执行中', '4': '已完成', '5': '已关闭', '6': '暂停', '7': '驳回' }[v] || v || '-')
const projectStatusBadge = (v) => ({ '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'green', '5': 'gray', '6': 'amber', '7': 'red' }[v] || 'gray')
const money = (v) => Number(v || 0).toLocaleString()

// 加载项目列表
function loadProjectOptions() {
  listProject({ pageNum: 1, pageSize: 1000 }).then(res => {
    projectOptions.value = res.rows || []
  })
}

function loadSummary() {
  if (!selectedProjectId.value) { purchaseSummary.value = null; return }
  getPurchaseSummary(selectedProjectId.value).then(res => {
    purchaseSummary.value = res.data
  })
}

function onProjectChange() {
  loadTree()
  loadSummary()
}

function refreshAll() {
  loadTree()
  loadSummary()
}

function resetQuery() {
  selectedProjectId.value = null
  onProjectChange()
}

// 加载任务树
function loadTree() {
  if (!selectedProjectId.value) { treeData.value = []; return }
  loading.value = true
  getTaskTree(selectedProjectId.value).then(res => {
    treeData.value = res.data || []
    applySavedWidths()
  }).finally(() => { loading.value = false })
}

function handleSelectionChange(selection) {
  selectedRows.value = selection
  single.value = selection.length !== 1
}

function handleUpdateSelected() {
  if (selectedRows.value.length !== 1) return
  handleEdit(selectedRows.value[0])
}

function handleDeleteSelected() {
  if (selectedRows.value.length !== 1) return
  handleDelete(selectedRows.value[0])
}

function handleAdd(parentRow) {
  title.value = parentRow ? '新增子任务' : '新增顶级任务'
  Object.assign(form, {
    taskId: null,
    projectId: selectedProjectId.value,
    parentTaskId: parentRow?.taskId || null,
    taskCode: parentRow ? (parentRow.taskCode ? parentRow.taskCode + '.' + (parentRow.children?.length || 0) + 1 : '') : '',
    taskName: '',
    taskType: '1',
    status: '0',
    priority: '2',
    progress: 0,
    needPurchase: '0',
    ownerName: '',
    planStartDate: null,
    planEndDate: null,
    description: ''
  })
  open.value = true
}

function handleEdit(row) {
  title.value = '修改任务'
  getTask(row.taskId).then(res => { Object.assign(form, res.data); open.value = true })
}

function submitForm() {
  if (form.children) delete form.children
  if (form.taskId != null) {
    updateTask(form).then(() => { ElMessage.success('修改成功'); open.value = false; loadTree() })
  } else {
    addTask(form).then(() => { ElMessage.success('新增成功'); open.value = false; loadTree() })
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除任务「${row.taskName}」及其所有子任务?`, '提示', { type: 'warning' }).then(() => {
    delTask(row.taskId).then(() => { ElMessage.success('删除成功'); loadTree() })
  })
}

// ============ 发起采购 ============
const purchaseOpen = ref(false)
const purchaseTask = ref(null)
const purchaseForm = reactive({ title: '', purpose: '', details: [] })

const purchaseTotal = computed(() => {
  return purchaseForm.details.reduce((sum, d) => sum + (Number(d.qty || 0) * Number(d.estimatedPrice || 0)), 0)
})

/** 采购物料浏览框 */
const purchaseMatPickerRef = ref()
const currentPurchaseDetailIndex = ref(-1)

function handleBuildPurchase(row) {
  purchaseTask.value = row
  purchaseForm.title = (currentProject.value?.projectName || '') + ' - ' + (row.taskName || '') + ' 采购'
  purchaseForm.purpose = 'WBS任务[' + (row.taskName || '') + ']采购需求'
  purchaseForm.details = [{ materialId: null, materialCode: '', materialName: '', specModel: '', qty: 1, estimatedPrice: 0 }]
  purchaseOpen.value = true
}

function addPurchaseDetail() {
  purchaseForm.details.push({ materialId: null, materialCode: '', materialName: '', specModel: '', qty: 1, estimatedPrice: 0 })
}

function removePurchaseDetail(idx) {
  purchaseForm.details.splice(idx, 1)
}

function openPurchaseMaterialPicker(idx) {
  currentPurchaseDetailIndex.value = idx
  const row = purchaseForm.details[idx]
  purchaseMatPickerRef.value?.open(row?.materialId)
}

function clearPurchaseMaterial(idx) {
  const row = purchaseForm.details[idx]
  if (!row) return
  row.materialId = null
  row.materialCode = ''
  row.materialName = ''
  row.specModel = ''
  ElMessage.info('已解除物料关联，可手动填写')
}

function onPurchaseMaterialConfirm(material) {
  const idx = currentPurchaseDetailIndex.value
  if (idx < 0 || !purchaseForm.details[idx]) return
  const row = purchaseForm.details[idx]
  row.materialId = material.materialId
  row.materialCode = material.materialCode
  row.materialName = material.materialName
  row.specModel = material.specModel
  ElMessage.success(`已关联物料：${material.materialCode} ${material.materialName}`)
}

function submitPurchase() {
  if (!purchaseForm.title.trim()) { ElMessage.warning('请输入采购标题'); return }
  if (purchaseForm.details.length === 0) { ElMessage.warning('请至少添加一条采购明细'); return }
  // 清理空行
  const validDetails = purchaseForm.details.filter(d => d.materialName && d.materialName.trim())
  if (validDetails.length === 0) { ElMessage.warning('请填写采购明细的物料名称'); return }

  buildPurchase({
    projectId: selectedProjectId.value,
    taskId: purchaseTask.value?.taskId,
    title: purchaseForm.title,
    purpose: purchaseForm.purpose,
    details: validDetails
  }).then(res => {
    ElMessage.success('采购申请已发起！单号已生成，申请ID=' + res.data)
    purchaseOpen.value = false
    loadTree()
    loadSummary()  // 刷新采购汇总
  })
}

onMounted(() => { loadProjectOptions() })
</script>

<style scoped>
.pjm-task-page .tree-label { font-size: 14px; font-weight: 600; color: var(--ink-700); }
.pjm-task-page .tree-empty { padding: 48px 0; }
.pjm-task-page .control-text { font-size: 14px; color: var(--ink-700); font-variant-numeric: tabular-nums; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pjm-task-page .muted-dash { color: var(--ink-400); }
/* 发起采购弹框（append-to-body，token 需带回退值） */
.purchase-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; }
.purchase-total { font-size: 13px; color: var(--ink-500, #64748b); }

/* 发起采购 — 物料浏览框 append 按钮样式 */
.pjm-task-page .el-input-group__append {
  background: transparent !important; border: 0 !important; box-shadow: none !important;
  padding: 0 0 0 4px; color: var(--brand-600);
}
.pjm-task-page .el-input-group__append .el-button {
  margin: 0; height: 24px; padding: 0 7px; border-radius: 6px;
  background: var(--brand-50, #eef2ff); border: 1px solid var(--brand-200, #c7d2fe);
  color: var(--brand-600);
}
.pjm-task-page .el-input-group__append .el-button:hover {
  background: #e0e7ff; border-color: var(--brand-300, #a5b4fc); color: var(--brand-700);
}
.pjm-task-page .el-input-group__append .el-button .el-icon { font-size: 13px; }
</style>
