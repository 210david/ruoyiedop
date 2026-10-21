<template>
  <el-dialog v-model="visible" width="760px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="shift-picker">
      <!-- 搜索栏 -->
      <div class="shift-picker-search">
        <el-input v-model="queryParams.shiftName" placeholder="班次名称" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="queryParams.nightShift" placeholder="班次类型" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option label="白班" :value="0" />
          <el-option label="夜班" :value="1" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 班次表格 -->
      <div class="shift-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.shiftId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="班次编码" prop="shiftCode" width="110" show-overflow-tooltip />
          <el-table-column label="班次名称" prop="shiftName" width="120" show-overflow-tooltip />
          <el-table-column label="开始时间" prop="startTime" width="90" align="center" />
          <el-table-column label="结束时间" prop="endTime" width="90" align="center" />
          <el-table-column label="工作时长" prop="workHours" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.workHours != null">{{ row.workHours }}h</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="夜班" prop="nightShift" width="70" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.nightShift === 1" type="warning" size="small" effect="light">夜班</el-tag>
              <el-tag v-else type="info" size="small" effect="light">白班</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small" effect="light">
                {{ row.status === '0' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="shift-picker-pager">
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
import { listHrShift } from '@/api/hr/hrShift'

const props = defineProps({
  title: {
    type: String,
    default: '选择班次'
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
  shiftName: undefined,
  nightShift: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.shiftName = undefined
  queryParams.nightShift = undefined
  queryParams.pageNum = 1
  selectedId.value = pendingId.value || null
  selectedRow.value = null
  getList()
}

/** 加载班次列表 */
function getList() {
  loading.value = true
  listHrShift(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.shiftId === selectedId.value))
        }
      })
    }
  })
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.shiftName = undefined
  queryParams.nightShift = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.shiftId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.shiftId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  const row = selectedRow.value
  emit('confirm', {
    shiftId: row.shiftId,
    shiftCode: row.shiftCode,
    shiftName: row.shiftName,
    startTime: row.startTime,
    endTime: row.endTime,
    workHours: row.workHours,
    nightShift: row.nightShift
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentShiftId) {
  pendingId.value = currentShiftId || null
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.shift-picker {
  display: flex;
  flex-direction: column;
}

.shift-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.shift-picker-table {
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

.shift-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
