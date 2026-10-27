<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="mk-channel-picker">
      <!-- 搜索栏 -->
      <div class="mk-channel-picker-search">
        <el-input v-model="queryParams.accountName" placeholder="账号名称" clearable size="small" style="width: 180px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="queryParams.channelType" placeholder="渠道类型" clearable size="small" style="width: 120px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in marketing_channel_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 渠道账号表格（单选） -->
      <div class="mk-channel-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          row-key="channelId"
          highlight-current-row
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          height="360"
          size="small"
        >
          <el-table-column width="45" align="center">
            <template #default="{ row }">
              <el-radio :model-value="selectedId" :value="row.channelId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="账号名称" prop="accountName" min-width="180" show-overflow-tooltip />
          <el-table-column label="渠道类型" prop="channelType" width="110" align="center">
            <template #default="scope">
              <span class="badge" :class="typeBadgeClass(scope.row.channelType)">{{ typeLabel(scope.row.channelType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="负责人" prop="userName" width="110" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.userName || '-' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="mk-channel-picker-pager">
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
      <el-button type="primary" @click="handleConfirm" :disabled="selectedId == null">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listChannel } from '@/api/mk/channel'

const { proxy } = getCurrentInstance()
const { marketing_channel_type } = proxy.useDict('marketing_channel_type')

const props = defineProps({
  title: {
    type: String,
    default: '选择渠道账号'
  },
  // v-model：渠道账号ID
  modelValue: {
    type: [String, Number],
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
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  accountName: undefined,
  channelType: undefined,
  channelStatus: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.accountName = undefined
  queryParams.channelType = undefined
  queryParams.pageNum = 1
  getList()
}

/** 加载渠道账号列表 */
function getList() {
  loading.value = true
  listChannel(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    // 回显选中行
    if (selectedId.value != null) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.channelId === selectedId.value))
        }
      })
    }
  }).catch(() => {
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
  queryParams.accountName = undefined
  queryParams.channelType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.channelId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.channelId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (selectedId.value == null) return
  const r = selectedRow.value || list.value.find(i => i.channelId === selectedId.value) || { channelId: selectedId.value }
  emit('update:modelValue', selectedId.value)
  emit('confirm', {
    channelId: r.channelId,
    accountName: r.accountName,
    channelType: r.channelType
  })
  visible.value = false
}

/**
 * 打开弹窗
 * open(currentChannelId) 传入当前已选渠道ID（未传时回退 modelValue）
 */
function open(selected) {
  const id = selected != null && selected !== '' ? selected : props.modelValue
  selectedId.value = id != null ? id : null
  selectedRow.value = null
  visible.value = true
}

/** 渠道类型徽章颜色 */
function typeBadgeClass(type) {
  const item = marketing_channel_type.value.find(d => d.value == type)
  const cls = item ? item.listClass : ''
  const map = { 'primary': 'blue', 'info': 'gray', 'success': 'green', 'warning': 'amber', 'danger': 'red' }
  return map[cls] || 'gray'
}

/** 渠道类型标签 */
function typeLabel(type) {
  const item = marketing_channel_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

defineExpose({ open })
</script>

<style scoped>
.mk-channel-picker {
  display: flex;
  flex-direction: column;
}
.mk-channel-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}
.mk-channel-picker-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.mk-channel-picker-table :deep(.el-table__row) {
  cursor: pointer;
}
.mk-channel-picker-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
