<template>
  <div class="app-container flowable-search-page">
    <!-- 筛选 -->
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
              <el-option label="已结束" value="completed" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 查询范围：我发起的 + 我审批过的全部流程</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 列表 -->
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
        </div>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="流程名称" prop="processDefinitionName" min-width="150" show-overflow-tooltip>
            <template #default="scope"><span class="col-strong">{{ scope.row.processDefinitionName || scope.row.processDefinitionKey }}</span></template>
          </el-table-column>
          <el-table-column label="业务单号" prop="businessKey" min-width="120" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.businessKey || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="发起人" width="110" align="center">
            <template #default="scope">{{ scope.row.startUserName || '-' }}</template>
          </el-table-column>
          <el-table-column label="当前节点" prop="currentActivityName" min-width="110" show-overflow-tooltip>
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
          <el-table-column label="操作" width="100" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
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

    <!-- 流程详情弹窗（四页签共享组件） -->
    <flow-detail v-model:open="detailOpen" :instance-id="detailInstanceId">
      <template #header-extra>
        <span v-if="detailStatus" class="badge" :class="statusBadge(detailStatus)"><span class="dot"></span>{{ statusLabel(detailStatus) }}</span>
      </template>
    </flow-detail>

  </div>
</template>

<script setup name="FlowSearch">
import request from '@/utils/request'
import FlowDetail from '@/components/FlowDetail/index.vue'
import { Search, Filter, Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, running: 0, completed: 0 })
const detailOpen = ref(false)
const detailInstanceId = ref('')
const detailStatus = ref('')

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, processDefinitionName: undefined, processDefinitionKey: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  request({ url: '/flowable/process/involved', method: 'get', params: queryParams.value }).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  request({ url: '/flowable/process/involved', method: 'get', params: { pageNum: 1, pageSize: 999 } }).then(res => {
    const counts = { all: res.total, running: 0, completed: 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.processDefinitionName = undefined
  queryParams.value.processDefinitionKey = undefined
  activeStatusTab.value = 'all'; queryParams.value.status = undefined
  handleQuery()
}
function handleStatusTabClick(s) {
  activeStatusTab.value = s
  queryParams.value.status = s === 'all' ? undefined : (s === 'completed' ? 'completed' : s)
  handleQuery()
}
function statusBadge(s) { return { running: 'blue', completed: 'green', canceled: 'gray' }[s] || 'gray' }
function statusLabel(s) { return { running: '进行中', completed: '已结束', canceled: '已撤销' }[s] || s || '-' }

async function handleView(row) {
  detailInstanceId.value = row.instanceId
  detailStatus.value = row.status || ''
  detailOpen.value = true
}

getList()
loadStatusCounts()
onActivated(() => { getList(); loadStatusCounts() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';
</style>
