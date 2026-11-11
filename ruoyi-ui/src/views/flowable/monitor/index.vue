<template>
  <div class="app-container flowable-monitor-page">
    <div class="task-layout">
      <!-- ===== 左侧：流程分类树 ===== -->
      <div class="surface tree-card" :class="{ 'is-collapsed': !treeExpanded }">
        <div class="tree-head">
          <template v-if="treeExpanded">
            <div class="filter-title"><span class="glyph"></span> 流程分类</div>
          </template>
          <el-tooltip :content="treeExpanded ? '收起侧栏' : '展开侧栏'" placement="right">
            <el-icon class="tree-toggle" @click="toggleTree"><Fold v-if="treeExpanded" /><Expand v-else /></el-icon>
          </el-tooltip>
        </div>
        <template v-if="treeExpanded">
          <div class="tree-search">
            <el-input v-model="treeKeyword" placeholder="搜索流程类型" clearable>
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="tree-body">
            <el-tree
              ref="treeRef"
              :data="treeData"
              :key="treeRenderKey"
              node-key="id"
              :props="{ label: 'label', children: 'children' }"
              :default-expand-all="treeExpanded"
              :filter-node-method="filterNode"
              highlight-current
              :expand-on-click-node="false"
              @node-click="handleNodeClick">
              <template #default="{ data }">
                <span class="tree-node" :class="{ 'is-leaf': !data.children }">
                  <el-icon v-if="data.children" class="tree-icon"><Folder /></el-icon>
                  <el-icon v-else class="tree-icon leaf"><Document /></el-icon>
                  <span class="tree-label">{{ data.label }}</span>
                  <span class="tree-count" :class="{ 'is-zero': !data.count }">{{ data.count || 0 }}</span>
                </span>
              </template>
            </el-tree>
            <div class="tree-empty" v-if="!treeLoading && !treeData.length">暂无流程定义</div>
          </div>
        </template>
      </div>

      <!-- ===== 右侧：流程监控列表 ===== -->
      <div class="task-main">
        <!-- 筛选条件卡片 -->
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
              <label>流程标题</label>
              <div class="control">
                <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Document /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>流程名称</label>
              <div class="control">
                <el-input v-model="queryParams.processDefinitionName" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>创建时间</label>
              <div class="control">
                <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期"
                  end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:100%" />
              </div>
            </div>
            <div class="field">
              <label>当前节点</label>
              <div class="control">
                <el-input v-model="queryParams.currentActivityName" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Guide /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>创建人</label>
              <div class="control">
                <el-input v-model="queryParams.startUserName" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>流程编号</label>
              <div class="control">
                <el-input v-model="queryParams.instanceId" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><CollectionTag /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>当前状态</label>
              <div class="control is-select">
                <el-select v-model="queryParams.status" placeholder="全部" clearable @change="syncStatusTab">
                  <el-option label="进行中" value="running" />
                  <el-option label="已结束" value="completed" />
                  <el-option label="已撤销" value="canceled" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索；查询范围：全部流程实例（管理员视角）</div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- 数据列表 -->
        <div class="surface">
          <div class="status-tabs">
            <div class="tabs-track">
              <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
                <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
              </button>
              <button class="status-tab tab-audit" :class="{ 'is-active': activeStatusTab === 'running' }" @click="handleStatusTabClick('running')">
                <span class="dot"></span><span>进行中</span><span class="count">{{ statusCounts.running }}</span>
              </button>
              <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === 'completed' }" @click="handleStatusTabClick('completed')">
                <span class="dot"></span><span>已结束</span><span class="count">{{ statusCounts.completed }}</span>
              </button>
              <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === 'canceled' }" @click="handleStatusTabClick('canceled')">
                <span class="dot"></span><span>已撤销</span><span class="count">{{ statusCounts.canceled }}</span>
              </button>
            </div>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_monitor_columns" />
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
              <el-table-column type="index" label="序号" :width="colWidth('序号', 85)" align="center" />
              <el-table-column v-if="columns.title.visible" label="流程标题" prop="title" key="title" :min-width="colWidth('title', 210)" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="urgencyBadge(scope.row.urgency)" class="badge" :class="urgencyBadge(scope.row.urgency).cls">{{ urgencyBadge(scope.row.urgency).text }}</span>
                  <span class="col-strong link-like" role="link" tabindex="0" @click="handleView(scope.row)" @keydown.enter="handleView(scope.row)">{{ scope.row.title || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.processDefinitionName.visible" label="流程名称" prop="processDefinitionName" key="processDefinitionName" :min-width="colWidth('processDefinitionName', 150)" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.processDefinitionName || '-' }}</template>
              </el-table-column>
              <el-table-column v-if="columns.startUserName.visible" label="创建人" prop="startUserName" key="startUserName" :width="colWidth('startUserName', 110)" align="center" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.startUserName || '-' }}</template>
              </el-table-column>
              <el-table-column v-if="columns.startTime.visible" label="创建时间" prop="startTime" key="startTime" :width="colWidth('startTime', 170)" align="center" />
              <el-table-column v-if="columns.currentActivityName.visible" label="当前节点" prop="currentActivityName" key="currentActivityName" :min-width="colWidth('currentActivityName', 130)" show-overflow-tooltip>
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === 'running' ? 'blue' : 'gray'"><span class="dot"></span>{{ scope.row.currentActivityName || (scope.row.status === 'completed' ? '流程结束' : '已中断') }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.status.visible" label="状态" prop="status" key="status" :width="colWidth('status', 100)" align="center">
                <template #default="scope">
                  <span class="badge" :class="statusBadge(scope.row)"><span class="dot"></span>{{ statusLabel(scope.row) }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="columns.instanceId.visible" label="流程编号" prop="instanceId" key="instanceId" :min-width="colWidth('instanceId', 180)" show-overflow-tooltip>
                <template #default="scope"><span class="col-mono">{{ scope.row.instanceId || '-' }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="210" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                    <el-button v-if="scope.row.status === 'running'" link type="warning" icon="Position"
                      v-hasPermi="['flowable:monitor:intervene']" @click="openIntervene(scope.row)">干预</el-button>
                    <el-dropdown trigger="click" @command="cmd => handleCommand(cmd, scope.row)">
                      <el-button link type="primary" icon="MoreFilled">更多</el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="recall"
                            v-hasPermi="['flowable:monitor:intervene']">强制收回</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="assign"
                            v-hasPermi="['flowable:monitor:intervene']">改派办理人</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="end" divided
                            v-hasPermi="['flowable:monitor:end']">强制结束</el-dropdown-item>
                          <el-dropdown-item v-if="scope.row.status === 'running'" command="cancel"
                            v-hasPermi="['flowable:monitor:end']">撤销流程</el-dropdown-item>
                          <el-dropdown-item command="remove" divided
                            v-hasPermi="['flowable:monitor:remove']">删除流程</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination-container">
            <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
              v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 流程干预弹窗：强制跳转到指定节点 ===== -->
    <el-dialog v-model="intervene.open" title="流程干预" width="660px" append-to-body class="rd-dialog" :close-on-click-modal="false">
      <div class="monitor-info" v-loading="intervene.loading">
        <div class="info-row"><span class="label">流程标题</span><span class="value strong">{{ intervene.title || '-' }}</span></div>
        <div class="info-row"><span class="label">流程名称</span><span class="value">{{ intervene.processName || '-' }}</span></div>
        <div class="info-row">
          <span class="label">当前节点</span>
          <span class="value">
            <span v-if="!intervene.currentNames.length" class="muted">流程已结束</span>
            <span v-for="(n, i) in intervene.currentNames" :key="i" class="badge blue"><span class="dot"></span>{{ n }}</span>
          </span>
        </div>
        <div class="info-row" v-if="intervene.currentTasks.length">
          <span class="label">当前办理人</span>
          <span class="value">
            <span v-for="t in intervene.currentTasks" :key="t.taskId" class="task-chip">
              {{ t.taskName }}：{{ t.assigneeName || '未指派（候选池）' }}
            </span>
          </span>
        </div>
      </div>
      <el-form label-width="92px" class="monitor-form">
        <el-form-item label="目标节点" required>
          <el-select v-model="intervene.form.targetActivityId" placeholder="请选择要强制跳转到的节点" filterable style="width:100%">
            <el-option v-for="n in intervene.nodes" :key="n.id" :label="n.name" :value="n.id">
              <span>{{ n.name }}</span>
              <span class="opt-tags">
                <span v-if="n.current" class="opt-tag current">当前</span>
                <span v-if="n.multiInstance" class="opt-tag multi">会签</span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="指定办理人">
          <el-input :model-value="intervene.form.assigneeName" readonly placeholder="不选则由节点「操作人员」配置自动派发">
            <template #append>
              <el-button @click="pickAssignee('intervene')">选择</el-button>
            </template>
          </el-input>
          <div class="form-tip">仅对普通审批节点生效；会签/串签节点由配置的集合自动派发。</div>
        </el-form-item>
        <el-form-item label="干预意见">
          <el-input v-model="intervene.form.comment" type="textarea" :rows="3" placeholder="填写干预原因，将写入流程流转意见" />
        </el-form-item>
      </el-form>
      <div class="monitor-warn">
        <el-icon><WarningFilled /></el-icon>
        干预会跳过中间审批节点，被跳过的环节不会再生成待办；操作会记录到流程流转意见与系统操作日志。
      </div>
      <template #footer>
        <el-button size="small" @click="intervene.open = false">取 消</el-button>
        <el-button size="small" type="warning" :loading="intervene.submitting" @click="submitIntervene">确认干预</el-button>
      </template>
    </el-dialog>

    <!-- ===== 改派办理人弹窗 ===== -->
    <el-dialog v-model="assign.open" title="改派办理人" width="560px" append-to-body class="rd-dialog" :close-on-click-modal="false">
      <div class="monitor-info">
        <div class="info-row"><span class="label">流程标题</span><span class="value strong">{{ assign.title || '-' }}</span></div>
      </div>
      <el-form label-width="92px" class="monitor-form">
        <el-form-item label="待办任务" required v-if="assign.tasks.length > 1">
          <el-select v-model="assign.taskId" placeholder="请选择要改派的任务" style="width:100%">
            <el-option v-for="t in assign.tasks" :key="t.taskId" :label="t.taskName + '（' + (t.assigneeName || '未指派') + '）'" :value="t.taskId" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前环节" v-else>
          <span class="cur-task">{{ assign.tasks[0] ? assign.tasks[0].taskName + '（' + (assign.tasks[0].assigneeName || '未指派（候选池）') + '）' : '-' }}</span>
        </el-form-item>
        <el-form-item label="新办理人" required>
          <el-input :model-value="assign.targetUserName" readonly placeholder="请选择新的办理人">
            <template #append>
              <el-button @click="pickAssignee('assign')">选择</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="改派说明">
          <el-input v-model="assign.comment" type="textarea" :rows="2" placeholder="填写改派原因（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="assign.open = false">取 消</el-button>
        <el-button size="small" type="primary" :loading="assign.submitting" @click="submitAssign">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择（干预指定办理人 / 改派） -->
    <UserPicker ref="userPickerRef" :multiple="false" title="选择办理人" @confirm="onUserPicked" />
  </div>
</template>

<script setup name="FlowMonitor">
import {
  listMonitorProcess, countMonitorProcess, getMonitorNodes,
  interveneProcess, recallMonitorProcess, assignMonitorTask, endMonitorProcess, deleteMonitorProcess
} from '@/api/flowable/monitor'
import { getProcessTree } from '@/api/flowable/flow'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useRouter } from 'vue-router'
import { Search, Filter, Document, Guide, User, CollectionTag, ArrowDown, Folder, Fold, Expand, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_monitor_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, running: 0, completed: 0, canceled: 0 })
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1, pageSize: 10, title: undefined, processDefinitionName: undefined,
    startUserName: undefined, currentActivityName: undefined, instanceId: undefined,
    processDefinitionKey: undefined, status: undefined
  }
})
const { queryParams } = toRefs(data)

