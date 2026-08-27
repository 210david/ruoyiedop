<template>
  <el-dialog v-model="visible" width="720px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="location-picker">
      <!-- 搜索栏 -->
      <div class="location-picker-search">
        <el-input v-model="queryParams.warehouseCode" placeholder="库位编码" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.warehouseName" placeholder="库位名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 库位表格 -->
      <div class="location-picker-table">
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
          <el-table-column label="库位编码" prop="warehouseCode" width="150" show-overflow-tooltip />
          <el-table-column label="库位名称" prop="warehouseName" min-width="180" show-overflow-tooltip />
          <el-table-column label="库位类型" prop="locationType" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="locationTypeClass(scope.row.locationType)">{{ locationTypeLabel(scope.row.locationType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 提示 -->
      <div class="location-picker-tip" v-if="excludeLocationId">
        <el-icon><WarningFilled /></el-icon>
        <span>源库位已排除，不可选择相同库位</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listLocation } from '@/api/wms/warehouse'
import { Search, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '选择库位'
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
const excludeLocationId = ref(null)

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

/** 加载库位列表 */
function getList() {
  loading.value = true
  listLocation({ warehouseId: warehouseId.value, status: '0', pageSize: 999 }).then(res => {
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

/** 前端过滤（库位编码、名称 + 排除源库位） */
function applyFilter() {
  let result = allList.value
  if (queryParams.warehouseCode) {
    result = result.filter(r => r.warehouseCode && r.warehouseCode.toLowerCase().includes(queryParams.warehouseCode.toLowerCase()))
  }
  if (queryParams.warehouseName) {
    result = result.filter(r => r.warehouseName && r.warehouseName.toLowerCase().includes(queryParams.warehouseName.toLowerCase()))
  }
  if (excludeLocationId.value) {
    result = result.filter(r => r.warehouseId !== excludeLocationId.value)
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
  const loc = selectedRow.value
  emit('confirm', {
    locationId: loc.warehouseId,
    locationCode: loc.warehouseCode,
    locationName: loc.warehouseName
  })
  visible.value = false
}

/** 打开弹窗
 * @param {Number} whId - 仓库ID（必填）
 * @param {Number} currentLocationId - 当前已选的库位ID（用于回显）
 * @param {Number} excludeLocId - 需要排除的源库位ID
 */
function open(whId, currentLocationId, excludeLocId) {
  warehouseId.value = whId
  excludeLocationId.value = excludeLocId || null
  if (currentLocationId) {
    selectedId.value = currentLocationId
  }
  visible.value = true
}

/** 库位类型标签 */
function locationTypeLabel(type) {
  const map = { '0': '存储区', '1': '暂存区', '2': '分拣区', '3': '退货区' }
  return map[type] || '其他'
}

/** 库位类型样式 */
function locationTypeClass(type) {
  const map = { '0': 'blue', '1': 'amber', '2': 'violet', '3': 'red' }
  return map[type] || 'gray'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.location-picker {
  display: flex;
  flex-direction: column;
}

.location-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.location-picker-table {
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

.location-picker-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.5;

  &.blue { background: #ecf5ff; color: #409eff; border: 1px solid #d9ecff; }
  &.green { background: #f0f9ff; color: #67c23a; border: 1px solid #e1f3d8; }
  &.gray { background: #f4f4f5; color: #909399; border: 1px solid #e9e9eb; }
  &.amber { background: #fdf6ec; color: #e6a23c; border: 1px solid #f5dab1; }
  &.violet { background: #f3e8ff; color: #7c3aed; border: 1px solid #e9d5ff; }
  &.red { background: #fef0f0; color: #f56c6c; border: 1px solid #fbc4c4; }
}
</style>
