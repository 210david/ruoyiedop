<template>
  <div class="app-container flowable-process-page">
    <div class="task-layout">
      <!-- ===== 左侧：流程分类树（对齐待办流程页） ===== -->
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

      <!-- ===== 右侧：我发起的流程列表 ===== -->
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
          <label>流程名称</label>
          <div class="control">
            <el-input v-model="queryParams.processDefinitionName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>创建日期</label>
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
      <!-- 状态标签栏（规范：圆点 + 名称 + 数量徽章） -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab tab-audit" :class="{ 'is-active': activeStatusTab === 'running' }" @click="handleStatusTabClick('running')">
            <span class="dot"></span><span>进行中</span><span class="count">{{ statusCounts.running }}</span>
          </button>
          <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === 'completed' }" @click="handleStatusTabClick('completed')">
            <span class="dot"></span><span>已完成</span><span class="count">{{ statusCounts.completed }}</span>
          </button>
          <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === 'canceled' }" @click="handleStatusTabClick('canceled')">
            <span class="dot"></span><span>已撤销</span><span class="count">{{ statusCounts.canceled }}</span>
          </button>
        </div>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_process_columns" />
      </div>
      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" :width="colWidth('序号', 85)" align="center" />
          <el-table-column v-if="columns.title.visible" label="流程标题" prop="title" key="title" :min-width="colWidth('title', 220)" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="urgencyBadge(scope.row.urgency)" class="badge" :class="urgencyBadge(scope.row.urgency).cls">{{ urgencyBadge(scope.row.urgency).text }}</span>
              <span class="col-strong link-like" @click="handleView(scope.row)">{{ scope.row.title || scope.row.processDefinitionName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.processDefinitionName.visible" label="流程名称" prop="processDefinitionName" key="processDefinitionName" :min-width="colWidth('processDefinitionName', 150)" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.processDefinitionName || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="columns.startTime.visible" label="创建日期" prop="startTime" key="startTime" :width="colWidth('startTime', 170)" align="center" />
          <el-table-column v-if="columns.currentActivityName.visible" label="当前节点" prop="currentActivityName" key="currentActivityName" :min-width="colWidth('currentActivityName', 130)" show-overflow-tooltip>
            <template #default="scope">
              <span class="badge" :class="scope.row.status === 'running' ? 'blue' : 'gray'"><span class="dot"></span>{{ scope.row.currentActivityName || (scope.row.status === 'completed' ? '流程结束' : '已终止') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="danger" icon="CircleClose" @click="handleCancel(scope.row)" v-if="scope.row.status === 'running'" v-hasPermi="['flowable:process:cancel']">撤销</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>
      </div>
    </div>

  </div>
</template>

<script setup name="FlowProcess">
import { listMyProcess, cancelProcess, getProcessTree } from '@/api/flowable/flow'
import FlowDetail from '@/components/FlowDetail/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useRouter } from 'vue-router'
import { Search, Filter, Document, Guide, Folder, Fold, Expand } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_process_index')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, running: 0, completed: 0, canceled: 0 })
/** 紧急程度徽标：1普通（不显示）/ 2紧急 / 3特急 */
function urgencyBadge(v) {
  if (v === '3' || v === 3) return { text: '特急', cls: 'red' }
  if (v === '2' || v === 2) return { text: '紧急', cls: 'amber' }
  return null
}

const detailOpen = ref(false)
const detailInstanceId = ref('')
const detailStatus = ref('')
const dateRange = ref([])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, processDefinitionName: undefined, currentActivityName: undefined, processDefinitionKey: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

// ===== 左侧流程树（对齐待办流程页） =====
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
    const totalTree = groups.reduce((s, g) => s + (g.count || 0), 0)
    treeData.value = [{ id: '__all__', label: '全部流程', count: totalTree, children: groups }].filter(g => groups.length || g.children)
    treeLoading.value = false
  }).catch(() => { treeLoading.value = false })
}

function handleNodeClick(node) {
  // 叶子节点=具体流程（按定义Key过滤）；分类节点/根节点不过滤
  queryParams.value.processDefinitionKey = node.children || !node.key ? undefined : node.key
  handleQuery()
}

// ===== 列显隐（RightToolbar 持久化） =====
const defaultColumns = {
  title: { label: '流程标题', visible: true },
  processDefinitionName: { label: '流程名称', visible: true },
  startTime: { label: '创建日期', visible: true },
  currentActivityName: { label: '当前节点', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_process_columns')
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
  if (queryParams.value.currentActivityName) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  const params = proxy.addDateRange(queryParams.value, dateRange.value)
  listMyProcess(params).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  listMyProcess({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, running: 0, completed: 0, canceled: 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => { statusCounts.value = { all: 0, running: 0, completed: 0, canceled: 0 } })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.title = undefined
  queryParams.value.processDefinitionName = undefined
  queryParams.value.currentActivityName = undefined
  queryParams.value.processDefinitionKey = undefined
  queryParams.value.status = undefined
  dateRange.value = []
  activeStatusTab.value = 'all'
  if (treeRef.value) treeRef.value.setCurrentKey(null)
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.status = status === 'all' ? undefined : status
  handleQuery()
}

function statusBadge(status) {
  const map = { running: 'blue', completed: 'green', canceled: 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const map = { running: '进行中', completed: '已完成', canceled: '已撤销' }
  return map[status] || status || '-'
}

/** 查看流程详情：新标签页打开独立页面（不再使用当前页弹框） */
function handleView(row) {
  const href = router.resolve({
    path: '/flow/detail/create',
    query: { instanceId: row.instanceId || '', defName: row.processDefinitionName || row.title || '' }
  }).href
  window.open(href, '_blank')
}

function handleCancel(row) {
  proxy.$modal.confirm('确认撤销该流程？撤销后流程终止且不可恢复').then(() => {
    return proxy.$prompt('请输入撤销原因（可选）', '撤销流程', { confirmButtonText: '确定', cancelButtonText: '取消', inputType: 'textarea' })
  }).then(({ value }) => {
    return cancelProcess(row.instanceId, value)
  }).then(() => {
    proxy.$modal.msgSuccess('流程已撤销')
    getList()
    loadStatusCounts()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('撤销失败：' + (e?.message || e)) })
}

getList()
loadStatusCounts()
loadTree()
onActivated(() => { getList(); loadStatusCounts(); loadTree() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 左树右列表布局（对齐待办流程页） ===== */
.flowable-process-page {
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
  /* 紧凑筛选区 */
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
  .link-like {
    cursor: pointer;
    &:hover { color: var(--el-color-primary); }
  }
}
</style>
