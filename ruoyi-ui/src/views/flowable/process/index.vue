<template>
  <div class="app-container flowable-process-page">
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
      <div class="list-head">
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
        </div>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_process_columns" />
      </div>
      <!-- 表格 -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column v-if="columns.title.visible" label="流程标题" prop="title" key="title" min-width="220" show-overflow-tooltip>
            <template #default="scope">
              <span class="col-strong link-like" @click="handleView(scope.row)">{{ scope.row.title || scope.row.processDefinitionName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.processDefinitionName.visible" label="流程名称" prop="processDefinitionName" key="processDefinitionName" min-width="150" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.processDefinitionName || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="columns.startTime.visible" label="创建日期" prop="startTime" key="startTime" width="170" align="center" />
          <el-table-column v-if="columns.currentActivityName.visible" label="当前节点" prop="currentActivityName" key="currentActivityName" min-width="130" show-overflow-tooltip>
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

    <!-- ===== 流程详情弹窗（四页签共享组件） ===== -->
    <flow-detail v-model:open="detailOpen" :instance-id="detailInstanceId">
      <template #header-extra>
        <span v-if="detailStatus" class="badge" :class="statusBadge(detailStatus)"><span class="dot"></span>{{ statusLabel(detailStatus) }}</span>
      </template>
    </flow-detail>

  </div>
</template>

<script setup name="FlowProcess">
import { listMyProcess, cancelProcess } from '@/api/flowable/flow'
import FlowDetail from '@/components/FlowDetail/index.vue'
import { Search, Filter, Document, Guide } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, running: 0, completed: 0, canceled: 0 })
const detailOpen = ref(false)
const detailInstanceId = ref('')
const detailStatus = ref('')
const dateRange = ref([])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, processDefinitionName: undefined, currentActivityName: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

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
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  listMyProcess({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, running: 0, completed: 0, canceled: 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.title = undefined
  queryParams.value.processDefinitionName = undefined
  queryParams.value.currentActivityName = undefined
  queryParams.value.status = undefined
  dateRange.value = []
  activeStatusTab.value = 'all'
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

async function handleView(row) {
  detailInstanceId.value = row.instanceId
  detailStatus.value = row.status || ''
  detailOpen.value = true
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
  }).catch(() => {})
}

getList()
loadStatusCounts()
onActivated(() => { getList(); loadStatusCounts() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 紧凑布局（对齐待办流程页/车间产线） ===== */
.flowable-process-page {
  /* 列表卡片头部（状态页签 + 工具栏） */
  .list-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px 0;
    .status-tabs { flex: 1; min-width: 0; }
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
  .link-like {
    cursor: pointer;
    &:hover { color: var(--el-color-primary); }
  }
}
</style>
