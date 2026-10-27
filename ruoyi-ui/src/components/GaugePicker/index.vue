<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"/>
            <path d="M12 7v5l3 3"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="gauge-picker">
      <!-- 搜索栏 -->
      <div class="gauge-picker-search">
        <el-input v-model="queryParams.gaugeNo" placeholder="量检具编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.gaugeName" placeholder="量检具名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.gaugeType" placeholder="类型" clearable size="small" style="width: 130px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in qms_gauge_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>
      <div v-if="hint" class="gauge-picker-hint">{{ hint }}</div>

      <!-- 量检具表格 -->
      <div class="gauge-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.gaugeId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="量检具编号" prop="gaugeNo" width="140" show-overflow-tooltip />
          <el-table-column label="量检具名称" prop="gaugeName" min-width="160" show-overflow-tooltip />
          <el-table-column label="类型" prop="gaugeType" width="100" align="center">
            <template #default="scope"><span class="badge violet">{{ gaugeTypeLabel(scope.row.gaugeType) }}</span></template>
          </el-table-column>
          <el-table-column label="精度" prop="precisionVal" width="90" align="center" />
          <el-table-column label="测量范围" prop="measureRange" width="130" show-overflow-tooltip />
          <el-table-column label="量检具状态" prop="gaugeStatus" width="100" align="center">
            <template #default="scope"><span class="badge" :class="gaugeBadgeClass(scope.row.gaugeStatus)">{{ gaugeStatusLabel(scope.row.gaugeStatus) }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="gauge-picker-pager">
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
import { listGauge } from '@/api/qms/gauge'

const { proxy } = getCurrentInstance()
const { qms_gauge_type, qms_gauge_status } = proxy.useDict('qms_gauge_type', 'qms_gauge_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择量检具'
  },
  hint: {
    type: String,
    default: ''
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
  gaugeNo: undefined,
  gaugeName: undefined,
  gaugeType: undefined,
  gaugeStatus: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.gaugeNo = undefined
  queryParams.gaugeName = undefined
  queryParams.gaugeType = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载量检具列表 */
function getList() {
  loading.value = true
  listGauge(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.gaugeId === selectedId.value))
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
  queryParams.gaugeNo = undefined
  queryParams.gaugeName = undefined
  queryParams.gaugeType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.gaugeId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.gaugeId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', {
    gaugeId: selectedRow.value.gaugeId,
    gaugeNo: selectedRow.value.gaugeNo,
    gaugeName: selectedRow.value.gaugeName
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentGaugeId) {
  visible.value = true
  if (currentGaugeId) {
    selectedId.value = currentGaugeId
  }
}

/** 量检具类型标签 */
function gaugeTypeLabel(type) {
  const item = qms_gauge_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

/** 量检具状态标签 */
function gaugeStatusLabel(val) {
  const item = qms_gauge_status.value.find(d => d.value == val)
  return item ? item.label : '-'
}

/** 量检具状态样式 */
function gaugeBadgeClass(val) {
  if (val == '0') return 'green'
  if (val == '1') return 'red'
  if (val == '2') return 'orange'
  return 'gray'
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.gauge-picker {
  display: flex;
  flex-direction: column;
}

.gauge-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.gauge-picker-hint {
  margin: -4px 0 8px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}

.gauge-picker-table {
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

.gauge-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
