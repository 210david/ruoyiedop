<template>
  <div class="app-container flowable-task-page">
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

      <!-- ===== 右侧：待办/已办列表 ===== -->
      <div class="task-main">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
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
        <div class="field" v-if="activeTab !== 'cc'">
          <label>创建人</label>
          <div class="control">
            <el-input v-model="queryParams.startUserName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>{{ activeTab === 'done' ? '办理时间' : '创建时间' }}</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期"
              end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:100%" />
          </div>
        </div>
        <div class="field" v-if="activeTab !== 'draft' && activeTab !== 'cc'">
          <label>办理环节</label>
          <div class="control">
            <el-input v-model="queryParams.taskName" placeholder="请输入" clearable @keyup.enter="handleQuery">
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

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 页签：按规范使用统一的状态标签栏（圆点 + 名称 + 数量徽章） -->
      <div class="status-tabs">
        <div class="tabs-track">
          <template v-if="activeTab !== 'done'">
            <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="switchTab('all')">
              <span class="dot"></span><span>全部</span><span class="count">{{ allTotal || 0 }}</span>
            </button>
            <button class="status-tab tab-audit" :class="{ 'is-active': activeTab === 'todo' }" @click="switchTab('todo')">
              <span class="dot"></span><span>待办</span><span class="count">{{ todoTotal || 0 }}</span>
            </button>
            <!-- 退回：被上游驳回退回给我重新办理的流程（「待办-退回」口径） -->
            <button class="status-tab tab-reject" :class="{ 'is-active': activeTab === 'returned' }" @click="switchTab('returned')">
              <span class="dot"></span><span>退回</span><span class="count">{{ returnedTotal || 0 }}</span>
            </button>
            <button class="status-tab tab-cc" :class="{ 'is-active': activeTab === 'cc' }" @click="switchTab('cc')">
              <span class="dot"></span><span>待阅</span><span class="count">{{ ccTotal || 0 }}</span>
            </button>
            <button class="status-tab tab-draft" :class="{ 'is-active': activeTab === 'draft' }" @click="switchTab('draft')">
              <span class="dot"></span><span>草稿</span><span class="count">{{ draftTotal || 0 }}</span>
            </button>
          </template>
          <template v-else>
            <button class="status-tab" :class="{ 'is-active': doneScope === 'all' }" @click="switchDoneScope('all')">
              <span class="dot"></span><span>全部</span><span class="count">{{ doneCounts.all || 0 }}</span>
            </button>
            <button v-for="s in doneScopeTabs" :key="s.value"
              class="status-tab" :class="[statusTabClass(s.value), { 'is-active': doneScope === s.value }]"
              @click="switchDoneScope(s.value)">
              <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ doneCounts[s.value] || 0 }}</span>
            </button>
          </template>
        </div>
        <right-toolbar v-if="activeTab !== 'draft' && activeTab !== 'cc'" v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_task_columns" />
      </div>
      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" :key="activeTab" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" :width="colWidth('序号', 85)" align="center" />
          <el-table-column v-if="columns.title.visible" label="流程标题" prop="title" key="title" :min-width="colWidth('title', 220)" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="urgencyBadge(scope.row.urgency)" class="badge" :class="urgencyBadge(scope.row.urgency).cls">{{ urgencyBadge(scope.row.urgency).text }}</span>
              <span class="col-strong link-like" role="link" tabindex="0" @click="handleTitleClick(scope.row)" @keydown.enter="handleTitleClick(scope.row)">{{ scope.row.title || scope.row.processDefinitionName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.startUserName.visible && activeTab !== 'cc'" label="创建人" prop="startUserName" key="startUserName" :width="colWidth('startUserName', 120)" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.startNickName || scope.row.startUserName || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'draft' || activeTab === 'all' || activeTab === 'cc' || activeTab === 'done'" label="所属流程" prop="processName" width="180" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.processName || scope.row.processDefinitionName || scope.row.processKey || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'cc'" label="抄送人" prop="senderName" width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.senderName || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'all'" label="状态" prop="statusText" width="90" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.bizType === 'draft' ? 'gray' : (scope.row.bizType === 'cc' ? 'amber' : 'blue')"><span class="dot"></span>{{ scope.row.statusText }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'cc'" label="状态" prop="readFlag" width="90" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.readFlag === '1' ? 'gray' : 'amber'"><span class="dot"></span>{{ scope.row.readFlag === '1' ? '已读' : '待阅' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done' && columns.endTime.visible" label="办理时间" prop="endTime" key="endTime" :width="colWidth('endTime', 170)" align="center" />
          <el-table-column v-else-if="activeTab === 'draft'" label="保存时间" prop="updateTime" width="170" align="center" />
          <el-table-column v-else-if="activeTab === 'cc'" label="抄送时间" prop="createTime" width="170" align="center" />
          <el-table-column v-else-if="activeTab === 'all'" label="时间" prop="time" width="170" align="center" />
          <el-table-column v-else-if="columns.createTime.visible" label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" align="center" />
          <!-- 退回页签专用：谁在哪个环节退回的（便于判断为什么被打回） -->
          <el-table-column v-if="activeTab === 'returned'" label="退回来源" prop="returnFrom" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <span class="badge red"><span class="dot"></span>退回自：{{ scope.row.returnFrom || '-' }}</span>
              <span v-if="scope.row.returnByName" class="badge gray" style="margin-left:6px">{{ scope.row.returnByName }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="(activeTab === 'todo' || activeTab === 'returned' || activeTab === 'all' || activeTab === 'done') && columns.taskName.visible" label="办理环节" prop="taskName" key="taskName" :min-width="colWidth('taskName', 130)" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.taskName" class="badge blue"><span class="dot"></span>{{ scope.row.taskName }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done' && columns.status.visible" label="流程状态" prop="status" key="status" :width="colWidth('status', 110)" align="center">
            <template #default="scope">
              <span class="badge" :class="statusClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done'" label="当前环节" prop="currentTaskName" width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.status === 'running' ? (scope.row.currentTaskName || '办理中') : '-' }}</template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'draft'" label="操作" width="160" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="EditPen" @click="handleDraftContinue(scope.row)">继续填写</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDraftDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'cc'" label="操作" width="180" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleCcView(scope.row)">查看</el-button>
                <el-button v-if="scope.row.readFlag !== '1'" link type="primary" icon="Check" @click="handleCcRead(scope.row)">标记已读</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-else-if="activeTab === 'all'" label="操作" width="190" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row" v-if="scope.row.bizType === 'draft'">
                <el-button link type="primary" icon="EditPen" @click="handleDraftContinue(scope.row)">继续填写</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDraftDelete(scope.row)">删除</el-button>
              </div>
              <div class="action-btn-row" v-else-if="scope.row.bizType === 'cc'">
                <el-button link type="primary" icon="View" @click="handleCcView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Check" @click="handleCcRead(scope.row)">标记已读</el-button>
              </div>
              <div class="action-btn-row" v-else>
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">流程详情</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column v-else label="操作" width="100" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <!-- 列表不再提供「审批」按钮：待办直接点击流程标题进入办理页，此处仅保留流程详情 -->
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">流程详情</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="pageTotal"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>
      </div>
    </div>
  </div>
</template>

<script setup name="FlowTask">
import { listTodoTask, listDoneTask, countDoneTask, listAllTask, countTodoTask, countTodoTasks, getProcessTree } from '@/api/flowable/flow'
import { listDraft, delDraft } from '@/api/flowable/draft'
import { listMyCc, getCcUnreadCount, readCc } from '@/api/flowable/cc'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, Document, Folder, User, Fold, Expand } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_task_index')
/**
 * 生成独立页地址：优先用 router.resolve（保留基路径），
 * 极端情况下 router 未就绪时回退为字符串地址，保证按钮始终可用。
 */
