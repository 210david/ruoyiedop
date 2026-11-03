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
    <div class="shipment-picker">
      <!-- 搜索栏 -->
      <div class="shipment-picker-search">
        <el-input v-model="queryParams.shipmentNo" placeholder="发货单号" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.customerName" placeholder="客户名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 发货单表格 -->
      <div class="shipment-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.shipmentId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="发货单号" prop="shipmentNo" width="170" show-overflow-tooltip />
          <el-table-column label="客户名称" prop="customerName" min-width="200" show-overflow-tooltip />
          <el-table-column label="发货日期" prop="shipmentDate" width="110" align="center">
            <template #default="scope">{{ (scope.row.shipmentDate || '').slice(0, 10) }}</template>
          </el-table-column>
          <el-table-column label="出库单号" prop="outboundOrderNo" width="160" show-overflow-tooltip />
        </el-table>
        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          :pager-count="5"
          @pagination="getList"
          style="margin-top: 8px"
        />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="ShipmentPicker">
import { listShipment } from '@/api/mk/shipment'

const props = defineProps({
  title: {
    type: String,
    default: '选择发货单'
  },
  /** 固定客户类型（如流向登记传 '2' 仅经销商）：列表只查该类型客户的发货单 */
  customerType: {
    type: String,
    default: undefined
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
  shipmentNo: undefined,
  customerName: undefined,
  customerType: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.shipmentNo = undefined
  queryParams.customerName = undefined
  queryParams.customerType = props.customerType || undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载发货单列表 */
function getList() {
  loading.value = true
  listShipment(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.shipmentId === selectedId.value))
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
  queryParams.shipmentNo = undefined
  queryParams.customerName = undefined
  queryParams.customerType = props.customerType || undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.shipmentId
  selectedRow.value = row
}

/** 行双击 - 选中并确认 */
function onRowDblClick(row) {
  onRowClick(row)
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    shipmentId: selectedRow.value.shipmentId,
    shipmentNo: selectedRow.value.shipmentNo,
    customerName: selectedRow.value.customerName
  })
  visible.value = false
}

/** 打开弹窗（currentShipmentId 用于回显选中） */
function open(currentShipmentId) {
  visible.value = true
  if (currentShipmentId) {
    selectedId.value = currentShipmentId
  }
}

defineExpose({ open })
</script>

<style scoped>
.shipment-picker-search { display: flex; align-items: center; margin-bottom: 12px; }
.shipment-picker-table { margin-bottom: 4px; }
</style>