/** 紧急程度徽标：1普通（不显示）/ 2紧急 / 3特急 */
function urgencyBadge(v) {
  if (v === '3' || v === 3) return { text: '特急', cls: 'red' }
  if (v === '2' || v === 2) return { text: '紧急', cls: 'amber' }
  return null
}

/** 状态徽标：强制结束与撤销都用中性色，仅文案区分 */
function statusBadge(row) {
  if (row.status === 'running') return 'blue'
  if (row.status === 'completed') return 'green'
  return row.endType === 'forceEnd' ? 'amber' : 'gray'
}
function statusLabel(row) {
  if (row.status === 'running') return '进行中'
  if (row.status === 'completed') return '已结束'
  return row.endType === 'forceEnd' ? '强制结束' : '已撤销'
}

// ===== 左侧流程树 =====
const treeRef = ref(null)
const treeData = ref([])
const treeLoading = ref(false)
const treeKeyword = ref('')
const treeExpanded = ref(true)
const treeRenderKey = ref(0)

watch(treeKeyword, val => { treeRef.value && treeRef.value.filter(val) })

function toggleTree() {
  treeExpanded.value = !treeExpanded.value
  treeRenderKey.value++
}
function filterNode(value, nodeData) {
  if (!value) return true
  return (nodeData.label || '').includes(value)
}
function loadTree() {
  treeLoading.value = true
  getProcessTree().then(res => {
    const groups = res.data || []
    const totalCount = groups.reduce((s, g) => s + (g.count || 0), 0)
    treeData.value = [{ id: '__all__', label: '全部流程', count: totalCount, children: groups }].filter(g => groups.length || g.children)
    treeLoading.value = false
  }).catch(() => { treeLoading.value = false })
}
function handleNodeClick(node) {
  queryParams.value.processDefinitionKey = node.children || !node.key ? undefined : node.key
  handleQuery()
}

