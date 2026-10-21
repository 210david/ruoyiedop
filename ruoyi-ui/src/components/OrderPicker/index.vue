<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="1" y="3" width="15" height="13"/>
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
            <circle cx="5.5" cy="18.5" r="2.5"/>
            <circle cx="18.5" cy="18.5" r="2.5"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="order-picker">
      <!-- 搜索栏 -->
      <div class="order-picker-search">
        <el-input v-model="queryParams.orderNo" placeholder="采购单号" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.supplierName" placeholder="供应商" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <!-- 过滤条件提示 -->
      <div v-if="hint" class="order-picker-hint">{{ hint }}</div>

      <!-- 订单表格 -->
      <div class="order-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.orderId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="采购单号" prop="orderNo" width="170" show-overflow-tooltip />
          <el-table-column label="供应商" prop="supplierName" min-width="160" show-overflow-tooltip />
          <el-table-column label="采购员" prop="buyerName" width="100" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.buyerName || '-' }}</template>
          </el-table-column>
          <el-table-column label="总金额" prop="totalAmount" width="110" align="right">
            <template #default="scope">{{ formatMoney(scope.row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope"><dict-tag :options="pms_order_status" :value="scope.row.status" /></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="order-picker-pager">
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

<script setup>
import { listOrder } from '@/api/pms/order'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_order_status } = proxy.useDict('pms_order_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择采购订单'
  },
  // 可选：限定订单状态列表（如收货页仅可选择已审批/已下单/部分到货的订单 ['2','3','4']）
  statuses: {
    type: Array,
    default: () => []
  },
  // 可选：排除的订单ID列表（如排除已有进行中收货单的订单）
  excludeIds: {
    type: Array,
    default: () => []
  },
  // 可选：过滤条件提示文案（说明弹框内数据的筛选范围）
  hint: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['confirm'])

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
  orderNo: undefined,
  supplierName: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.orderNo = undefined
  queryParams.supplierName = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载采购订单列表（状态/排除项客户端过滤，前端分页） */
function getList() {
  loading.value = true
  listOrder({
    pageNum: 1,
    pageSize: 999,
    orderNo: queryParams.orderNo,
    supplierName: queryParams.supplierName
  }).then(res => {
    allList.value = (res.rows || []).filter(o =>
      (props.statuses.length === 0 || props.statuses.includes(o.status)) &&
      !(props.excludeIds && props.excludeIds.includes(o.orderId))
    )
    total.value = allList.value.length
    paginate()
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.orderId === selectedId.value))
        }
      })
    }
  })
}

/** 前端分页 */
function paginate() {
  const start = (queryParams.pageNum - 1) * queryParams.pageSize
  list.value = allList.value.slice(start, start + queryParams.pageSize)
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.orderNo = undefined
  queryParams.supplierName = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.orderId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.orderId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    orderId: selectedRow.value.orderId,
    orderNo: selectedRow.value.orderNo,
    supplierId: selectedRow.value.supplierId,
    supplierName: selectedRow.value.supplierName,
    buyerId: selectedRow.value.buyerId,
    buyerName: selectedRow.value.buyerName
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentOrderId) {
  visible.value = true
  if (currentOrderId) {
    selectedId.value = currentOrderId
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.order-picker {
  display: flex;
  flex-direction: column;
}

.order-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.order-picker-hint {
  margin: -4px 0 8px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}

.order-picker-table {
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

.order-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
