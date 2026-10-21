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
        <div class="field">
          <label>创建人</label>
          <div class="control">
            <el-input v-model="queryParams.startUserName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
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
      <div class="list-head">
        <div class="filter-title"><span class="glyph"></span> {{ activeTab === 'todo' ? '待办列表' : '已办列表' }}</div>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_task_columns" />
      </div>
      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" :key="activeTab" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" :width="colWidth('序号', 85)" align="center" />
          <el-table-column v-if="columns.title.visible" label="流程标题" prop="title" key="title" :min-width="colWidth('title', 220)" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="urgencyBadge(scope.row.urgency)" class="badge" :class="urgencyBadge(scope.row.urgency).cls">{{ urgencyBadge(scope.row.urgency).text }}</span>
              <span class="col-strong link-like" @click="handleTitleClick(scope.row)">{{ scope.row.title || scope.row.processDefinitionName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.startUserName.visible" label="创建人" prop="startUserName" key="startUserName" :width="colWidth('startUserName', 120)" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.startUserName || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done' && columns.endTime.visible" label="完成时间" prop="endTime" key="endTime" :width="colWidth('endTime', 170)" align="center" />
          <el-table-column v-else-if="columns.createTime.visible" label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 170)" align="center" />
          <el-table-column v-if="activeTab === 'todo' && columns.taskName.visible" label="办理环节" prop="taskName" key="taskName" :min-width="colWidth('taskName', 130)" show-overflow-tooltip>
            <template #default="scope">
              <span class="badge blue"><span class="dot"></span>{{ scope.row.taskName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done' && columns.comment.visible" label="审批意见" prop="comment" key="comment" :min-width="colWidth('comment', 160)" show-overflow-tooltip />
          <el-table-column v-if="activeTab === 'done' && columns.outcome.visible" label="审批结果" prop="outcome" key="outcome" :width="colWidth('outcome', 100)" align="center">
            <template #default="scope">
              <span class="badge" :class="outcomeClass(scope.row.outcome)">
                <span class="dot"></span>{{ outcomeLabel(scope.row.outcome) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right" class-name="col-action">
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
        <el-pagination background layout="total, prev, pager, next, sizes" :total="activeTab === 'todo' ? todoTotal : doneTotal"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>
      </div>
    </div>
  </div>
</template>

<script setup name="FlowTask">
import { listTodoTask, listDoneTask, countTodoTask, getProcessTree } from '@/api/flowable/flow'
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
const activeTab = ref(route.query.tab === 'done' ? 'done' : 'todo')
const todoTotal = ref(0)
const doneTotal = ref(0)
/** 紧急程度徽标：1普通（不显示）/ 2紧急 / 3特急 */
function urgencyBadge(v) {
  if (v === '3' || v === 3) return { text: '特急', cls: 'red' }
  if (v === '2' || v === 2) return { text: '紧急', cls: 'amber' }
  return null
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
  endTime: { label: '完成时间', visible: true },
  taskName: { label: '办理环节', visible: true },
  comment: { label: '审批意见', visible: true },
  outcome: { label: '审批结果', visible: true }
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
  const api = activeTab.value === 'todo' ? listTodoTask : listDoneTask
  api(params).then(res => {
    list.value = res.rows || []
    if (activeTab.value === 'todo') todoTotal.value = res.total; else doneTotal.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function loadCounts() {
  countTodoTask().then(res => { todoTotal.value = res.data || 0 }).catch(() => { todoTotal.value = 0 })
  listDoneTask({ pageNum: 1, pageSize: 1 }).then(res => { doneTotal.value = res.total || 0 }).catch(() => { doneTotal.value = 0 })
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

/** 点击标题：待办进入任务办理页（优先），已办进入流程详情 */
function handleTitleClick(row) {
  if (activeTab.value === 'todo') handleHandle(row)
  else handleView(row)
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
  /* 列表卡片头部（标题 + 工具栏） */
  .list-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px 0;
    .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink-700); }
  }
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
  .link-like {
    cursor: pointer;
    &:hover { color: var(--el-color-primary); }
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