// ===== 列显隐 =====
const defaultColumns = {
  title: { label: '流程标题', visible: true },
  processDefinitionName: { label: '流程名称', visible: true },
  startUserName: { label: '创建人', visible: true },
  startTime: { label: '创建时间', visible: true },
  currentActivityName: { label: '当前节点', visible: true },
  status: { label: '状态', visible: true },
  instanceId: { label: '流程编号', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_monitor_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.title) count++
  if (queryParams.value.processDefinitionName) count++
  if (queryParams.value.startUserName) count++
  if (queryParams.value.currentActivityName) count++
  if (queryParams.value.instanceId) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value)
  listMonitorProcess(params).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  countMonitorProcess().then(res => {
    const d = res.data || {}
    statusCounts.value = {
      all: d.all || 0, running: d.running || 0,
      completed: d.completed || 0, canceled: d.canceled || 0
    }
  }).catch(() => { statusCounts.value = { all: 0, running: 0, completed: 0, canceled: 0 } })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.title = undefined
  queryParams.value.processDefinitionName = undefined
  queryParams.value.startUserName = undefined
  queryParams.value.currentActivityName = undefined
  queryParams.value.instanceId = undefined
  queryParams.value.processDefinitionKey = undefined
  queryParams.value.status = undefined
  dateRange.value = []
  activeStatusTab.value = 'all'
  showAdvanced.value = false
  if (treeRef.value) treeRef.value.setCurrentKey(null)
  handleQuery()
}
function handleStatusTabClick(s) {
  activeStatusTab.value = s
  queryParams.value.status = s === 'all' ? undefined : s
  handleQuery()
}
/** 高级筛选里的状态下拉与页签联动 */
function syncStatusTab() {
  const s = queryParams.value.status
  activeStatusTab.value = ['running', 'completed', 'canceled'].includes(s) ? s : 'all'
  handleQuery()
}

