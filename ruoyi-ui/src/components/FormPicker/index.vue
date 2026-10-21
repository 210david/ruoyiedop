<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="form-picker">
      <!-- 搜索栏 -->
      <div class="form-picker-search">
        <el-input v-model="queryParams.formName" placeholder="表单名称" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.tableName" placeholder="数据表名" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 表单表格 -->
      <div class="form-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.formId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="表单名称" prop="formName" min-width="180" show-overflow-tooltip />
          <el-table-column label="表单描述" prop="formDesc" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ row.formDesc || '—' }}</template>
          </el-table-column>
          <el-table-column label="数据表名" prop="tableName" width="170" show-overflow-tooltip>
            <template #default="{ row }"><span class="col-mono">{{ row.tableName || '—' }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="form-picker-pager">
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
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listForm } from '@/api/flowable/form'

defineProps({
  title: {
    type: String,
    default: '选择流程表单'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedId = ref(null)
const selectedRow = ref(null)
const pendingId = ref(null)
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  formName: undefined,
  tableName: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.formName = undefined
  queryParams.tableName = undefined
  queryParams.pageNum = 1
  selectedId.value = pendingId.value || null
  selectedRow.value = null
  getList()
}

/** 加载表单列表 */
function getList() {
  loading.value = true
  listForm({ ...queryParams }).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.formId === selectedId.value))
        }
      })
    }
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.formName = undefined
  queryParams.tableName = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.formId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.formId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  const row = selectedRow.value || list.value.find(r => r.formId === selectedId.value)
  emit('confirm', {
    formId: row.formId,
    formName: row.formName,
    tableName: row.tableName
  })
  visible.value = false
}

/** 打开弹窗，传入当前已选表单ID用于回显 */
function open(currentFormId) {
  pendingId.value = currentFormId || null
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.form-picker {
  display: flex;
  flex-direction: column;
}

.form-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.form-picker-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover > td {
      background: #f0f7ff;
    }
  }

  :deep(.el-table__row.is-current > td) {
    background: #e6f0fd;
  }
}

.form-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
