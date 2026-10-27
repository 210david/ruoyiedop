<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="bid-project-picker">
      <div class="bid-project-picker-search">
        <el-input v-model="queryParams.bidNo" placeholder="投标编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery" />
        <el-input v-model="queryParams.projectName" placeholder="项目名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <div class="bid-project-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="360"
          size="small"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.projectId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="投标编号" prop="bidNo" width="150" show-overflow-tooltip />
          <el-table-column label="项目名称" prop="projectName" min-width="220" show-overflow-tooltip />
          <el-table-column label="招标人" prop="tenderee" min-width="140" show-overflow-tooltip />
          <el-table-column label="递交截止" prop="submitDeadline" width="110" align="center">
            <template #default="scope">{{ (scope.row.submitDeadline || '—').slice(0, 10) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <div class="bid-project-picker-pager">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @current-change="paginate"
        />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="BidProjectPicker">
import { listProject } from '@/api/bid/project'

const emit = defineEmits(['confirm'])

const props = defineProps({
  title: { type: String, default: '选择投标项目' }
})

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const allList = ref([])
const total = ref(0)
const selectedId = ref(null)
const selectedRow = ref(null)
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  bidNo: undefined,
  projectName: undefined
})

function onOpen() {
  queryParams.bidNo = undefined
  queryParams.projectName = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

function getList() {
  loading.value = true
  listProject({ pageNum: 1, pageSize: 999, bidNo: queryParams.bidNo, projectName: queryParams.projectName }).then(res => {
    allList.value = res.rows || []
    total.value = allList.value.length
    paginate()
    loading.value = false
  })
}

function paginate() {
  const start = (queryParams.pageNum - 1) * queryParams.pageSize
  list.value = allList.value.slice(start, start + queryParams.pageSize)
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.bidNo = undefined
  queryParams.projectName = undefined
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
  if (!selectedRow.value) return
  visible.value = false
  emit('confirm', selectedRow.value)
}

/** 对外：打开弹窗，selectedId 可传入当前已选项目 ID 用于回显 */
function open(selectedIdVal) {
  visible.value = true
  if (selectedIdVal != null) {
    selectedId.value = selectedIdVal
    nextTick(() => {
      if (list.value.length) {
        const row = list.value.find(r => r.projectId === selectedIdVal)
        if (row) { selectedRow.value = row; tableRef.value && tableRef.value.setCurrentRow(row) }
      }
    })
  }
}

defineExpose({ open })
</script>

<style scoped>
.bid-project-picker-search { display: flex; align-items: center; margin-bottom: 10px; }
.bid-project-picker-hint { margin-bottom: 8px; font-size: 12px; color: #909399; }
.bid-project-picker-pager { display: flex; justify-content: flex-end; margin-top: 10px; }
</style>