function generateRouteHref(location) {
  try {
    return router.resolve(location).href
  } catch (e) {
    const q = new URLSearchParams(location.query || {}).toString()
    return location.path + (q ? '?' + q : '')
  }
}

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
/** 当前页签：全部 / 待办 / 退回 / 待阅 / 草稿（由地址栏 tab 参数或菜单路径推导）；已办走独立菜单同组件 `/flow/done` */
function resolveTab() {
  const q = route.query.tab
  if (q === 'all' || q === 'draft' || q === 'todo' || q === 'cc' || q === 'returned') return q
  if (route.path && route.path.endsWith('/done')) return 'done'
  return 'todo'
}
const activeTab = ref(resolveTab())
const allTotal = ref(0)
const todoTotal = ref(0)
/** 被退回的待办数量（待办页「退回」页签角标） */
const returnedTotal = ref(0)
/** 已办页签（全部/未结束/已结束）数量与当前选中项 */
const doneScopeTabs = [
  { value: 'running', label: '未结束' },
  { value: 'finished', label: '已结束' }
]
const doneCounts = reactive({ all: 0, running: 0, finished: 0 })
/** 已办状态页签配色（对齐 list-page-style-guide 第 3 章）：未结束=蓝、已结束=绿 */
function statusTabClass(v) {
  if (v === 'running') return 'tab-audit'
  if (v === 'finished') return 'tab-approved'
  return ''
}
/** 已办页签从地址栏 scope 参数推导，刷新后保持 */
function resolveDoneScope() {
  const s = route.query.scope
  return s === 'running' || s === 'finished' ? s : 'all'
}
const doneScope = ref(resolveDoneScope())
/** 未读待阅数量 */
const ccTotal = ref(0)
/** 待阅列表总数（含已读） */
const ccPageTotal = ref(0)
/** 草稿总数（我的未提交草稿） */
const draftTotal = ref(0)
const pageTotal = computed(() => {
  if (activeTab.value === 'all') return allTotal.value
  if (activeTab.value === 'done') return doneCounts[doneScope.value] || 0
  if (activeTab.value === 'draft') return draftTotal.value
  if (activeTab.value === 'cc') return ccPageTotal.value
  if (activeTab.value === 'returned') return returnedTotal.value
  return todoTotal.value
})
/** 紧急程度徽标：1普通（不显示）/ 2紧急 / 3特急 */
function urgencyBadge(v) {
  if (v === '3' || v === 3) return { text: '特急', cls: 'red' }
  if (v === '2' || v === 2) return { text: '紧急', cls: 'amber' }
  return null
}

