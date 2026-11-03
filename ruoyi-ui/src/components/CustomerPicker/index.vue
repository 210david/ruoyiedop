<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="customer-picker">
      <!-- 搜索栏 -->
      <div class="customer-picker-search">
        <el-input v-model="queryParams.customerNo" placeholder="客户编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.customerName" placeholder="企业名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.customerLevel" placeholder="客户等级" clearable size="small" style="width: 120px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-select v-model="queryParams.customerStatus" placeholder="客户状态（全部）" clearable size="small" style="width: 140px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 客户表格 -->
      <div class="customer-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="360"
          size="small"
          empty-text="未找到客户，请调整筛选条件或先在客户管理中新增客户"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.customerId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="客户编号" prop="customerNo" width="130" show-overflow-tooltip />
          <el-table-column label="企业名称" prop="customerName" min-width="180" show-overflow-tooltip />
          <el-table-column label="客户等级" prop="customerLevel" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_customer_level" :value="scope.row.customerLevel" /></template>
          </el-table-column>
          <el-table-column label="客户状态" prop="customerStatus" width="100" align="center">
            <template #default="scope"><dict-tag :options="marketing_customer_status" :value="scope.row.customerStatus" /></template>
          </el-table-column>
          <el-table-column label="信用代码" prop="creditCode" width="150" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="customer-picker-pager">
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
import { listCustomer, getCustomer } from '@/api/mk/customer'

const { proxy } = getCurrentInstance()
const { marketing_customer_level, marketing_customer_status } = proxy.useDict('marketing_customer_level', 'marketing_customer_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择客户'
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

/** 打开时由父页面带入的当前选中客户，用于回显 */
let pending = { id: null, name: '' }

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  customerNo: undefined,
  customerName: undefined,
  customerLevel: undefined,
  customerStatus: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.customerNo = undefined
  queryParams.customerName = undefined
  queryParams.customerLevel = undefined
  queryParams.customerStatus = undefined
  queryParams.pageNum = 1
  selectedId.value = pending.id || null
  selectedRow.value = null
  // 修改场景：先回填当前所属客户，避免其不在当前页时确定按钮不可用
  if (pending.id) {
    if (pending.name) {
      selectedRow.value = { customerId: pending.id, customerName: pending.name }
    } else {
      // 调用方只传了ID：补拉详情，防止直接点确定时把客户名称写成空值
      getCustomer(pending.id).then(res => {
        const d = res.data || {}
        if (selectedId.value === pending.id) {
          selectedRow.value = { customerId: d.customerId || pending.id, customerName: d.customerName, customerNo: d.customerNo }
        }
      }).catch(() => {
        // 详情拉取失败时不阻塞选择，用户仍可在列表中重新选
      })
    }
  }
  getList()
}

/** 加载客户列表 */
function getList() {
  loading.value = true
  listCustomer(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行：命中当前页时用行数据补全名称等信息
    if (selectedId.value) {
      const hit = (list.value || []).find(r => r.customerId === selectedId.value)
      if (hit) {
        selectedRow.value = hit
      }
      nextTick(() => {
        if (tableRef.value && hit) {
          tableRef.value.setCurrentRow(hit)
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
  queryParams.customerNo = undefined
  queryParams.customerName = undefined
  queryParams.customerLevel = undefined
  queryParams.customerStatus = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.customerId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.customerId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
async function handleConfirm() {
  if (!selectedId.value) return
  let row = selectedRow.value
  if (!row) {
    // 仅回显了ID且详情尚未取到：确认时补拉一次，避免按钮点了没反应
    try {
      const res = await getCustomer(selectedId.value)
      const d = res.data || {}
      row = { customerId: d.customerId || selectedId.value, customerName: d.customerName, customerNo: d.customerNo }
    } catch (e) {
      proxy.$modal.msgWarning('未能获取客户信息，请重新选择客户')
      return
    }
  }
  emit('confirm', {
    customerId: row.customerId,
    customerNo: row.customerNo,
    customerName: row.customerName
  })
  visible.value = false
}

/**
 * 打开弹窗
 * @param {Number|String} currentCustomerId 当前已选客户ID（回显用）
 * @param {String} currentCustomerName 当前已选客户名称（回显用，缺省时仅保留ID）
 */
function open(currentCustomerId, currentCustomerName) {
  pending = { id: currentCustomerId || null, name: currentCustomerName || '' }
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.customer-picker {
  display: flex;
  flex-direction: column;
}

.customer-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.customer-picker-table {
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

.customer-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
