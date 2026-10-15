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
    <div class="batch-stock-picker">
      <el-alert v-if="materialName" type="info" :closable="false" style="margin-bottom: 10px">
        <template #title>物料：{{ materialName }}　仅可选择【合格】状态批次，按效期先进先出（FEFO）排序</template>
      </el-alert>
      <div class="bsp-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="340"
          size="small"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedCode" :value="row.batchCode" :disabled="!selectable(row)" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchCode" min-width="150" show-overflow-tooltip />
          <el-table-column label="可用量" width="100" align="right">
            <template #default="{ row }">{{ formatQty(availableQty(row)) }}</template>
          </el-table-column>
          <el-table-column label="到期日期" prop="expireDate" width="105" align="center">
            <template #default="{ row }">{{ formatDate(row.expireDate) }}</template>
          </el-table-column>
          <el-table-column label="仓库" prop="warehouseName" width="110" show-overflow-tooltip />
          <el-table-column label="库位" prop="locationName" width="110" show-overflow-tooltip />
          <el-table-column label="批次状态" width="90" align="center">
            <template #default="{ row }">
              <span class="bsp-badge" :class="statusClass(row.batchStatus)"><span class="dot"></span>{{ statusLabel(row.batchStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="hasUnselectable" label="不可选原因" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="!selectable(row)" class="bsp-reason">{{ statusLabel(row.batchStatus) }}批次不可领用</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && list.length === 0" class="bsp-empty">
          <el-icon><WarningFilled /></el-icon> 该物料暂无批次库存，请先办理采购收货或完工入库生成批次
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedCode">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listBatchStockByMaterial } from '@/api/food/batch'

const { proxy } = getCurrentInstance()
const { food_batch_status } = proxy.useDict('food_batch_status')

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
const selectedCode = ref(null)
const selectedRow = ref(null)
const tableRef = ref()
const materialId = ref(null)
const materialName = ref('')

/** 存在不可选批次时显示原因列 */
const hasUnselectable = computed(() => list.value.some(r => !selectable(r)))

/** 仅合格批次可领用（待检/冻结/报废禁选） */
function selectable(row) {
  return row.batchStatus === '1'
}

/** 可用量 = 库存量 - 锁定量 */
function availableQty(row) {
  const qty = Number(row.qty) || 0
  const locked = Number(row.qtyLocked) || 0
  return Math.max(0, qty - locked)
}

/** 弹窗打开时初始化 */
function onOpen() {
  selectedCode.value = null
  selectedRow.value = null
  getList()
}

/** 加载该物料批次库存行（FEFO排序） */
function getList() {
  if (!materialId.value) return
  loading.value = true
  listBatchStockByMaterial(materialId.value).then(res => {
    list.value = res.data || []
    loading.value = false
    // 回显选中行
    if (selectedCode.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.batchCode === selectedCode.value && selectable(r)))
        }
      })
    }
  }).catch(() => { loading.value = false })
}

/** 行点击 - 选中（不可选行提示原因） */
function onRowClick(row) {
  if (!selectable(row)) {
    proxy.$modal.msgWarning('【' + row.batchCode + '】为' + statusLabel(row.batchStatus) + '批次，不可领用')
    return
  }
  selectedCode.value = row.batchCode
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  onRowClick(row)
  if (selectedCode.value === row.batchCode) {
    handleConfirm()
  }
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedCode.value) return
  emit('confirm', {
    batchNo: selectedRow.value.batchCode,
    batchStatus: selectedRow.value.batchStatus,
    expireDate: selectedRow.value.expireDate,
    availableQty: availableQty(selectedRow.value),
    batchId: selectedRow.value.batchId
  })
  visible.value = false
}

/** 打开弹窗：materialId 必传，materialName 用于展示 */
function open(mid, currentBatchNo, mName) {
  if (!mid) {
    proxy.$modal.msgWarning('缺少物料信息，无法选择批次')
    return
  }
  materialId.value = mid
  materialName.value = mName || ''
  if (currentBatchNo) {
    selectedCode.value = currentBatchNo
  }
  visible.value = true
}

function formatQty(v) {
  return v == null ? "0" : Number(v).toLocaleString(undefined, { maximumFractionDigits: 2 })
}

function formatDate(v) {
  if (!v) return '-'
  return String(v).length > 10 ? String(v).slice(0, 10) : String(v)
}

/** 字典值转标签 */
function statusLabel(v) {
  const item = food_batch_status.value.find(d => d.value == v)
  return item ? item.label : v
}

function statusClass(v) {
  return { "0": "amber", "1": "green", "2": "red", "3": "gray" }[v] || "gray"
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.batch-stock-picker {
  display: flex;
  flex-direction: column;
}

.bsp-table {
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

.bsp-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 28px 0;
  color: #909399;
  font-size: 13px;
}

.bsp-reason {
  color: #e6a23c;
  font-size: 12px;
}

/* 弹框挂载在 body 下不在页面容器内，badge 样式需组件内自包含 */
.bsp-badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.bsp-badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.bsp-badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.bsp-badge.amber .dot { background: #f59e0b; }
.bsp-badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.bsp-badge.green .dot { background: #10b981; }
.bsp-badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.bsp-badge.red .dot { background: #ef4444; }
.bsp-badge.gray { background: #f3f4f6; color: #6b7280; border-color: #e5e7eb; }
.bsp-badge.gray .dot { background: #9ca3af; }
</style>
