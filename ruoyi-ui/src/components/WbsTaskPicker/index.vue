<template>
  <el-dialog v-model="visible" width="880px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 11l3 3L22 4"/>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
        <template v-if="projectLabel">
          <span class="rd-detail-header-divider"></span>
          <span class="rd-detail-header-no">{{ projectLabel }}</span>
        </template>
      </div>
    </template>
    <div class="wbs-task-picker">
      <div class="wbs-task-picker-search">
        <el-input v-model="queryParams.taskName" placeholder="任务名称" clearable size="small" style="width: 220px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="queryParams.status" placeholder="任务状态" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="list"
        highlight-current-row
        height="380"
        size="small"
        @row-click="onRowClick"
        @row-dblclick="onRowDblClick"
      >
        <el-table-column width="45" align="center">
          <template #default="{ row }">
            <el-radio :model-value="selectedId" :value="row.taskId" @click.stop="onRowClick(row)"><span /></el-radio>
          </template>
        </el-table-column>
        <el-table-column label="任务编码" prop="taskCode" width="150" class-name="col-mono" show-overflow-tooltip />
        <el-table-column label="任务名称" prop="taskName" min-width="220" show-overflow-tooltip />
        <el-table-column label="负责人" prop="ownerName" width="110" show-overflow-tooltip />
        <el-table-column label="任务状态" width="100" align="center">
          <template #default="{ row }">
            <span class="badge" :class="statusBadge(row.status)">{{ statusMap[row.status] || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计划工时" prop="planHours" width="90" align="center">
          <template #default="{ row }">{{ row.planHours != null ? row.planHours + 'H' : '-' }}</template>
        </el-table-column>
        <el-table-column label="实际工时" prop="actualHours" width="90" align="center">
          <template #default="{ row }">{{ row.actualHours != null ? row.actualHours + 'H' : '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="wbs-task-picker-pager">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @current-change="getList"
        />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" :disabled="!selectedId" @click="handleConfirm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { listTask } from '@/api/pjm/task'

const props = defineProps({
  title: { type: String, default: '选择WBS任务' }
})
const emit = defineEmits(['confirm'])

const statusMap = { '0': '未开始', '1': '进行中', '2': '完成', '3': '取消', '4': '延期' }
const statusBadge = (v) => ({ '0': 'gray', '1': 'blue', '2': 'green', '3': 'gray', '4': 'amber' }[v] || 'gray')

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedId = ref(null)
const selectedRow = ref(null)
const tableRef = ref()
const projectId = ref(null)
const projectLabel = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  taskName: undefined,
  status: undefined
})

function onOpen() {
  queryParams.taskName = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  selectedRow.value = null
  // 弹框每次打开都按当前项目重新取数；未指定项目时按空结果处理，避免误查全量任务
  if (!projectId.value) { list.value = []; total.value = 0; return }
  getList()
}

function getList() {
  if (!projectId.value) { list.value = []; total.value = 0; return }
  loading.value = true
  listTask({ ...queryParams, projectId: projectId.value }).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.taskName = undefined
  queryParams.status = undefined
  handleQuery()
}

function onRowClick(row) {
  selectedId.value = row.taskId
  selectedRow.value = row
}

function onRowDblClick(row) {
  selectedId.value = row.taskId
  selectedRow.value = row
  handleConfirm()
}

function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', { ...selectedRow.value })
  visible.value = false
}

/**
 * 打开任务选择弹框
 * @param {Object} options { projectId, taskId, projectLabel }
 */
function open(options = {}) {
  projectId.value = options.projectId || null
  projectLabel.value = options.projectLabel || ''
  selectedId.value = options.taskId || null
  selectedRow.value = null
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.wbs-task-picker-search { display: flex; align-items: center; margin-bottom: 12px; }
.wbs-task-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }
.wbs-task-picker :deep(.el-table__row) { cursor: pointer; }
.wbs-task-picker :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.wbs-task-picker :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
</style>
