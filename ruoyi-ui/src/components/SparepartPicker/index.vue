<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
    <div class="sparepart-picker">
      <!-- 搜索栏 -->
      <div class="sparepart-picker-search">
        <el-input v-model="queryParams.partCode" placeholder="备件编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.partName" placeholder="备件名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.partType" placeholder="备件类别" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in dms_part_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 备件表格 -->
      <div class="sparepart-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.stockId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="备件编号" prop="partCode" width="140" show-overflow-tooltip />
          <el-table-column label="备件名称" prop="partName" min-width="160" show-overflow-tooltip />
          <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
          <el-table-column label="仓库" prop="warehouseName" width="110" show-overflow-tooltip />
          <el-table-column label="库存数量" prop="currentStock" width="100" align="center">
            <template #default="scope"><span class="badge green">{{ scope.row.currentStock }}</span></template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" width="80" align="center">
            <template #default="scope"><span class="badge amber">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="sparepart-picker-pager">
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
import { listPartLedger } from '@/api/dms/partledger'
import { Search, RefreshLeft } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { dms_part_type, wms_unit } = proxy.useDict('dms_part_type', 'wms_unit')

const props = defineProps({
  title: {
    type: String,
    default: '选择备件'
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
  partCode: undefined,
  partName: undefined,
  partType: undefined,
  status: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.partCode = undefined
  queryParams.partName = undefined
  queryParams.partType = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载备件台账列表（仅库存大于0） */
function getList() {
  loading.value = true
  listPartLedger(queryParams).then(res => {
    // 仅显示库存大于0的备件
    list.value = (res.rows || []).filter(item => item.currentStock != null && Number(item.currentStock) > 0)
    total.value = list.value.length
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.stockId === selectedId.value))
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
  queryParams.partCode = undefined
  queryParams.partName = undefined
  queryParams.partType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.stockId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.stockId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    stockId: selectedRow.value.stockId,
    partId: selectedRow.value.partId,
    partCode: selectedRow.value.partCode,
    partName: selectedRow.value.partName,
    partType: selectedRow.value.partType,
    specModel: selectedRow.value.specModel,
    unit: selectedRow.value.unit,
    currentStock: selectedRow.value.currentStock,
    warehouseName: selectedRow.value.warehouseName || '备件库'
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentStockId) {
  visible.value = true
  if (currentStockId) {
    selectedId.value = currentStockId
  }
}

/** 单位标签 */
function unitLabel(unit) {
  const item = wms_unit.value.find(d => d.value == unit)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.sparepart-picker {
  display: flex;
  flex-direction: column;
}

.sparepart-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.sparepart-picker-table {
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

.sparepart-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  &.green {
    background: #ecfdf5;
    color: #047857;
    border-color: #a7f3d0;

    .dot { background: #10b981; }
  }

  &.amber {
    background: #fffbeb;
    color: #b45309;
    border-color: #fde68a;

    .dot { background: #f59e0b; }
  }
}
</style>