/** 已办「流程状态」徽标：running 办理中 / completed 已办结 / canceled 已撤销 */
const STATUS_MAP = {
  running: { text: '办理中', cls: 'blue' },
  completed: { text: '已办结', cls: 'green' },
  canceled: { text: '已撤销', cls: 'gray' }
}
function statusLabel(v) {
  return (STATUS_MAP[v] || { text: '-' }).text
}
function statusClass(v) {
  return (STATUS_MAP[v] || { cls: 'gray' }).cls
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, taskName: undefined, processDefinitionKey: undefined, title: undefined, startUserName: undefined }
})
const { queryParams } = toRefs(data)
const dateRange = ref([])

// ===== 列显隐（RightToolbar 持久化） =====
const defaultColumns = {
  title: { label: '流程标题', visible: true },
  startUserName: { label: '创建人', visible: true },
  createTime: { label: '创建时间', visible: true },
  endTime: { label: '办理时间', visible: true },
  taskName: { label: '办理环节', visible: true },
  status: { label: '流程状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_task_columns')
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
    // 根节点"全部流程"，count=各类数量之和
    const total = groups.reduce((s, g) => s + (g.count || 0), 0)
    treeData.value = [{ id: '__all__', label: '全部流程', count: total, children: groups }].filter(g => groups.length || g.children)
    treeLoading.value = false
  }).catch(() => { treeLoading.value = false })
}

