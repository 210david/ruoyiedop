<template>
  <el-dialog v-model="visible" width="880px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="project-picker">
      <div class="project-picker-search">
        <el-input v-model="queryParams.projectNo" placeholder="项目编号" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.projectName" placeholder="项目名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.status" placeholder="项目状态" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
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
            <el-radio :model-value="selectedId" :value="row.projectId" @click.stop="onRowClick(row)"><span /></el-radio>
          </template>
        </el-table-column>
        <el-table-column label="项目编号" prop="projectNo" width="150" class-name="col-mono" show-overflow-tooltip />
        <el-table-column label="项目名称" prop="projectName" min-width="220" show-overflow-tooltip />
        <el-table-column label="项目经理" prop="managerName" width="110" show-overflow-tooltip />
        <el-table-column label="项目状态" width="100" align="center">
          <template #default="{ row }">
            <span class="badge" :class="statusBadge(row.status)">{{ statusMap[row.status] || row.status || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计划开始" prop="planStartDate" width="110" align="center" />
      </el-table>

      <div class="project-picker-pager">
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
import { listProject } from '@/api/pjm/project'

const props = defineProps({
  title: { type: String, default: '选择项目' }
})
const emit = defineEmits(['confirm'])

const statusMap = { '0': '草稿', '1': '待审批', '2': '审批通过', '3': '执行中', '4': '已完成', '5': '已关闭', '6': '暂停', '7': '驳回' }
const statusBadge = (v) => ({ '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'green', '5': 'gray', '6': 'amber', '7': 'red' }[v] || 'gray')

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedId = ref(null)
const selectedRow = ref(null)
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  projectNo: undefined,
  projectName: undefined,
  status: undefined
})

function onOpen() {
  queryParams.projectNo = undefined
  queryParams.projectName = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

function getList() {
  loading.value = true
  listProject(queryParams).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.projectNo = undefined
  queryParams.projectName = undefined
  queryParams.status = undefined
  handleQuery()
}

function onRowClick(row) {
  selectedId.value = row.projectId
  selectedRow.value = row
}

function onRowDblClick(row) {
  selectedId.value = row.projectId
  selectedRow.value = row
  handleConfirm()
}

function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', { ...selectedRow.value })
  visible.value = false
}

function open(projectId) {
  visible.value = true
  if (projectId) selectedId.value = projectId
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.project-picker-search { display: flex; align-items: center; margin-bottom: 12px; }
.project-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }
.project-picker :deep(.el-table__row) { cursor: pointer; }
.project-picker :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.project-picker :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
</style>
