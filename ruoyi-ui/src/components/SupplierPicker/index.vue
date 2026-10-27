<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="supplier-picker">
      <!-- 搜索栏 -->
      <div class="supplier-picker-search">
        <el-input v-model="queryParams.supplierCode" placeholder="供应商编码" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.supplierName" placeholder="供应商名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.supplierType" placeholder="供应商类型" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 供应商表格 -->
      <div class="supplier-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.supplierId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="供应商编码" prop="supplierCode" width="140" show-overflow-tooltip />
          <el-table-column label="供应商名称" prop="supplierName" min-width="180" show-overflow-tooltip />
          <el-table-column label="简称" prop="supplierShortName" width="100" show-overflow-tooltip />
          <el-table-column label="类型" prop="supplierType" width="90" align="center">
            <template #default="scope"><span class="badge violet">{{ supplierTypeLabel(scope.row.supplierType) }}</span></template>
          </el-table-column>
          <el-table-column label="联系人" prop="contactPerson" width="90" show-overflow-tooltip />
          <el-table-column label="联系电话" prop="contactPhone" width="120" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="supplier-picker-pager">
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
import { listSupplier } from '@/api/wms/supplier'

const { proxy } = getCurrentInstance()
const { wms_supplier_type } = proxy.useDict('wms_supplier_type')

const props = defineProps({
  title: {
    type: String,
    default: '选择供应商'
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
  supplierCode: undefined,
  supplierName: undefined,
  supplierType: undefined,
  status: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.supplierCode = undefined
  queryParams.supplierName = undefined
  queryParams.supplierType = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载供应商列表 */
function getList() {
  loading.value = true
  listSupplier(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.supplierId === selectedId.value))
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
  queryParams.supplierCode = undefined
  queryParams.supplierName = undefined
  queryParams.supplierType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.supplierId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.supplierId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    supplierId: selectedRow.value.supplierId,
    supplierCode: selectedRow.value.supplierCode,
    supplierName: selectedRow.value.supplierName,
    supplierShortName: selectedRow.value.supplierShortName,
    supplierType: selectedRow.value.supplierType,
    contactPerson: selectedRow.value.contactPerson,
    contactPhone: selectedRow.value.contactPhone
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentSupplierId) {
  visible.value = true
  if (currentSupplierId) {
    selectedId.value = currentSupplierId
  }
}

/** 供应商类型标签 */
function supplierTypeLabel(type) {
  const item = wms_supplier_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.supplier-picker {
  display: flex;
  flex-direction: column;
}

.supplier-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.supplier-picker-table {
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

.supplier-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
