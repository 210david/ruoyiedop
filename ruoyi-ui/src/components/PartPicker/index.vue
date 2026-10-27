<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
    <div class="part-picker">
      <!-- 搜索栏 -->
      <div class="part-picker-search">
        <el-input v-model="queryParams.partCode" placeholder="备件编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.partName" placeholder="备件名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.partType" placeholder="备件类别" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in dms_part_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 备件表格 -->
      <div class="part-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.partId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="备件编号" prop="partCode" width="130" show-overflow-tooltip />
          <el-table-column label="备件名称" prop="partName" min-width="160" show-overflow-tooltip />
          <el-table-column label="备件类别" prop="partType" width="90" align="center">
            <template #default="scope"><span class="badge violet">{{ partTypeLabel(scope.row.partType) }}</span></template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" width="70" align="center">
            <template #default="scope"><span class="badge amber">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplier" width="120" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 排除提示 -->
      <div class="part-picker-tip" v-if="excludeIds.length > 0">
        <el-icon><WarningFilled /></el-icon>
        <span>已选 {{ excludeIds.length }} 个备件，已被排除选择</span>
      </div>

      <!-- 分页 -->
      <div class="part-picker-pager">
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
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedRow">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listSparepart } from '@/api/dms/sparepart'
import { Search, RefreshLeft, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { dms_part_type, wms_unit } = proxy.useDict('dms_part_type', 'wms_unit')

const props = defineProps({
  title: {
    type: String,
    default: '选择备件'
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
const excludeIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  partCode: undefined,
  partName: undefined,
  partType: undefined,
  status: '0'
})

/** 当前页可见列表（排除已选备件，避免重复选择） */
const filteredList = computed(() => {
  if (excludeIds.value.length === 0) return list.value
  return list.value.filter(r => !excludeIds.value.includes(r.partId))
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.partCode = undefined
  queryParams.partName = undefined
  queryParams.partType = undefined
  queryParams.pageNum = 1
  selectedRow.value = null
  getList()
}

/** 加载备件档案列表（仅启用状态） */
function getList() {
  loading.value = true
  listSparepart(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      const match = (res.rows || []).find(r => r.partId === selectedId.value)
      if (match) {
        selectedRow.value = match
        nextTick(() => {
          if (tableRef.value) {
            tableRef.value.setCurrentRow(match)
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
  queryParams.partCode = undefined
  queryParams.partName = undefined
  queryParams.partType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.partId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.partId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value || !selectedRow.value) return
  emit('confirm', {
    partId: selectedRow.value.partId,
    partCode: selectedRow.value.partCode,
    partName: selectedRow.value.partName,
    partType: selectedRow.value.partType,
    specModel: selectedRow.value.specModel,
    unit: selectedRow.value.unit,
    supplier: selectedRow.value.supplier
  })
  visible.value = false
}

/**
 * 打开弹窗
 * @param {Number} currentPartId 当前已选备件ID（用于回显）
 * @param {Array} excludedIds 需要排除的备件ID数组（如已被其他行选中的）
 */
function open(currentPartId, excludedIds = []) {
  visible.value = true
  excludeIds.value = excludedIds || []
  selectedId.value = currentPartId || null
  selectedRow.value = null
}

/** 单位标签 */
function unitLabel(unit) {
  const item = wms_unit.value.find(d => d.value == unit)
  return item ? item.label : '-'
}

/** 备件类别标签 */
function partTypeLabel(type) {
  const item = dms_part_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.part-picker {
  display: flex;
  flex-direction: column;
}

.part-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.part-picker-table {
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

.part-picker-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #e6a23c;
}

.part-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  &.violet {
    background: #f5f3ff;
    color: #4338ca;
    border-color: #c7d2fe;

    .dot { background: #6366f1; }
  }

  &.amber {
    background: #fffbeb;
    color: #b45309;
    border-color: #fde68a;

    .dot { background: #f59e0b; }
  }
}
</style>
