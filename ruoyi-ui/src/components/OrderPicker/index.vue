<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="order-picker">
      <div class="order-picker-search">
        <el-input v-model="queryParams.orderNo" placeholder="订单编号" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.customerName" placeholder="客户名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <div class="order-picker-table">
        <el-table ref="tableRef" v-loading="loading" :data="list" highlight-current-row @row-click="onRowClick" @row-dblclick="onRowDblClick" height="360" size="small">
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.orderId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="订单编号" prop="orderNo" width="140" show-overflow-tooltip />
          <el-table-column label="客户名称" prop="customerName" min-width="140" show-overflow-tooltip />
          <el-table-column label="合同编号" prop="contractNo" width="140" show-overflow-tooltip />
          <el-table-column label="订单金额" prop="totalAmount" width="120" align="right">
            <template #default="scope"><span class="rd-amount">￥{{ formatAmount(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope"><dict-tag :options="marketing_order_status" :value="scope.row.orderStatus" /></template>
          </el-table-column>
        </el-table>
      </div>
      <div class="order-picker-pager">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, prev, pager, next" small @current-change="getList" />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listOrder } from '@/api/mk/order'
import { formatAmount } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { marketing_order_status } = proxy.useDict('marketing_order_status')

const props = defineProps({
  title: { type: String, default: '选择订单' },
  /** 订单状态过滤（如 ['1','2','3'] 只查已确认+已发货+已完成） */
  orderStatuses: { type: Array, default: () => [] },
  /** 按合同ID过滤 */
  contractId: { type: [Number, String], default: null }
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
  orderNo: undefined,
  customerName: undefined,
  orderStatusList: [],
  contractId: undefined
})

function onOpen() {
  queryParams.orderNo = undefined
  queryParams.customerName = undefined
  queryParams.orderStatusList = props.orderStatuses.length > 0 ? [...props.orderStatuses] : []
  queryParams.contractId = props.contractId || undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

function getList() {
  loading.value = true
  listOrder(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) tableRef.value.setCurrentRow(list.value.find(r => r.orderId === selectedId.value))
      })
    }
  })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryParams.orderNo = undefined
  queryParams.customerName = undefined
  handleQuery()
}

function onRowClick(row) { selectedId.value = row.orderId; selectedRow.value = row }
function onRowDblClick(row) { onRowClick(row); handleConfirm() }

function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    orderId: selectedRow.value.orderId,
    orderNo: selectedRow.value.orderNo,
    customerId: selectedRow.value.customerId,
    customerName: selectedRow.value.customerName,
    contractId: selectedRow.value.contractId,
    contractNo: selectedRow.value.contractNo,
    totalAmount: selectedRow.value.totalAmount
  })
  visible.value = false
}

function open(currentId) {
  visible.value = true
  if (currentId) selectedId.value = currentId
}

defineExpose({ open })
</script>

<style scoped>
.order-picker { display: flex; flex-direction: column; }
.order-picker-search { display: flex; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 4px; }
.order-picker-table { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.order-picker-table :deep(.el-table__row) { cursor: pointer; }
.order-picker-table :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.order-picker-table :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
.order-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>
