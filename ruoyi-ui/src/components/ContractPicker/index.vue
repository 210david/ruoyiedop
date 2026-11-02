<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="contract-picker">
      <!-- 搜索栏 -->
      <div class="contract-picker-search">
        <el-input v-model="queryParams.contractNo" placeholder="合同编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.contractName" placeholder="合同名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-input v-if="!isMk" v-model="queryParams.supplierName" placeholder="供应商" clearable size="small" style="width: 160px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-input v-else v-model="queryParams.customerName" placeholder="客户" clearable size="small" style="width: 160px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <!-- 过滤条件提示 -->
      <div v-if="hint" class="contract-picker-hint">{{ hint }}</div>

      <!-- 合同表格 -->
      <div class="contract-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.contractId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="合同编号" prop="contractNo" width="140" show-overflow-tooltip />
          <el-table-column label="合同名称" prop="contractName" min-width="160" show-overflow-tooltip />
          <el-table-column v-if="!isMk" label="供应商" prop="supplierName" min-width="130" show-overflow-tooltip />
          <el-table-column v-else label="客户" prop="customerName" min-width="130" show-overflow-tooltip />
          <el-table-column label="合同金额" prop="contractAmount" width="110" align="right">
            <template #default="scope">{{ formatMoney(scope.row.contractAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" :prop="isMk ? 'contractStatus' : 'status'" width="90" align="center">
            <template #default="scope">
              <dict-tag v-if="isMk" :options="marketing_contract_status" :value="scope.row.contractStatus" />
              <dict-tag v-else :options="pms_contract_status" :value="scope.row.status" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="contract-picker-pager">
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
import { listContract as listPmsContract } from '@/api/pms/contract'
import { listContract as listMkContract } from '@/api/mk/contract'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_contract_status, marketing_contract_status } = proxy.useDict('pms_contract_status', 'marketing_contract_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择采购合同'
  },
  // 业务类型：pms=采购合同（默认，保持原有语义）；mk=营销合同（销售订单/发票/收款使用）
  bizType: {
    type: String,
    default: 'pms'
  },
  // 可选：限定合同状态（单值，如订单页仅可选择审批通过且已生效的合同 status='1'）
  status: {
    type: String,
    default: undefined
  },
  // 可选：限定合同状态列表（多值，优先于 status，如发票页可选已签订/生效中等 ['1','2','3','4']）
  statuses: {
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
  contractNo: undefined,
  contractName: undefined,
  supplierName: undefined,
  customerName: undefined
})

/** 是否营销合同数据源 */
const isMk = computed(() => props.bizType === 'mk')
/** 统一读取合同状态（pms 用 status，mk 用 contractStatus） */
function rowStatus(row) {
  return isMk.value ? row.contractStatus : row.status
}

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.contractNo = undefined
  queryParams.contractName = undefined
  queryParams.supplierName = undefined
  queryParams.customerName = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载合同列表（状态客户端过滤，前端分页） */
function getList() {
  loading.value = true
  const api = isMk.value ? listMkContract : listPmsContract
  const params = {
    pageNum: 1,
    pageSize: 999,
    contractNo: queryParams.contractNo,
    contractName: queryParams.contractName
  }
  if (isMk.value) {
    params.customerName = queryParams.customerName
  } else {
    params.supplierName = queryParams.supplierName
  }
  api(params).then(res => {
    allList.value = (res.rows || []).filter(c =>
      props.statuses.length > 0 ? props.statuses.includes(rowStatus(c))
        : (props.status ? rowStatus(c) === props.status : true)
    )
    total.value = allList.value.length
    paginate()
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.contractId === selectedId.value))
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
  queryParams.contractNo = undefined
  queryParams.contractName = undefined
  queryParams.supplierName = undefined
  queryParams.customerName = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.contractId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.contractId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value || !selectedRow.value) return
  const row = selectedRow.value
  if (isMk.value) {
    // 营销合同：回传客户与营销合同状态，供销售订单联动带出
    emit('confirm', {
      contractId: row.contractId,
      contractNo: row.contractNo,
      contractName: row.contractName,
      customerId: row.customerId,
      customerName: row.customerName,
      contractStatus: row.contractStatus,
      contractAmount: row.contractAmount
    })
  } else {
    emit('confirm', {
      contractId: row.contractId,
      contractNo: row.contractNo,
      contractName: row.contractName,
      supplierId: row.supplierId,
      supplierName: row.supplierName,
      // 合同关联的采购申请单（用于联动带出）
      orderId: row.orderId,
      orderNo: row.orderNo
    })
  }
  visible.value = false
}

/** 打开弹窗 */
function open(currentContractId) {
  visible.value = true
  if (currentContractId) {
    selectedId.value = currentContractId
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.contract-picker {
  display: flex;
  flex-direction: column;
}

.contract-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.contract-picker-hint {
  margin: -4px 0 8px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}

.contract-picker-table {
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

.contract-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
