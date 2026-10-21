<template>
  <el-dialog v-model="visible" width="780px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
    <div class="product-picker">
      <div class="product-picker-search">
        <el-input v-model="queryParams.productCode" placeholder="产品编码" clearable size="small" style="width: 170px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.materialName" placeholder="产品名称" clearable size="small" style="width: 170px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.productStatus" placeholder="产品状态" clearable size="small" style="width: 120px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in pdm_product_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <div class="product-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.productId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="产品编码" prop="productCode" width="150" show-overflow-tooltip />
          <el-table-column label="产品名称" prop="materialName" min-width="160" show-overflow-tooltip />
          <el-table-column label="规格型号" prop="specModel" width="140" show-overflow-tooltip />
          <el-table-column label="状态" prop="productStatus" width="90" align="center">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.productStatus)">{{ statusLabel(scope.row.productStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="product-picker-pager">
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
import { listProduct } from '@/api/pdm/product'

const { proxy } = getCurrentInstance()
const { pdm_product_status } = proxy.useDict('pdm_product_status')

const props = defineProps({
  title: { type: String, default: '选择产品档案' }
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
  productCode: undefined,
  materialName: undefined,
  productStatus: undefined
})

function onOpen() {
  queryParams.productCode = undefined
  queryParams.materialName = undefined
  queryParams.productStatus = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

function getList() {
  loading.value = true
  listProduct(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.productId === selectedId.value))
        }
      })
    }
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.productCode = undefined
  queryParams.materialName = undefined
  queryParams.productStatus = undefined
  handleQuery()
}

function onRowClick(row) {
  selectedId.value = row.productId
  selectedRow.value = row
}

function onRowDblClick(row) {
  selectedId.value = row.productId
  selectedRow.value = row
  handleConfirm()
}

function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    productId: selectedRow.value.productId,
    productCode: selectedRow.value.productCode,
    productName: selectedRow.value.materialName,
    specModel: selectedRow.value.specModel,
    unit: selectedRow.value.unit
  })
  visible.value = false
}

function open(currentProductId) {
  visible.value = true
  if (currentProductId) {
    selectedId.value = currentProductId
  }
}

function statusLabel(status) {
  const item = pdm_product_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}

function statusBadgeClass(status) {
  const item = pdm_product_status.value.find(d => d.value == status)
  const map = { 'info': 'gray', 'success': 'green', 'danger': 'red', 'warning': 'amber', 'primary': 'blue' }
  return map[item ? item.listClass : ''] || 'gray'
}

defineExpose({ open })
</script>

<style scoped>
.product-picker { display: flex; flex-direction: column; }
.product-picker-search { display: flex; align-items: center; margin-bottom: 12px; }
.product-picker-table { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.product-picker-table :deep(.el-table__row) { cursor: pointer; }
.product-picker-table :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.product-picker-table :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
.product-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>
