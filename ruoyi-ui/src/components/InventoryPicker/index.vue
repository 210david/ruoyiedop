<template>
  <el-dialog v-model="visible" width="900px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
    <div class="inventory-picker">
      <!-- 搜索栏 -->
      <div class="inventory-picker-search">
        <el-input v-model="queryParams.materialCode" placeholder="物料编码" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.materialName" placeholder="物料名称" clearable size="small" style="width: 160px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-input v-model="queryParams.batchNo" placeholder="批次号" clearable size="small" style="width: 130px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 库存表格 -->
      <div class="inventory-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="filteredList"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="360"
          size="small"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.inventoryId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="物料编码" prop="materialCode" width="130" show-overflow-tooltip />
          <el-table-column label="物料名称" prop="materialName" min-width="140" show-overflow-tooltip />
          <el-table-column label="规格型号" prop="specModel" width="110" show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" width="60" align="center">
            <template #default="scope"><span class="badge blue">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchNo" width="100" align="center" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.batchNo || '无' }}</template>
          </el-table-column>
          <el-table-column label="库位" prop="locationName" width="90" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.locationName || '-' }}</template>
          </el-table-column>
          <el-table-column label="可用库存" prop="qty" width="80" align="center">
            <template #default="scope"><span class="badge green">{{ scope.row.qty }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 提示 -->
      <div class="inventory-picker-tip" v-if="excludeIds.length > 0">
        <el-icon><WarningFilled /></el-icon>
        <span>已选 {{ excludeIds.length }} 条库存，已被排除选择</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listInventory } from '@/api/wms/inventory'
import { Search, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { wms_unit } = proxy.useDict('wms_unit')

const props = defineProps({
  title: {
    type: String,
    default: '选择库存物料'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const allList = ref([])
const filteredList = ref([])
const selectedId = ref(null)
const selectedRow = ref(null)
const tableRef = ref()
const warehouseId = ref(null)
const excludeIds = ref([])

const queryParams = reactive({
  materialCode: undefined,
  materialName: undefined,
  batchNo: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.materialCode = undefined
  queryParams.materialName = undefined
  queryParams.batchNo = undefined
  selectedId.value = null
  selectedRow.value = null
  if (warehouseId.value) {
    getList()
  }
}

/** 加载库存列表 */
function getList() {
  loading.value = true
  listInventory({ warehouseId: warehouseId.value, pageSize: 999 }).then(res => {
    allList.value = (res.rows || []).filter(r => r.qty > 0)
    applyFilter()
    loading.value = false
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(filteredList.value.find(r => r.inventoryId === selectedId.value))
        }
      })
    }
  })
}

/** 前端过滤（物料编码、名称、批次号 + 排除已选） */
function applyFilter() {
  let result = allList.value
  if (queryParams.materialCode) {
    result = result.filter(r => r.materialCode && r.materialCode.toLowerCase().includes(queryParams.materialCode.toLowerCase()))
  }
  if (queryParams.materialName) {
    result = result.filter(r => r.materialName && r.materialName.toLowerCase().includes(queryParams.materialName.toLowerCase()))
  }
  if (queryParams.batchNo) {
    result = result.filter(r => r.batchNo && r.batchNo.toLowerCase().includes(queryParams.batchNo.toLowerCase()))
  }
  if (excludeIds.value.length > 0) {
    result = result.filter(r => !excludeIds.value.includes(r.inventoryId))
  }
  filteredList.value = result
}

/** 查询 */
function handleQuery() {
  applyFilter()
}

/** 重置 */
function resetQuery() {
  queryParams.materialCode = undefined
  queryParams.materialName = undefined
  queryParams.batchNo = undefined
  applyFilter()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.inventoryId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.inventoryId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value || !selectedRow.value) return
  const inv = selectedRow.value
  emit('confirm', {
    inventoryId: inv.inventoryId,
    materialId: inv.materialId,
    materialCode: inv.materialCode,
    materialName: inv.materialName,
    specModel: inv.specModel,
    unit: inv.unit,
    batchNo: inv.batchNo || '',
    locationId: inv.locationId,
    locationCode: inv.locationCode,
    locationName: inv.locationName,
    qty: inv.qty,
    availableQty: inv.qty
  })
  visible.value = false
}

/** 打开弹窗
 * @param {Number} whId - 仓库ID（必填）
 * @param {Number} currentInventoryId - 当前已选的库存ID（用于回显）
 * @param {Array} excludedIds - 需要排除的库存ID数组（如已被其他行选中的）
 */
function open(whId, currentInventoryId, excludedIds = []) {
  warehouseId.value = whId
  excludeIds.value = excludedIds
  if (currentInventoryId) {
    selectedId.value = currentInventoryId
  }
  visible.value = true
}

/** 单位标签 */
function unitLabel(unit) {
  const item = wms_unit.value.find(d => d.value == unit)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.inventory-picker {
  display: flex;
  flex-direction: column;
}

.inventory-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.inventory-picker-table {
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

.inventory-picker-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.5;

  &.blue { background: #ecf5ff; color: #409eff; border: 1px solid #d9ecff; }
  &.green { background: #f0f9ff; color: #67c23a; border: 1px solid #e1f3d8; }
}
</style>