function refreshAll() { getList(); loadStatusCounts() }

/** 查看流程详情：新标签页打开独立页面 */
function handleView(row) {
  const href = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: row.instanceId || '', defName: row.processDefinitionName || row.title || '' }
  }).href
  window.open(href, '_blank')
}

// ===== 人员选择（干预指定办理人 / 改派共用） =====
const userPickerRef = ref(null)
const pickTarget = ref('intervene')
function pickAssignee(target) {
  pickTarget.value = target
  if (userPickerRef.value) userPickerRef.value.open()
}
function onUserPicked(users) {
  const user = (Array.isArray(users) ? users : [users]).filter(u => u && u.userId)[0]
  if (!user) return
  const name = user.nickName || user.userName
  if (pickTarget.value === 'intervene') {
    intervene.form.assigneeId = user.userId
    intervene.form.assigneeName = name
  } else {
    assign.targetUserId = user.userId
    assign.targetUserName = name
  }
}

// ===== 流程干预 =====
const intervene = reactive({
  open: false, loading: false, submitting: false,
  instanceId: '', title: '', processName: '',
  currentNames: [], currentTasks: [], nodes: [],
  form: { targetActivityId: '', assigneeId: null, assigneeName: '', comment: '' }
})

function openIntervene(row) {
  intervene.open = true
  intervene.loading = true
  intervene.submitting = false
  intervene.instanceId = row.instanceId
  intervene.title = row.title
  intervene.processName = row.processDefinitionName
  intervene.currentNames = []
  intervene.currentTasks = []
  intervene.nodes = []
  intervene.form = { targetActivityId: '', assigneeId: null, assigneeName: '', comment: '' }
  getMonitorNodes(row.instanceId).then(res => {
    const d = res.data || {}
    intervene.title = d.title || row.title
    intervene.processName = d.processDefinitionName || row.processDefinitionName
    intervene.currentNames = d.currentActivityNames || []
    intervene.currentTasks = d.currentTasks || []
    intervene.nodes = d.nodes || []
    intervene.loading = false
  }).catch(() => { intervene.loading = false })
}

