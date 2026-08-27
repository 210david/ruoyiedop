<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="equipment-picker">
      <!-- 搜索栏 -->
      <div class="equipment-picker-search">
        <el-input v-model="queryParams.equipmentCode" placeholder="设备编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.equipmentName" placeholder="设备名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.equipmentStatus" placeholder="设备状态" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in dms_equipment_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 多选模式：已选设备标签栏 -->
      <div v-if="multiple" v-show="selectedRows.length > 0" class="equipment-picker-selected">
        <span class="selected-label">已选 {{ selectedRows.length }} 台：</span>
        <div class="selected-tags">
          <el-tag v-for="(item, idx) in selectedRows" :key="item.equipmentId" closable size="small" @close="removeSelected(idx)" style="margin: 2px 4px 2px 0">
            {{ item.equipmentCode }} - {{ item.equipmentName }}
          </el-tag>
        </div>
      </div>

      <!-- 设备表格 -->
      <div class="equipment-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          @selection-change="onSelectionChange"
          height="360"
          size="small"
        >
          <!-- 多选模式：checkbox 列 -->
          <el-table-column v-if="multiple" type="selection" width="45" align="center" :reserve-selection="true" />
          <!-- 单选模式：radio 列 -->
          <el-table-column v-else width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.equipmentId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="设备编号" prop="equipmentCode" width="140" show-overflow-tooltip />
          <el-table-column label="设备名称" prop="equipmentName" min-width="160" show-overflow-tooltip />
          <el-table-column label="型号" prop="model" width="120" show-overflow-tooltip />
          <el-table-column label="使用部门" prop="deptName" width="110" show-overflow-tooltip />
          <el-table-column label="安装位置" prop="installLocation" width="120" show-overflow-tooltip />
          <el-table-column label="状态" prop="equipmentStatus" width="80" align="center">
            <template #default="scope"><span class="badge green">{{ equipmentStatusLabel(scope.row.equipmentStatus) }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="equipment-picker-pager">
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
      <el-button v-if="multiple" type="primary" @click="handleConfirm" :disabled="selectedRows.length === 0">确 定</el-button>
      <el-button v-else type="primary" @click="handleConfirm" :disabled="!selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listEquipment } from '@/api/dms/equipment'

const { proxy } = getCurrentInstance()
const { dms_equipment_status } = proxy.useDict('dms_equipment_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择设备'
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedId = ref(null)
const selectedRow = ref(null)
const selectedRows = ref([])  // 多选模式
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  equipmentCode: undefined,
  equipmentName: undefined,
  equipmentStatus: undefined,
  status: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.equipmentCode = undefined
  queryParams.equipmentName = undefined
  queryParams.equipmentStatus = undefined
  queryParams.pageNum = 1
  if (!props.multiple) {
    selectedId.value = null
    selectedRow.value = null
  }
  getList()
}

/** 加载设备列表 */
function getList() {
  loading.value = true
  listEquipment(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (props.multiple) {
      // 多选模式：回显已选中行的checkbox
      nextTick(() => {
        if (tableRef.value && selectedRows.value.length > 0) {
          list.value.forEach(row => {
            if (selectedRows.value.find(r => r.equipmentId === row.equipmentId)) {
              tableRef.value.toggleRowSelection(row, true)
            }
          })
        }
      })
    } else {
      // 单选模式：回显选中行
      if (selectedId.value) {
        nextTick(() => {
          if (tableRef.value) {
            tableRef.value.setCurrentRow(list.value.find(r => r.equipmentId === selectedId.value))
          }
        })
      }
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
  queryParams.equipmentCode = undefined
  queryParams.equipmentName = undefined
  queryParams.equipmentStatus = undefined
  handleQuery()
}

/** 行点击 - 选中（单选模式） */
function onRowClick(row) {
  if (props.multiple) return // 多选模式由 checkbox 处理
  selectedId.value = row.equipmentId
  selectedRow.value = row
}

/** 行双击 - 确认（单选模式） */
function onRowDblClick(row) {
  if (props.multiple) return
  selectedId.value = row.equipmentId
  selectedRow.value = row
  handleConfirm()
}

/** 多选模式：选择变化 */
function onSelectionChange(selection) {
  if (!props.multiple) return
  // 合并跨页选择：保留不在当前页的已选行，加上当前页新选中的行
  const currentPageIds = list.value.map(r => r.equipmentId)
  const keptFromOtherPages = selectedRows.value.filter(r => !currentPageIds.includes(r.equipmentId))
  selectedRows.value = [...keptFromOtherPages, ...selection]
}

/** 多选模式：移除已选设备 */
function removeSelected(idx) {
  const removed = selectedRows.value[idx]
  selectedRows.value.splice(idx, 1)
  // 取消表格选中状态
  if (tableRef.value) {
    const row = list.value.find(r => r.equipmentId === removed.equipmentId)
    if (row) tableRef.value.toggleRowSelection(row, false)
  }
}

/** 确认选择 */
function handleConfirm() {
  if (props.multiple) {
    if (selectedRows.value.length === 0) return
    emit('confirm', selectedRows.value.map(r => ({
      equipmentId: r.equipmentId,
      equipmentCode: r.equipmentCode,
      equipmentName: r.equipmentName,
      model: r.model,
      categoryId: r.categoryId,
      categoryName: r.categoryName,
      deptId: r.deptId,
      deptName: r.deptName,
      installLocation: r.installLocation,
      equipmentStatus: r.equipmentStatus
    })))
  } else {
    if (!selectedId.value) return
    emit('confirm', {
      equipmentId: selectedRow.value.equipmentId,
      equipmentCode: selectedRow.value.equipmentCode,
      equipmentName: selectedRow.value.equipmentName,
      model: selectedRow.value.model,
      categoryId: selectedRow.value.categoryId,
      categoryName: selectedRow.value.categoryName,
      deptId: selectedRow.value.deptId,
      deptName: selectedRow.value.deptName,
      installLocation: selectedRow.value.installLocation,
      equipmentStatus: selectedRow.value.equipmentStatus
    })
  }
  visible.value = false
}

/** 打开弹窗 */
function open(currentEquipmentId, selectedRowsData) {
  visible.value = true
  if (props.multiple) {
    // 多选模式：传入已选设备数组用于回显
    selectedRows.value = selectedRowsData ? [...selectedRowsData] : []
    // 清空表格选中状态（将在 getList 后回显）
    if (tableRef.value) {
      tableRef.value.clearSelection()
    }
  } else {
    // 单选模式：传入当前已选ID
    if (currentEquipmentId) {
      selectedId.value = currentEquipmentId
    } else {
      selectedId.value = null
    }
    selectedRow.value = null
  }
}

/** 设备状态标签 */
function equipmentStatusLabel(status) {
  const item = dms_equipment_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.equipment-picker {
  display: flex;
  flex-direction: column;
}

.equipment-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.equipment-picker-selected {
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f0f7ff;
  border: 1px solid #d6e4ff;
  border-radius: 8px;

  .selected-label {
    font-size: 13px;
    font-weight: 600;
    color: #409eff;
    margin-right: 4px;
  }

  .selected-tags {
    display: inline;
  }
}

.equipment-picker-table {
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

.equipment-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
