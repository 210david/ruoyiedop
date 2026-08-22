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
    <div class="material-picker">
      <!-- 搜索栏 -->
      <div class="material-picker-search">
        <el-input v-model="queryParams.materialCode" placeholder="物料编码" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.materialName" placeholder="物料名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.materialType" placeholder="物料类型" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="item in availableMaterialTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 物料表格 -->
      <div class="material-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.materialId" @click.stop><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="物料编码" prop="materialCode" width="140" show-overflow-tooltip />
          <el-table-column label="物料名称" prop="materialName" min-width="160" show-overflow-tooltip />
          <el-table-column label="物料类型" prop="materialType" width="90" align="center">
            <template #default="scope"><span class="badge" :class="materialTypeBadgeClass(scope.row.materialType)">{{ materialTypeLabel(scope.row.materialType) }}</span></template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" width="130" show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" width="70" align="center">
            <template #default="scope"><span class="badge blue">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="material-picker-pager">
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
import { listMaterial } from '@/api/wms/material'

const { proxy } = getCurrentInstance()
const { wms_unit, wms_material_type } = proxy.useDict('wms_unit', 'wms_material_type')

const props = defineProps({
  title: {
    type: String,
    default: '选择物料'
  },
  /** 物料类型列表（用于过滤，如 ['1','2'] 表示半成品+成品） */
  materialTypes: {
    type: Array,
    default: () => []
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
  materialCode: undefined,
  materialName: undefined,
  materialType: undefined,
  materialTypes: [],
  status: '0'
})

/** 可选的物料类型（受 props.materialTypes 限制） */
const availableMaterialTypes = computed(() => {
  if (props.materialTypes.length > 0) {
    return wms_material_type.value.filter(d => props.materialTypes.includes(d.value))
  }
  return wms_material_type.value
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.materialCode = undefined
  queryParams.materialName = undefined
  queryParams.materialType = undefined
  queryParams.materialTypes = props.materialTypes.length > 0 ? [...props.materialTypes] : []
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载物料列表 */
function getList() {
  loading.value = true
  listMaterial(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.materialId === selectedId.value))
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
  queryParams.materialCode = undefined
  queryParams.materialName = undefined
  queryParams.materialType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.materialId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.materialId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    materialId: selectedRow.value.materialId,
    materialCode: selectedRow.value.materialCode,
    materialName: selectedRow.value.materialName,
    materialType: selectedRow.value.materialType,
    specModel: selectedRow.value.specModel,
    unit: selectedRow.value.unit
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentMaterialId) {
  visible.value = true
  if (currentMaterialId) {
    selectedId.value = currentMaterialId
  }
}

/** 单位标签 */
function unitLabel(unit) {
  const item = wms_unit.value.find(d => d.value == unit)
  return item ? item.label : '-'
}

/** 物料类型标签 */
function materialTypeLabel(type) {
  const item = wms_material_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

/** 物料类型徽章样式 */
function materialTypeBadgeClass(type) {
  const item = wms_material_type.value.find(d => d.value == type)
  const listClass = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[listClass] || 'gray'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.material-picker {
  display: flex;
  flex-direction: column;
}

.material-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.material-picker-table {
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

.material-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