function submitIntervene() {
  const f = intervene.form
  if (!f.targetActivityId) {
    proxy.$modal.msgWarning('请选择目标节点')
    return
  }
  const target = intervene.nodes.find(n => n.id === f.targetActivityId)
  const targetName = target ? target.name : f.targetActivityId
  proxy.$modal.confirm('确认把流程强制干预到「' + targetName + '」？跳过的中间节点不会再生成待办。').then(() => {
    intervene.submitting = true
    return interveneProcess(intervene.instanceId, {
      targetActivityId: f.targetActivityId,
      assigneeId: f.assigneeId || undefined,
      comment: f.comment || undefined
    })
  }).then(() => {
    proxy.$modal.msgSuccess('流程已干预至「' + targetName + '」')
    intervene.open = false
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('干预失败：' + (e?.message || e))
  }).finally(() => { intervene.submitting = false })
}

// ===== 改派办理人 =====
const assign = reactive({
  open: false, submitting: false,
  instanceId: '', title: '', tasks: [], taskId: '',
  targetUserId: null, targetUserName: '', comment: ''
})

function openAssign(row) {
  assign.open = true
  assign.submitting = false
  assign.instanceId = row.instanceId
  assign.title = row.title
  assign.tasks = []
  assign.taskId = ''
  assign.targetUserId = null
  assign.targetUserName = ''
  assign.comment = ''
  getMonitorNodes(row.instanceId).then(res => {
    const d = res.data || {}
    assign.tasks = d.currentTasks || []
    assign.taskId = assign.tasks.length === 1 ? assign.tasks[0].taskId : ''
  }).catch(() => {})
}

