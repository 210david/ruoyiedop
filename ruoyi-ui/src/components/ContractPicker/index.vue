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
        <el-input v-model="queryParams.supplierName" placeholder="供应商" clearable size="small" style="width: 160px; margin-left: 8px" @keyup.enter="handleQuery" />
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
          <el-table-column label="供应商" prop="supplierName" min-width="130" show-overflow-tooltip />
          <el-table-column label="合同金额" prop="contractAmount" width="110" align="right">
            <template #default="scope">{{ formatMoney(scope.row.contractAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope"><dict-tag :options="pms_contract_status" :value="scope.row.status" /></template>
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
import { listContract } from '@/api/pms/contract'
import { formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_contract_status } = proxy.useDict('pms_contract_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择采购合同'
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
  supplierName: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.contractNo = undefined
  queryParams.contractName = undefined
  queryParams.supplierName = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载合同列表（状态客户端过滤，前端分页） */
function getList() {
  loading.value = true
  listContract({
    pageNum: 1,
    pageSize: 999,
    contractNo: queryParams.contractNo,
    contractName: queryParams.contractName,
    supplierName: queryParams.supplierName
  }).then(res => {
    allList.value = (res.rows || []).filter(c =>
      props.statuses.length > 0 ? props.statuses.includes(c.status)
        : (props.status ? c.status === props.status : true)
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
  if (!selectedId.value) return
  emit('confirm', {
    contractId: selectedRow.value.contractId,
    contractNo: selectedRow.value.contractNo,
    contractName: selectedRow.value.contractName,
    supplierId: selectedRow.value.supplierId,
    supplierName: selectedRow.value.supplierName,
    // 合同关联的采购申请单（用于联动带出）
    orderId: selectedRow.value.orderId,
    orderNo: selectedRow.value.orderNo
  })
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
