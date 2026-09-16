<template>
  <div class="app-container flowable-process-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>流程名称</label>
          <div class="control">
            <el-input v-model="queryParams.processDefinitionName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程Key</label>
          <div class="control">
            <el-input v-model="queryParams.processDefinitionKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="进行中" value="running" />
              <el-option label="已完成" value="completed" />
              <el-option label="已撤销" value="canceled" />
            </el-select>
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
      <!-- 状态标签栏 -->
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

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="流程名称" prop="processDefinitionName" min-width="150" show-overflow-tooltip>
            <template #default="scope"><span class="col-strong">{{ scope.row.processDefinitionName || scope.row.processDefinitionKey }}</span></template>
          </el-table-column>
          <el-table-column label="业务单号" prop="businessKey" min-width="130" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.businessKey || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="流程版本" prop="processDefinitionVersion" width="90" align="center">
            <template #default="scope">v{{ scope.row.processDefinitionVersion }}</template>
          </el-table-column>
          <el-table-column label="当前节点" prop="currentActivityName" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.currentActivityName || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="发起时间" prop="startTime" width="170" align="center" />
          <el-table-column label="结束时间" prop="endTime" width="170" align="center">
            <template #default="scope">{{ scope.row.endTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right" class-name="col-action">
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

    <!-- ===== 流程详情弹窗（含流程图 + 流转记录） ===== -->
    <el-dialog v-model="detailOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </div>
          <span class="rd-detail-header-title">流程详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.processDefinitionName">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ detailData.processDefinitionName }}</span>
            <span class="badge" :class="statusBadge(detailData.status)"><span class="dot"></span>{{ statusLabel(detailData.status) }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">i</span>基本信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">流程实例ID</span><div class="rd-value">{{ detailData.instanceId || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">流程名称</span><div class="rd-value">{{ detailData.processDefinitionName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">业务单号</span><div class="rd-value">{{ detailData.businessKey || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">当前节点</span><div class="rd-value">{{ detailData.currentActivityName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发起时间</span><div class="rd-value">{{ detailData.startTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ detailData.endTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">◈</span>流程图</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="diagram-wrap">
              <img v-if="diagramUrl" :src="diagramUrl" class="diagram-img" alt="流程图" />
              <div v-else class="diagram-empty">流程已结束或暂无流程图</div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">≡</span>流转记录</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="t in historyTasks" :key="t.taskId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': t.endTime, 'rd-timeline-dot--active': !t.endTime }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ t.taskName }}</span>
                    <span class="rd-timeline-time">{{ t.createTime }}</span>
                  </div>
                  <div class="rd-timeline-comment" v-if="t.comment"><strong>审批意见：</strong>{{ t.comment }}</div>
                </div>
              </div>
              <div class="rd-empty" v-if="!historyTasks.length">
                <p class="rd-empty-text">暂无流转记录</p>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowProcess">
import { listMyProcess, cancelProcess } from '@/api/flowable/flow'
import request from '@/utils/request'
import { Search, Filter, Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, running: 0, completed: 0, canceled: 0 })
const detailOpen = ref(false)
const detailData = ref({})
const historyTasks = ref([])
const diagramUrl = ref('')

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, processDefinitionName: undefined, processDefinitionKey: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.processDefinitionName) count++
  if (queryParams.value.processDefinitionKey) count++
  if (queryParams.value.status) count++
  return count
})

function getList() {
  loading.value = true
  listMyProcess(queryParams.value).then(res => {
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
  queryParams.value.processDefinitionName = undefined
  queryParams.value.processDefinitionKey = undefined
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
  detailData.value = {}
  historyTasks.value = []
  diagramUrl.value = ''
  detailOpen.value = true
  request({ url: '/flowable/process/' + row.instanceId, method: 'get' }).then(res => {
    detailData.value = res.data || {}
  })
  request({ url: '/flowable/process/history/' + row.instanceId, method: 'get' }).then(res => {
    historyTasks.value = res.data || []
  })
  request({ url: '/flowable/process/diagram/' + row.instanceId, method: 'get', responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'image/png' })
    diagramUrl.value = URL.createObjectURL(blob)
  }).catch(() => {})
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
</style>