function submitAssign() {
  if (!assign.taskId) {
    proxy.$modal.msgWarning('请选择待改派的任务')
    return
  }
  if (!assign.targetUserId) {
    proxy.$modal.msgWarning('请选择新的办理人')
    return
  }
  assign.submitting = true
  assignMonitorTask(assign.taskId, assign.targetUserId, assign.comment || undefined).then(() => {
    proxy.$modal.msgSuccess('已改派给「' + assign.targetUserName + '」')
    assign.open = false
    refreshAll()
  }).catch(e => {
    proxy.$modal.msgError('改派失败：' + (e?.message || e))
  }).finally(() => { assign.submitting = false })
}

// ===== 更多操作：强制收回 / 改派 / 强制结束 / 撤销 / 删除 =====
function handleCommand(cmd, row) {
  if (cmd === 'recall') confirmRecall(row)
  else if (cmd === 'assign') openAssign(row)
  else if (cmd === 'end') confirmEnd(row, 'end')
  else if (cmd === 'cancel') confirmEnd(row, 'cancel')
  else if (cmd === 'remove') confirmRemove(row)
}

/** 强制收回：回退到上一节点，由上一节点原办理人重新办理（设计口径：不填意见、流程记录不留痕） */
function confirmRecall(row) {
  const cur = row.currentActivityName ? '「' + row.currentActivityName + '」' : '当前节点'
  proxy.$modal.confirm('确认把流程「' + row.title + '」从' + cur + '强制收回到上一节点？收回后上一节点的原办理人将重新收到待办，当前待办作废；该操作不写流转意见，也不在流程记录中留痕。', '强制收回', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    return recallMonitorProcess(row.instanceId)
  }).then(res => {
    const d = (res && res.data) || {}
    proxy.$modal.msgSuccess('已收回到「' + (d.prevActivityName || '上一节点') + '」'
      + (d.prevAssigneeName ? '，办理人：' + d.prevAssigneeName : ''))
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('强制收回失败：' + (e?.message || e))
  })
}

function confirmEnd(row, action) {
  const isEnd = action === 'end'
  const title = isEnd ? '强制结束' : '撤销流程'
  const tip = isEnd
    ? '确认强制结束流程「' + row.title + '」？结束后流程立即终止，所有待办任务将失效。'
    : '确认撤销流程「' + row.title + '」？撤销后流程终止且不可恢复（记录为已撤销）。'
  proxy.$modal.confirm(tip).then(() => {
    return proxy.$prompt('请输入' + (isEnd ? '强制结束' : '撤销') + '原因（将写入流转意见）', title, {
      confirmButtonText: '确定', cancelButtonText: '取消', inputType: 'textarea',
      inputValidator: v => (v && v.trim().length >= 2) || '请填写至少 2 个字的原因'
    })
  }).then(({ value }) => {
    return endMonitorProcess(row.instanceId, action, value)
  }).then(() => {
    proxy.$modal.msgSuccess(isEnd ? '流程已强制结束' : '流程已撤销')
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('操作失败：' + (e?.message || e))
  })
}

function confirmRemove(row) {
  proxy.$modal.confirm('确认删除流程「' + row.title + '」？删除后流程实例与全部审批记录将被彻底清除，不可恢复。').then(() => {
    return proxy.$prompt('请输入完整流程标题以确认删除', '删除流程', {
      confirmButtonText: '确认删除', cancelButtonText: '取消',
      inputValidator: v => v === row.title || '流程标题不匹配，请完整输入'
    })
  }).then(() => {
    return deleteMonitorProcess(row.instanceId)
  }).then(() => {
    proxy.$modal.msgSuccess('流程已删除')
    refreshAll()
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e))
  })
}

