<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2"/>
            <polyline points="2 17 12 22 22 17"/>
            <polyline points="2 12 12 17 22 12"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="mk-material-picker">
      <!-- 搜索栏 -->
      <div class="mk-material-picker-search">
        <el-input v-model="queryParams.title" placeholder="素材标题" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.materialNo" placeholder="素材编号" clearable size="small" style="width: 150px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.materialType" placeholder="素材类型" clearable size="small" style="width: 120px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in marketing_material_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 多选模式：已选素材标签栏 -->
      <div v-if="multiple" v-show="selectedRows.length > 0" class="mk-material-picker-selected">
        <span class="selected-label">已选 {{ selectedRows.length }} 项：</span>
        <div class="selected-tags">
          <el-tag v-for="(item, idx) in selectedRows" :key="item.materialId" closable size="small" @close="removeSelected(idx)" style="margin: 2px 4px 2px 0">
            {{ item.materialNo }} - {{ item.title }}
          </el-tag>
        </div>
      </div>

      <!-- 素材表格 -->
      <div class="mk-material-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          row-key="materialId"
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
              <el-radio :model-value="selectedId" :value="row.materialId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="素材编号" prop="materialNo" width="140" show-overflow-tooltip />
          <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
          <el-table-column label="类型" prop="materialType" width="90" align="center">
            <template #default="scope"><span class="badge" :class="typeBadgeClass(scope.row.materialType)">{{ typeLabel(scope.row.materialType) }}</span></template>
          </el-table-column>
          <el-table-column label="标签" prop="tags" min-width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.tags || '-' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="mk-material-picker-pager">
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
      <el-button v-else type="primary" @click="handleConfirm" :disabled="selectedId == null">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listMaterial } from '@/api/mk/material'

const { proxy } = getCurrentInstance()
const { marketing_material_type } = proxy.useDict('marketing_material_type')

const props = defineProps({
  title: {
    type: String,
    default: '选择素材'
  },
  multiple: {
    type: Boolean,
    default: false
  },
  // v-model：素材ID（多选为数组，单选为单值）
  modelValue: {
    type: [Array, String, Number],
    default: undefined
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

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
  title: undefined,
  materialNo: undefined,
  materialType: undefined,
  materialStatus: '0'
})

/** 弹窗打开时初始化（仅重置搜索条件，选中状态由 open() 维护以保留回显） */
function onOpen() {
  queryParams.title = undefined
  queryParams.materialNo = undefined
  queryParams.materialType = undefined
  queryParams.pageNum = 1
  getList()
}

/** 加载素材列表 */
function getList() {
  loading.value = true
  listMaterial(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    if (props.multiple) {
      // 多选模式：回显已选中行的checkbox
      nextTick(() => {
        if (tableRef.value && selectedRows.value.length > 0) {
          list.value.forEach(row => {
            if (selectedRows.value.find(r => r.materialId === row.materialId)) {
              tableRef.value.toggleRowSelection(row, true)
            }
          })
        }
      })
    } else {
      // 单选模式：回显选中行
      if (selectedId.value != null) {
        nextTick(() => {
          if (tableRef.value) {
            tableRef.value.setCurrentRow(list.value.find(r => r.materialId === selectedId.value))
          }
        })
      }
    }
  }).catch(() => {
    // 请求失败（含会话过期）时清空列表，避免 loading 永久转圈
    list.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.title = undefined
  queryParams.materialNo = undefined
  queryParams.materialType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row, column) {
  if (props.multiple) {
    // 多选模式：点击行任意位置切换勾选；点击勾选框列本身时由 checkbox 处理，避免双重切换
    if (column && column.type === 'selection') return
    if (tableRef.value) tableRef.value.toggleRowSelection(row)
    return
  }
  selectedId.value = row.materialId
  selectedRow.value = row
}

/** 行双击 - 确认（单选模式） */
function onRowDblClick(row) {
  if (props.multiple) return
  selectedId.value = row.materialId
  selectedRow.value = row
  handleConfirm()
}

/** 多选模式：选择变化 */
function onSelectionChange(selection) {
  if (!props.multiple) return
  // 合并跨页选择：保留不在当前页的已选行，加上当前页新选中的行
  const currentPageIds = list.value.map(r => r.materialId)
  const keptFromOtherPages = selectedRows.value.filter(r => !currentPageIds.includes(r.materialId))
  selectedRows.value = [...keptFromOtherPages, ...selection]
}

/** 多选模式：移除已选素材 */
function removeSelected(idx) {
  const removed = selectedRows.value[idx]
  selectedRows.value.splice(idx, 1)
  // 取消表格选中状态
  if (tableRef.value) {
    const row = list.value.find(r => r.materialId === removed.materialId)
    if (row) tableRef.value.toggleRowSelection(row, false)
  }
}

/** 确认选择：emit 更新 modelValue（ID数组/单ID）与选中素材对象列表 */
function handleConfirm() {
  if (props.multiple) {
    if (selectedRows.value.length === 0) return
    const rows = selectedRows.value.map(r => ({
      materialId: r.materialId,
      materialNo: r.materialNo,
      title: r.title,
      materialType: r.materialType,
      tags: r.tags,
      content: r.content,
      fileUrl: r.fileUrl,
      fileName: r.fileName
    }))
    emit('update:modelValue', rows.map(r => r.materialId))
    emit('confirm', rows)
  } else {
    if (selectedId.value == null) return
    const r = selectedRow.value || list.value.find(i => i.materialId === selectedId.value) || { materialId: selectedId.value }
    emit('update:modelValue', selectedId.value)
    emit('confirm', {
      materialId: r.materialId,
      materialNo: r.materialNo,
      title: r.title,
      materialType: r.materialType,
      tags: r.tags,
      content: r.content,
      fileUrl: r.fileUrl,
      fileName: r.fileName
    })
  }
  visible.value = false
}

/**
 * 打开弹窗
 * 多选模式：open(selectedRowsData) 传入已选素材对象数组用于回显
 * 单选模式：open(currentMaterialId) 传入当前已选素材ID（未传时回退 modelValue）
 */
function open(selected, currentId) {
  if (props.multiple) {
    selectedRows.value = Array.isArray(selected) ? selected.map(r => ({ ...r })) : []
    if (tableRef.value) tableRef.value.clearSelection()
  } else {
    const id = selected != null && selected !== '' ? selected : (Array.isArray(props.modelValue) ? props.modelValue[0] : props.modelValue)
    selectedId.value = id != null ? id : null
    selectedRow.value = null
  }
  visible.value = true
}

/** 素材类型徽章颜色（按字典 listClass 映射） */
function typeBadgeClass(type) {
  const item = marketing_material_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}

/** 素材类型标签 */
function typeLabel(type) {
  const item = marketing_material_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.mk-material-picker {
  display: flex;
  flex-direction: column;
}

.mk-material-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.mk-material-picker-selected {
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

.mk-material-picker-table {
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

.mk-material-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

/* 类型徽章（弹窗 append-to-body 后脱离 mk-list-page 前缀，组件内自含样式） */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;
}
.badge.blue { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }
.badge.green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.badge.amber { background: #fffbeb; color: #b45309; border-color: #fde68a; }
.badge.red { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }
.badge.gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; }
</style>
