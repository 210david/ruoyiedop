<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2"/>
            <polyline points="2 17 12 22 22 17"/>
            <polyline points="2 12 12 17 22 12"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="mk-activity-picker">
      <!-- 搜索栏 -->
      <div class="mk-activity-picker-search">
        <el-input v-model="queryParams.activityName" placeholder="活动名称" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.activityNo" placeholder="活动编号" clearable size="small" style="width: 150px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.activityStatus" placeholder="活动状态" clearable size="small" style="width: 120px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in marketing_activity_status.filter(s => !['0', '4'].includes(s.value))" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 活动表格（单选） -->
      <div class="mk-activity-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          row-key="activityId"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="360"
          size="small"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.activityId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="活动编号" prop="activityNo" width="150" show-overflow-tooltip />
          <el-table-column label="活动名称" prop="activityName" min-width="200" show-overflow-tooltip />
          <el-table-column label="活动状态" prop="activityStatus" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.activityStatus)">{{ statusLabel(scope.row.activityStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="mk-activity-picker-pager">
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
      <el-button type="primary" @click="handleConfirm" :disabled="selectedId == null">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listActivity } from '@/api/mk/activity'

const { proxy } = getCurrentInstance()
const { marketing_activity_status } = proxy.useDict('marketing_activity_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择关联活动'
  },
  // v-model：活动ID
  modelValue: {
    type: [String, Number],
    default: undefined
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

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
  activityName: undefined,
  activityNo: undefined,
  activityStatus: undefined,
  // 排除草稿、已取消活动（不可作为关联目标）
  excludeStatuses: '0,4'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.activityName = undefined
  queryParams.activityNo = undefined
  queryParams.activityStatus = undefined
  queryParams.excludeStatuses = '0,4'
  queryParams.pageNum = 1
  getList()
}

/** 加载活动列表 */
function getList() {
  loading.value = true
  listActivity(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    // 回显选中行
    if (selectedId.value != null) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.activityId === selectedId.value))
        }
      })
    }
  }).catch(() => {
    list.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.activityName = undefined
  queryParams.activityNo = undefined
  queryParams.activityStatus = undefined
  queryParams.excludeStatuses = '0,4'
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.activityId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.activityId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (selectedId.value == null) return
  const r = selectedRow.value || list.value.find(i => i.activityId === selectedId.value) || { activityId: selectedId.value }
  emit('update:modelValue', selectedId.value)
  emit('confirm', {
    activityId: r.activityId,
    activityNo: r.activityNo,
    activityName: r.activityName
  })
  visible.value = false
}

/**
 * 打开弹窗
 * open(currentActivityId) 传入当前已选活动ID（未传时回退 modelValue）
 */
function open(selected) {
  const id = selected != null && selected !== '' ? selected : props.modelValue
  selectedId.value = id != null ? id : null
  selectedRow.value = null
  visible.value = true
}

/** 活动状态徽章颜色 */
function statusBadgeClass(status) {
  const item = marketing_activity_status.value.find(d => d.value == status)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}

/** 活动状态标签 */
function statusLabel(status) {
  const item = marketing_activity_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped>
.mk-activity-picker {
  display: flex;
  flex-direction: column;
}
.mk-activity-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}
.mk-activity-picker-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.mk-activity-picker-table :deep(.el-table__row) {
  cursor: pointer;
}
.mk-activity-picker-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