getList()
loadStatusCounts()
loadTree()
onActivated(() => { refreshAll(); loadTree() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.flowable-monitor-page {
  /* ===== 左树右列表布局（对齐流程查询页） ===== */
  .task-layout {
    display: flex;
    gap: 6px;
    align-items: stretch;
  }
  .task-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }
  .filter-card {
    padding: 12px 16px 14px;
    margin-bottom: 6px;
    .filter-head { margin-bottom: 10px; }
    .filter-bar { gap: 10px 14px; }
    .filter-actions { margin-top: 10px; padding-top: 10px; }
    .field .control { height: 32px; padding: 0 10px; }
    .field .control :deep(.el-input__wrapper) { height: 30px; }
    .field .control :deep(.el-input__inner) { height: 30px; line-height: 30px; }
    .field .control :deep(.el-range-editor.el-input__wrapper) { height: 30px; padding: 0 8px; }
  }
  .table-wrap { padding-top: 6px; }
  .table-wrap + .pagination-container { padding-top: 0; }
  .tree-card {
    width: 236px;
    flex-shrink: 0;
    align-self: flex-start;
    margin-bottom: 6px;
    padding: 12px 10px 10px;
    transition: width .2s ease;
    &.is-collapsed {
      width: 44px;
      padding: 14px 6px 10px;
      .tree-head {
        justify-content: center;
        border-bottom: none;
        margin-bottom: 0;
        padding-bottom: 0;
      }
    }
    .tree-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 6px 10px;
      border-bottom: 1px dashed var(--rd-border, #e5e7eb);
      margin-bottom: 8px;
      .tree-toggle {
        cursor: pointer;
        color: var(--rd-muted, #6b7280);
        &:hover { color: var(--el-color-primary); }
      }
    }
    .tree-search { padding: 0 2px 8px; }
    .tree-body { max-height: calc(100vh - 300px); overflow: auto; }
    .tree-node {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      overflow: hidden;
      .tree-icon { color: var(--el-color-primary); flex-shrink: 0; }
      .tree-icon.leaf { color: #10b981; }
      .tree-label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .tree-count {
        margin-left: auto;
        flex-shrink: 0;
        font-size: 12px;
        line-height: 1;
        padding: 3px 7px;
        border-radius: 9px;
        background: var(--el-color-primary-light-9, #ecf5ff);
        color: var(--el-color-primary);
        &.is-zero { background: #f3f4f6; color: #9ca3af; }
      }
    }
    .tree-empty {
      text-align: center;
      color: var(--rd-muted, #9ca3af);
      font-size: 13px;
      padding: 24px 0;
    }
    :deep(.el-tree-node__content) { height: 32px; border-radius: 8px; }
  }
}

/* ===== 干预 / 改派弹窗 ===== */
.monitor-info {
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 14px;
  .info-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    font-size: 13px;
    line-height: 22px;
    & + .info-row { margin-top: 4px; }
    .label { width: 72px; color: #6b7280; flex-shrink: 0; }
    .value {
      flex: 1;
      min-width: 0;
      color: #374151;
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      &.strong { color: #111827; font-weight: 600; }
      .muted { color: #9ca3af; }
    }
    .task-chip {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 6px;
      padding: 1px 8px;
      font-size: 12px;
      color: #4b5563;
    }
  }
}
.monitor-form {
  .form-tip { font-size: 12px; color: #9ca3af; line-height: 1.6; }
  .cur-task { font-size: 13px; color: #374151; }
}
.monitor-warn {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: #b45309;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 8px 12px;
  .el-icon { margin-top: 2px; flex-shrink: 0; }
}
.opt-tags {
  float: right;
  .opt-tag {
    margin-left: 6px;
    font-size: 11px;
    padding: 1px 6px;
    border-radius: 8px;
    background: #f3f4f6;
    color: #6b7280;
    &.current { background: #eff6ff; color: #2563eb; }
    &.multi { background: #fef3c7; color: #b45309; }
  }
}
</style>
