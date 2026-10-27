<template>
  <el-dialog v-model="visible" width="720px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="warehouse-picker">
      <!-- 搜索栏 -->
      <div class="warehouse-picker-search">
        <el-input v-model="queryParams.warehouseCode" placeholder="仓库编码" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.warehouseName" placeholder="仓库名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 仓库表格 -->
      <div class="warehouse-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.warehouseId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="仓库编码" prop="warehouseCode" width="150" show-overflow-tooltip />
          <el-table-column label="仓库名称" prop="warehouseName" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="warehouse-picker-pager">
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
import { listWarehouse } from '@/api/wms/warehouse'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '选择仓库'
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
  warehouseCode: undefined,
  warehouseName: undefined,
  status: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.warehouseCode = undefined
  queryParams.warehouseName = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载仓库列表 */
function getList() {
  loading.value = true
  listWarehouse(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.warehouseId === selectedId.value))
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
  queryParams.warehouseCode = undefined
  queryParams.warehouseName = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.warehouseId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.warehouseId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    warehouseId: selectedRow.value.warehouseId,
    warehouseCode: selectedRow.value.warehouseCode,
    warehouseName: selectedRow.value.warehouseName
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentWarehouseId) {
  visible.value = true
  if (currentWarehouseId) {
    selectedId.value = currentWarehouseId
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.warehouse-picker {
  display: flex;
  flex-direction: column;
}

.warehouse-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.warehouse-picker-table {
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

.warehouse-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.5;

  &.green { background: #f0f9ff; color: #67c23a; border: 1px solid #e1f3d8; }
  &.gray { background: #f4f4f5; color: #909399; border: 1px solid #e9e9eb; }
}
</style>
