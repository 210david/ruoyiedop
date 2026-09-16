<template>
  <div class="app-container flowable-task-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>任务名称</label>
          <div class="control">
            <el-input v-model="queryParams.taskName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程名称</label>
          <div class="control">
            <el-input v-model="queryParams.processDefinitionName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
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
      <!-- 待办/已办 Tab -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'todo' }" @click="switchTab('todo')">
            <span class="dot dot-blue"></span><span>我的待办</span><span class="count count-blue">{{ todoTotal }}</span>
          </button>
          <button class="status-tab" :class="{ 'is-active': activeTab === 'done' }" @click="switchTab('done')">
            <span class="dot dot-green"></span><span>我的已办</span><span class="count count-green">{{ doneTotal }}</span>
          </button>
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table" :key="activeTab">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="任务名称" prop="taskName" min-width="150" show-overflow-tooltip>
            <template #default="scope"><span class="col-strong">{{ scope.row.taskName || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="流程名称" prop="processDefinitionName" min-width="140" show-overflow-tooltip />
          <el-table-column label="业务单号" prop="businessKey" min-width="130" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.businessKey || '-' }}</span></template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done'" label="审批意见" prop="comment" min-width="160" show-overflow-tooltip />
          <el-table-column v-if="activeTab === 'done'" label="审批结果" prop="outcome" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="outcomeClass(scope.row.outcome)">
                <span class="dot"></span>{{ outcomeLabel(scope.row.outcome) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab === 'done'" label="完成时间" prop="endTime" width="170" align="center" />
          <el-table-column v-else label="到达时间" prop="createTime" width="170" align="center" />
          <el-table-column label="操作" width="200" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <template v-if="activeTab === 'todo'">
                  <el-button link type="success" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['flowable:task:complete']">审批</el-button>
                  <el-button link type="primary" icon="View" @click="handleView(scope.row)">流程</el-button>
                </template>
                <template v-else>
                  <el-button link type="primary" icon="View" @click="handleView(scope.row)">流程</el-button>
                </template>
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

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="approveOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          </div>
          <span class="rd-detail-header-title">任务审批</span>
          <div class="rd-detail-header-sub" v-if="approveTask.taskName">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ approveTask.taskName }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">i</span>任务信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">任务名称</span><div class="rd-value">{{ approveTask.taskName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">流程名称</span><div class="rd-value">{{ approveTask.processDefinitionName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">业务单号</span><div class="rd-value">{{ approveTask.businessKey || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">到达时间</span><div class="rd-value">{{ approveTask.createTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">✎</span>审批意见</div>
          </div>
          <div class="rd-card-body">
            <el-input v-model="approveForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitApprove('approved')">同 意</el-button>
        <el-button type="danger" @click="submitApprove('rejected')">驳 回</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

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
            <span class="rd-detail-header-no">{{ detailData.processDefinitionName }}（{{ detailData.instanceId }}）</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 流程图（后端高亮生成） -->
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
        <!-- 流转记录时间线 -->
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
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">状态</span>
                      <div class="rd-value">
                        <span v-if="t.endTime" class="badge" :class="outcomeClass(t.outcome)"><span class="dot"></span>{{ outcomeLabel(t.outcome) || '已办理' }}</span>
                        <span v-else class="badge blue"><span class="dot"></span>待办理</span>
                      </div>
                    </div>
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

<script setup name="FlowTask">
import { listTodoTask, listDoneTask, countTodoTask, completeTask } from '@/api/flowable/flow'
import request from '@/utils/request'
import { Search, Filter, Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const activeTab = ref('todo')
const todoTotal = ref(0)
const doneTotal = ref(0)
const approveOpen = ref(false)
const approveTask = ref({})
const approveForm = ref({ comment: '' })
const detailOpen = ref(false)
const detailData = ref({})
const historyTasks = ref([])
const diagramUrl = ref('')

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, taskName: undefined, processDefinitionName: undefined }
})
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.taskName) count++
  if (queryParams.value.processDefinitionName) count++
  return count
})

function switchTab(tab) {
  activeTab.value = tab
  queryParams.value.pageNum = 1
  getList()
}

function getList() {
  loading.value = true
  const api = activeTab.value === 'todo' ? listTodoTask : listDoneTask
  api(queryParams.value).then(res => {
    list.value = res.rows || []
    if (activeTab.value === 'todo') todoTotal.value = res.total; else doneTotal.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function loadCounts() {
  countTodoTask().then(res => { todoTotal.value = res.data || 0 }).catch(() => {})
  listDoneTask({ pageNum: 1, pageSize: 1 }).then(res => { doneTotal.value = res.total || 0 }).catch(() => {})
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.taskName = undefined
  queryParams.value.processDefinitionName = undefined
  handleQuery()
}

function handleApprove(row) {
  approveTask.value = row
  approveForm.value.comment = ''
  approveOpen.value = true
}

function submitApprove(outcome) {
  proxy.$modal.confirm(outcome === 'approved' ? '确认同意该任务？' : '确认驳回该任务？').then(() => {
    return completeTask({ taskId: approveTask.value.taskId, comment: approveForm.value.comment, outcome })
  }).then(() => {
    proxy.$modal.msgSuccess(outcome === 'approved' ? '已同意' : '已驳回')
    approveOpen.value = false
    getList()
    loadCounts()
  }).catch(() => {})
}

async function handleView(row) {
  detailData.value = {}
  historyTasks.value = []
  diagramUrl.value = ''
  detailOpen.value = true
  // 加载实例详情
  request({ url: '/flowable/process/' + row.instanceId, method: 'get' }).then(res => {
    detailData.value = res.data || {}
  })
  // 流转记录
  request({ url: '/flowable/process/history/' + row.instanceId, method: 'get' }).then(res => {
    historyTasks.value = res.data || []
  })
  // 流程图（需要认证头，转 blob）
  request({ url: '/flowable/process/diagram/' + row.instanceId, method: 'get', responseType: 'blob' }).then(res => {
    const blob = new Blob([res], { type: 'image/png' })
    diagramUrl.value = URL.createObjectURL(blob)
  }).catch(() => {})
}

function outcomeClass(outcome) {
  const map = { approved: 'green', rejected: 'red', delegate: 'blue' }
  return map[outcome] || 'gray'
}
function outcomeLabel(outcome) {
  const map = { approved: '同意', rejected: '驳回', delegate: '转办' }
  return map[outcome] || ''
}

getList()
loadCounts()
onActivated(() => { getList(); loadCounts() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';
</style>
