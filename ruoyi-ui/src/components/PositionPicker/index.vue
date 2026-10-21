<template>
  <el-dialog v-model="visible" width="640px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
    <div class="position-picker">
      <!-- 搜索栏 -->
      <div class="position-picker-search">
        <el-input v-model="queryParams.postCode" placeholder="岗位编码" clearable size="small" style="width: 150px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.postName" placeholder="岗位名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 岗位表格 -->
      <div class="position-picker-table">
        <!-- 多选模式 -->
        <el-table
          v-if="multiple"
          ref="tableRef"
          v-loading="loading"
          :data="list"
          height="360"
          size="small"
          @selection-change="onSelectionChange"
          @row-click="onRowClick"
        >
          <el-table-column type="selection" width="45" align="center" />
          <el-table-column label="岗位编码" prop="postCode" width="170" show-overflow-tooltip />
          <el-table-column label="岗位名称" prop="postName" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.status === '0'">正常</span>
              <span v-else-if="row.status === '1'">停用</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 单选模式 -->
        <el-table
          v-else
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
              <el-radio :model-value="selectedId" :value="row.postId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="岗位编码" prop="postCode" width="170" show-overflow-tooltip />
          <el-table-column label="岗位名称" prop="postName" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.status === '0'">正常</span>
              <span v-else-if="row.status === '1'">停用</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="position-picker-pager">
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
      <el-button type="primary" @click="handleConfirm" :disabled="multiple ? !selection.length : !selectedId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listPost } from '@/api/system/post'

const props = defineProps({
  title: {
    type: String,
    default: '选择岗位'
  },
  /** 是否多选模式 */
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
const pendingId = ref(null)
const pendingIds = ref([])
const selection = ref([])
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  postCode: undefined,
  postName: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.postCode = undefined
  queryParams.postName = undefined
  queryParams.pageNum = 1
  if (props.multiple) {
    selection.value = []
  } else {
    selectedId.value = pendingId.value || null
    selectedRow.value = null
  }
  getList()
}

/** 加载岗位列表（数据源与岗位管理一致：sys_post） */
function getList() {
  loading.value = true
  const params = { ...queryParams }
  listPost(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (props.multiple) {
      // 多选模式：回显已选行
      nextTick(() => {
        if (!tableRef.value) return
        list.value.forEach(row => {
          if (pendingIds.value.includes(row.postId)) {
            tableRef.value.toggleRowSelection(row, true)
          }
        })
      })
    } else {
      // 单选模式：回显选中行
      if (selectedId.value) {
        nextTick(() => {
          if (tableRef.value) {
            tableRef.value.setCurrentRow(list.value.find(r => r.postId === selectedId.value))
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
  queryParams.postCode = undefined
  queryParams.postName = undefined
  handleQuery()
}

/** 多选模式：跨页保留已选行 */
function onSelectionChange(rows) {
  const pageIds = list.value.map(r => r.postId)
  const kept = selection.value.filter(r => !pageIds.includes(r.postId))
  selection.value = [...kept, ...rows]
}

/** 多选模式：点击行切换勾选 */
function onRowClick(row, column) {
  if (props.multiple) {
    // 点击勾选框列本身时由 checkbox 处理，避免双重切换
    if (column && column.type === 'selection') return
    if (tableRef.value) tableRef.value.toggleRowSelection(row)
    return
  }
  // 单选模式：行点击 - 选中
  selectedId.value = row.postId
  selectedRow.value = row
}

/** 单选模式：行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.postId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (props.multiple) {
    if (!selection.value.length) return
    emit('confirm', selection.value.map(r => ({
      postId: r.postId,
      postCode: r.postCode,
      postName: r.postName
    })))
  } else {
    if (!selectedId.value) return
    emit('confirm', {
      positionId: selectedRow.value.postId,
      positionCode: selectedRow.value.postCode,
      positionName: selectedRow.value.postName
    })
  }
  visible.value = false
}

/** 打开弹窗
 *  单选模式：传入当前已选岗位ID
 *  多选模式：传入当前已选岗位ID数组
 */
function open(currentPositionId) {
  if (props.multiple) {
    pendingIds.value = Array.isArray(currentPositionId) ? currentPositionId : []
  } else {
    pendingId.value = currentPositionId || null
  }
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.position-picker {
  display: flex;
  flex-direction: column;
}

.position-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.position-picker-table {
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

.position-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
