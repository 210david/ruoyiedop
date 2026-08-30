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
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="contract-picker">
      <div class="contract-picker-search">
        <el-input v-model="queryParams.contractNo" placeholder="合同编号" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.contractName" placeholder="合同名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.customerName" placeholder="客户名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <div class="contract-picker-table">
        <el-table ref="tableRef" v-loading="loading" :data="list" highlight-current-row @row-click="onRowClick" @row-dblclick="onRowDblClick" height="360" size="small">
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.contractId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="合同编号" prop="contractNo" width="140" show-overflow-tooltip />
          <el-table-column label="合同名称" prop="contractName" min-width="160" show-overflow-tooltip />
          <el-table-column label="客户名称" prop="customerName" min-width="140" show-overflow-tooltip />
          <el-table-column label="合同金额" prop="totalAmount" width="120" align="right">
            <template #default="scope"><span class="rd-amount">￥{{ formatAmount(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope"><dict-tag :options="marketing_contract_status" :value="scope.row.contractStatus" /></template>
          </el-table-column>
        </el-table>
      </div>
      <div class="contract-picker-pager">
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
import { listContract } from '@/api/mk/contract'
import { formatAmount } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { marketing_contract_status } = proxy.useDict('marketing_contract_status')

const props = defineProps({
  title: { type: String, default: '选择合同' },
  /** 合同状态过滤（如 ['1','3'] 只查执行中+已完成的） */
  contractStatuses: { type: Array, default: () => [] }
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
  contractNo: undefined,
  contractName: undefined,
  customerName: undefined,
  contractStatusList: []
})

function onOpen() {
  queryParams.contractNo = undefined
  queryParams.contractName = undefined
  queryParams.customerName = undefined
  queryParams.contractStatusList = props.contractStatuses.length > 0 ? [...props.contractStatuses] : []
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

function getList() {
  loading.value = true
  listContract(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) tableRef.value.setCurrentRow(list.value.find(r => r.contractId === selectedId.value))
      })
    }
  })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryParams.contractNo = undefined
  queryParams.contractName = undefined
  queryParams.customerName = undefined
  handleQuery()
}

function onRowClick(row) { selectedId.value = row.contractId; selectedRow.value = row }
function onRowDblClick(row) { onRowClick(row); handleConfirm() }

function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    contractId: selectedRow.value.contractId,
    contractNo: selectedRow.value.contractNo,
    contractName: selectedRow.value.contractName,
    customerId: selectedRow.value.customerId,
    customerName: selectedRow.value.customerName,
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
.contract-picker { display: flex; flex-direction: column; }
.contract-picker-search { display: flex; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 4px; }
.contract-picker-table { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.contract-picker-table :deep(.el-table__row) { cursor: pointer; }
.contract-picker-table :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.contract-picker-table :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
.contract-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>
