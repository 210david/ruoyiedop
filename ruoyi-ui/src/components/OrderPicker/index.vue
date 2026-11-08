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
        <el-input v-model="queryParams.orderNo" :placeholder="isMk ? '订单编号' : '采购单号'" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-if="!isMk" v-model="queryParams.supplierName" placeholder="供应商" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-input v-else v-model="queryParams.customerName" placeholder="客户" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
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
          <el-table-column :label="isMk ? '订单编号' : '采购单号'" prop="orderNo" width="170" show-overflow-tooltip />
          <el-table-column v-if="!isMk" label="供应商" prop="supplierName" min-width="160" show-overflow-tooltip />
          <el-table-column v-else label="客户" prop="customerName" min-width="160" show-overflow-tooltip />
          <el-table-column v-if="!isMk" label="采购员" prop="buyerName" width="100" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.buyerName || '-' }}</template>
          </el-table-column>
          <el-table-column v-else label="负责人" prop="userName" width="100" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column :label="isMk ? '订单金额' : '总金额'" :prop="isMk ? 'orderAmount' : 'totalAmount'" width="110" align="right">
            <template #default="scope">{{ formatMoney(isMk ? scope.row.orderAmount : scope.row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" :prop="isMk ? 'orderStatus' : 'status'" width="90" align="center">
            <template #default="scope">
              <dict-tag v-if="isMk" :options="marketing_order_status" :value="scope.row.orderStatus" />
              <dict-tag v-else :options="pms_order_status" :value="scope.row.status" />
            </template>
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
import { listOrder as listPmsOrder } from '@/api/pms/order'
import { listOrder as listMkOrder } from '@/api/mk/order'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_order_status, marketing_order_status } = proxy.useDict('pms_order_status', 'marketing_order_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择采购订单'
  },
  // 业务类型：pms=采购订单（默认，保持原有语义）；mk=销售订单（发货/退货/开票使用）
  bizType: {
    type: String,
    default: 'pms'
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
  supplierName: undefined,
  customerName: undefined
})

/** 是否销售订单数据源 */
const isMk = computed(() => props.bizType === 'mk')
/** 统一读取订单状态（pms 用 status，mk 用 orderStatus） */
function rowStatus(row) {
  return isMk.value ? row.orderStatus : row.status
}

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.orderNo = undefined
  queryParams.supplierName = undefined
  queryParams.customerName = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载订单列表（状态/排除项客户端过滤，前端分页） */
function getList() {
  loading.value = true
  const api = isMk.value ? listMkOrder : listPmsOrder
  const params = {
    pageNum: 1,
    pageSize: 999,
    orderNo: queryParams.orderNo
  }
  if (isMk.value) {
    params.customerName = queryParams.customerName
  } else {
    params.supplierName = queryParams.supplierName
  }
  api(params).then(res => {
    allList.value = (res.rows || []).filter(o =>
      (props.statuses.length === 0 || props.statuses.includes(rowStatus(o))) &&
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
  }).catch(() => {
    loading.value = false
    allList.value = []
    total.value = 0
    paginate()
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
  queryParams.customerName = undefined
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
  if (!selectedId.value || !selectedRow.value) return
  const row = selectedRow.value
  if (isMk.value) {
    // 销售订单：回传客户、合同与金额，供发货/退货/开票联动带出
    emit('confirm', {
      orderId: row.orderId,
      orderNo: row.orderNo,
      customerId: row.customerId,
      customerName: row.customerName,
      contractId: row.contractId,
      contractNo: row.contractNo,
      orderAmount: row.orderAmount,
      orderStatus: row.orderStatus
    })
  } else {
    emit('confirm', {
      orderId: row.orderId,
      orderNo: row.orderNo,
      supplierId: row.supplierId,
      supplierName: row.supplierName,
      buyerId: row.buyerId,
      buyerName: row.buyerName
    })
  }
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