function handleNodeClick(node) {
  // 叶子节点=具体流程（按定义Key过滤）；分类节点/根节点不过滤
  queryParams.value.processDefinitionKey = node.children || !node.key ? undefined : node.key
  handleQuery()
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.title) count++
  if (queryParams.value.startUserName) count++
  if (queryParams.value.taskName) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value)
  if (activeTab.value === 'all') {
    // 全部：待办任务 + 草稿（后端合并后统一分页）
    listAllTask(params).then(res => {
      list.value = res.rows || []
      allTotal.value = res.total || 0
      loading.value = false
      applySavedWidths()
    }).catch(() => { loading.value = false })
    return
  }
  if (activeTab.value === 'draft') {
    // 草稿：按标题 + 流程Key 过滤（办理环节不适用）
    listDraft({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      title: params.title,
      processKey: params.processDefinitionKey,
      params: params.params
    }).then(res => {
      list.value = res.rows || []
      draftTotal.value = res.total || 0
      loading.value = false
      applySavedWidths()
    }).catch(() => { loading.value = false })
    return
  }
  if (activeTab.value === 'cc') {
    // 待阅：我收到的抄送（未读在前，含已读历史）
    listMyCc({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      title: params.title,
      processKey: params.processDefinitionKey,
      params: params.params
    }).then(res => {
      list.value = res.rows || []
      ccPageTotal.value = res.total || 0
      loading.value = false
      applySavedWidths()
    }).catch(() => { loading.value = false })
    return
  }
  if (activeTab.value === 'done') {
    // 已办：按状态页签（全部/未结束/已结束）过滤，后端内存分页
    listDoneTask({ ...params, statusScope: doneScope.value }).then(res => {
      list.value = res.rows || []
      doneCounts[doneScope.value] = res.total || 0
      loading.value = false
      applySavedWidths()
    }).catch(() => { loading.value = false })
    return
  }
  // 退回：只看被上游驳回退回给我的待办（后端按 task-local 退回标记过滤）
  listTodoTask({ ...params, returnedOnly: activeTab.value === 'returned' ? true : undefined }).then(res => {
    list.value = res.rows || []
    if (activeTab.value === 'returned') returnedTotal.value = res.total || 0
    else todoTotal.value = res.total || 0
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

/** 待办 / 已办 对应两个菜单路由，全部、草稿复用待办页 + tab 参数 */
const TAB_PATH = { todo: '/flow/todo', done: '/flow/done' }
/** 页签切换（同步到地址栏，刷新后保持当前页签） */
function switchTab(tab) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  queryParams.value.pageNum = 1
  const target = TAB_PATH[tab]
  const current = route.path || ''
  let location
  if (target) {
    location = { path: target, query: {} }
  } else if (current.endsWith('/done')) {
    // 已办菜单页上切到全部/草稿，回到待办页并带上 tab
    location = { path: '/flow/todo', query: { tab } }
  } else {
    location = { path: current, query: { tab } }
  }
  router.replace(location).catch(() => {})
  getList()
}
/** 已办状态页签切换（同步到地址栏 scope，刷新后保持） */
function switchDoneScope(scope) {
  if (doneScope.value === scope) return
  doneScope.value = scope
  queryParams.value.pageNum = 1
  router.replace({ path: route.path, query: scope === 'all' ? {} : { scope } }).catch(() => {})
  getList()
}

/** 菜单切换（待办 / 已办 同一组件，keep-alive 复用实例）时重新推导页签 */
watch(() => [route.path, route.query.tab, route.query.scope], () => {
  const tab = resolveTab()
  const scope = resolveDoneScope()
  if (tab === activeTab.value && scope === doneScope.value) return
  activeTab.value = tab
  doneScope.value = scope
  queryParams.value.pageNum = 1
  getList()
  loadCounts()
})

function loadCounts() {
  // 待办数量拆分：all 全部待办 / returned 被退回（退回页签角标）
  countTodoTasks().then(res => {
    const d = res.data || {}
    todoTotal.value = Number(d.all || 0)
    returnedTotal.value = Number(d.returned || 0)
  }).catch(() => {
    // 接口异常时回退到只取总数，不影响页面可用
    returnedTotal.value = 0
    countTodoTask().then(r => { todoTotal.value = r.data || 0 }).catch(() => { todoTotal.value = 0 })
  })
  getCcUnreadCount().then(res => { ccTotal.value = res.data ? (res.data.count || res.count || 0) : (res.count || 0) }).catch(() => { ccTotal.value = 0 })
  countDoneTask().then(res => {
    const d = res.data || {}
    doneCounts.all = d.all || 0
    doneCounts.running = d.running || 0
    doneCounts.finished = d.finished || 0
  }).catch(() => { doneCounts.all = 0; doneCounts.running = 0; doneCounts.finished = 0 })
  listDraft({ pageNum: 1, pageSize: 1 }).then(res => { draftTotal.value = res.total || 0 }).catch(() => { draftTotal.value = 0 })
  listAllTask({ pageNum: 1, pageSize: 1 }).then(res => { allTotal.value = res.total || 0 }).catch(() => { allTotal.value = 0 })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.taskName = undefined
  queryParams.value.title = undefined
  queryParams.value.startUserName = undefined
  queryParams.value.processDefinitionKey = undefined
  dateRange.value = []
  if (treeRef.value) treeRef.value.setCurrentKey(null)
  handleQuery()
}

/** 点击标题：全部→按行类型分发，待办→办理页，草稿→发起页续填，已办→流程详情 */
function handleTitleClick(row) {
  if (activeTab.value === 'todo' || activeTab.value === 'returned') handleHandle(row)
  else if (activeTab.value === 'draft') handleDraftContinue(row)
  else if (activeTab.value === 'cc') handleCcView(row)
  else if (activeTab.value === 'all') {
    if (row.bizType === 'draft') handleDraftContinue(row)
    else if (row.bizType === 'cc') handleCcView(row)
    else handleHandle(row)
  } else handleView(row)
}

/** 草稿续填：回到发起页并回填草稿内容 */
function handleDraftContinue(row) {
  const href = generateRouteHref({
    path: '/flow/launch/create',
    query: { key: row.processKey || '', draftId: row.draftId || '' }
  })
  window.open(href, '_blank')
}

/** 删除草稿 */
function handleDraftDelete(row) {
  proxy.$modal.confirm('确认删除草稿「' + (row.title || row.processName || '-') + '」？').then(() => {
    return delDraft(row.draftId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadCounts()
  }).catch(() => {})
}

/** 查看待阅流程：未读时先标记已读，再新开流程详情页 */
function handleCcView(row) {
  const openDetail = () => {
    const href = generateRouteHref({
      path: '/flow/detail/create',
      query: { instanceId: row.instanceId || '', defName: row.processName || row.title || '' }
    })
    window.open(href, '_blank')
  }
  if (row.readFlag === '1' || !row.ccId) {
    openDetail()
    return
  }
  readCc(row.ccId).then(() => {
    loadCounts()
    if (activeTab.value === 'cc' || activeTab.value === 'all') getList()
  }).catch(() => {}).finally(openDetail)
}

/** 标记已读 */
function handleCcRead(row) {
  if (!row.ccId) return
  readCc(row.ccId).then(() => {
    proxy.$modal.msgSuccess('已标记为已读')
    if (activeTab.value === 'cc' || activeTab.value === 'all') getList()
    loadCounts()
  }).catch(() => {})
}

/** 办理任务：新标签页打开办理独立页（与流程详情页同一承载口径，不再使用弹框） */
function handleHandle(row) {
  const href = generateRouteHref({
    path: '/flow/task/handle',
    query: {
      taskId: row.taskId || '',
      taskName: row.taskName || '',
      title: row.title || '',
      instanceId: row.instanceId || '',
      defName: row.processDefinitionName || '',
      startUserName: row.startUserName || '',
      createTime: row.createTime || ''
    }
  })
  window.open(href, '_blank')
}

/** 查看流程详情：新标签页打开独立页面（不再使用当前页弹框） */
function handleView(row) {
  const href = generateRouteHref({
    path: '/flow/detail/create',
    query: { instanceId: row.instanceId || '', defName: row.processDefinitionName || row.title || '' }
  })
  window.open(href, '_blank')
}

getList()
loadCounts()
loadTree()
onActivated(() => { getList(); loadCounts(); loadTree() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 左树右列表布局 ===== */
.flowable-task-page {
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
    gap: 0;
  }
  /* 页签 + 工具栏：样式来自 flow-model.scss 的规范状态标签栏（.status-tabs） */
  /* 紧凑筛选区（对齐车间产线） */
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
    .tree-search {
      padding: 0 2px 8px;
    }
    .tree-body {
      max-height: calc(100vh - 300px);
      overflow: auto;
    }
    .tree-node {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      overflow: hidden;
      .tree-icon {
        color: var(--el-color-primary);
        flex-shrink: 0;
      }
      .tree-icon.leaf {
        color: #10b981;
      }
      .tree-label {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .tree-count {
        margin-left: auto;
        flex-shrink: 0;
        font-size: 12px;
        line-height: 1;
        padding: 3px 7px;
        border-radius: 9px;
        background: var(--el-color-primary-light-9, #ecf5ff);
        color: var(--el-color-primary);
        &.is-zero {
          background: #f3f4f6;
          color: #9ca3af;
        }
      }
    }
    .tree-empty {
      text-align: center;
      color: var(--rd-muted, #9ca3af);
      font-size: 13px;
      padding: 24px 0;
    }
    :deep(.el-tree-node__content) {
      height: 32px;
      border-radius: 8px;
    }
  }
  .task-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  /* ===== 流程详情页签 ===== */
  .detail-tabs {
    display: flex;
    gap: 4px;
    padding: 0 20px;
    border-bottom: 1px solid var(--rd-border, #e5e7eb);
    margin-bottom: 12px;
    .detail-tab {
      background: none;
      border: none;
      padding: 10px 16px;
      font-size: 14px;
      color: var(--rd-muted, #6b7280);
      cursor: pointer;
      border-bottom: 2px solid transparent;
      transition: color .15s;
      &:hover { color: var(--el-color-primary); }
      &.is-active {
        color: var(--el-color-primary);
        font-weight: 600;
        border-bottom-color: var(--el-color-primary);
      }
    }
  }
  .status-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    margin-bottom: 12px;
    .status-card {
      background: #f8fafc;
      border: 1px solid var(--rd-border, #e5e7eb);
      border-radius: 10px;
      text-align: center;
      padding: 12px 0;
      .sc-num { font-size: 22px; font-weight: 700; color: var(--ink-900, #0f172a); }
      .sc-num.sc-green { color: #10b981; }
      .sc-num.sc-amber { color: #f59e0b; }
      .sc-label { font-size: 12px; color: var(--rd-muted, #6b7280); margin-top: 2px; }
    }
  }
  .resource-empty {
    text-align: center;
    color: var(--rd-muted, #9ca3af);
    font-size: 13px;
    padding: 14px 0 4px;
  }
  .badge.amber {
    background: #fef3c7;
    color: #b45309;
  }
}
</style>
