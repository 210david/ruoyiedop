<template>
  <el-dialog v-model="visible" width="720px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="7" height="7"/>
            <rect x="14" y="3" width="7" height="7"/>
            <rect x="3" y="14" width="7" height="7"/>
            <rect x="14" y="14" width="7" height="7"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="area-picker">
      <!-- 搜索栏 -->
      <div class="area-picker-search">
        <el-input v-model="queryParams.warehouseCode" placeholder="库区编码" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.warehouseName" placeholder="库区名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 库区表格 -->
      <div class="area-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="filteredList"
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
          <el-table-column label="库区编码" prop="warehouseCode" width="150" show-overflow-tooltip />
          <el-table-column label="库区名称" prop="warehouseName" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listArea } from '@/api/wms/warehouse'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '选择库区'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const allList = ref([])
const filteredList = ref([])
const selectedId = ref(null)
const selectedRow = ref(null)
const tableRef = ref()
const warehouseId = ref(null)

const queryParams = reactive({
  warehouseCode: undefined,
  warehouseName: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.warehouseCode = undefined
  queryParams.warehouseName = undefined
  selectedId.value = null
  selectedRow.value = null
  if (warehouseId.value) {
    getList()
  }
}

/** 加载库区列表 */
function getList() {
  loading.value = true
  listArea({ warehouseId: warehouseId.value, status: '0', pageSize: 999 }).then(res => {
    allList.value = res.rows || []
    applyFilter()
    loading.value = false
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(filteredList.value.find(r => r.warehouseId === selectedId.value))
        }
      })
    }
  })
}

/** 前端过滤 */
function applyFilter() {
  let result = allList.value
  if (queryParams.warehouseCode) {
    result = result.filter(r => r.warehouseCode && r.warehouseCode.toLowerCase().includes(queryParams.warehouseCode.toLowerCase()))
  }
  if (queryParams.warehouseName) {
    result = result.filter(r => r.warehouseName && r.warehouseName.toLowerCase().includes(queryParams.warehouseName.toLowerCase()))
  }
  filteredList.value = result
}

/** 查询 */
function handleQuery() {
  applyFilter()
}

/** 重置 */
function resetQuery() {
  queryParams.warehouseCode = undefined
  queryParams.warehouseName = undefined
  applyFilter()
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
  if (!selectedId.value || !selectedRow.value) return
  const area = selectedRow.value
  emit('confirm', {
    areaId: area.warehouseId,
    areaCode: area.warehouseCode,
    areaName: area.warehouseName
  })
  visible.value = false
}

/** 打开弹窗
 * @param {Number} whId - 仓库ID（必填）
 * @param {Number} currentAreaId - 当前已选的库区ID（用于回显）
 */
function open(whId, currentAreaId) {
  warehouseId.value = whId
  if (currentAreaId) {
    selectedId.value = currentAreaId
  }
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.area-picker {
  display: flex;
  flex-direction: column;
}

.area-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.area-picker-table {
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
