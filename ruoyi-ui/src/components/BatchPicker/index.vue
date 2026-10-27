<template>
  <el-dialog v-model="visible" width="880px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="batch-picker">
      <!-- 搜索栏 -->
      <div class="batch-picker-search">
        <el-input v-model="queryParams.batchCode" placeholder="批次号" clearable size="small" style="width: 170px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.materialName" placeholder="物料名称" clearable size="small" style="width: 170px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.batchStatus" placeholder="批次状态" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in food_batch_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 批次表格 -->
      <div class="batch-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.batchId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchCode" min-width="150" show-overflow-tooltip />
          <el-table-column label="物料" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.materialCode }} / {{ row.materialName }}</template>
          </el-table-column>
          <el-table-column label="批次数量" prop="qty" width="100" align="right">
            <template #default="{ row }">{{ formatQty(row.qty) }}</template>
          </el-table-column>
          <el-table-column label="单位" width="70" align="center">
            <template #default="{ row }"><span class="bp-badge blue">{{ unitLabel(row.qtyUnit) }}</span></template>
          </el-table-column>
          <el-table-column label="到期日期" prop="expireDate" width="105" align="center" />
          <el-table-column label="批次状态" width="90" align="center">
            <template #default="{ row }">
              <span class="bp-badge" :class="statusClass(row.batchStatus)"><span class="dot"></span>{{ statusLabel(row.batchStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="batch-picker-pager">
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
import { listBatch } from '@/api/food/batch'

const { proxy } = getCurrentInstance()
const { food_batch_status, wms_unit } = proxy.useDict('food_batch_status', 'wms_unit')

const props = defineProps({
  title: {
    type: String,
    default: '选择批次'
  }
})

const emit = defineEmits(['confirm'])

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
  batchCode: undefined,
  materialName: undefined,
  batchStatus: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.batchCode = undefined
  queryParams.materialName = undefined
  queryParams.batchStatus = undefined
  queryParams.pageNum = 1
  getList()
}

/** 加载批次列表 */
function getList() {
  loading.value = true
  listBatch(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.batchId === selectedId.value))
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
  queryParams.batchCode = undefined
  queryParams.materialName = undefined
  queryParams.batchStatus = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.batchId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.batchId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    batchId: selectedRow.value.batchId,
    batchCode: selectedRow.value.batchCode,
    materialId: selectedRow.value.materialId,
    materialCode: selectedRow.value.materialCode,
    materialName: selectedRow.value.materialName,
    qty: selectedRow.value.qty,
    qtyUnit: selectedRow.value.qtyUnit,
    batchStatus: selectedRow.value.batchStatus,
    expireDate: selectedRow.value.expireDate
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentBatchId) {
  visible.value = true
  if (currentBatchId) {
    selectedId.value = currentBatchId
  }
}

function formatQty(v) {
  return v == null ? "0" : Number(v).toLocaleString(undefined, { maximumFractionDigits: 2 })
}

/** 单位字典值转标签（存量文本数据回退原值显示） */
function unitLabel(v) {
  if (!v) return ""
  const item = wms_unit.value.find(d => d.value == v)
  return item ? item.label : v
}

/** 字典值转标签 */
function statusLabel(v) {
  const item = food_batch_status.value.find(d => d.value == v)
  return item ? item.label : v
}

function statusClass(v) {
  return { "0": "amber", "1": "green", "2": "blue", "3": "red" }[v] || "gray"
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.batch-picker {
  display: flex;
  flex-direction: column;
}

.batch-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.batch-picker-table {
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

.batch-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

/* 弹框挂载在 body 下不在页面容器内，badge 样式需组件内自包含 */
.bp-badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.bp-badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.bp-badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.bp-badge.amber .dot { background: #f59e0b; }
.bp-badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.bp-badge.blue .dot { background: #3b82f6; }
.bp-badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.bp-badge.green .dot { background: #10b981; }
.bp-badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.bp-badge.red .dot { background: #ef4444; }
.bp-badge.gray { background: #f3f4f6; color: #6b7280; border-color: #e5e7eb; }
.bp-badge.gray .dot { background: #9ca3af; }
</style>
