<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="plan-picker">
      <!-- 搜索栏 -->
      <div class="plan-picker-search">
        <el-input v-model="queryParams.planNo" placeholder="计划编号" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.title" placeholder="计划标题" clearable size="small" style="width: 220px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.planType" placeholder="计划类型" clearable size="small" style="width: 120px; margin-left: 8px" @change="handleQuery">
          <el-option v-for="d in pms_plan_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 计划表格 -->
      <div class="plan-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.planId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="计划编号" prop="planNo" width="140" show-overflow-tooltip />
          <el-table-column label="计划标题" prop="title" min-width="200" show-overflow-tooltip />
          <el-table-column label="计划类型" prop="planType" width="90" align="center">
            <template #default="scope"><span class="badge violet">{{ planTypeLabel(scope.row.planType) }}</span></template>
          </el-table-column>
          <el-table-column label="预算金额" prop="budgetAmount" width="110" align="right">
            <template #default="scope">{{ formatMoney(scope.row.budgetAmount) }}</template>
          </el-table-column>
          <el-table-column label="开始日期" prop="startDate" width="110" align="center" />
          <el-table-column label="编制人" prop="plannerName" width="100" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="plan-picker-pager">
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
import { listPlan } from '@/api/pms/plan'

const { proxy } = getCurrentInstance()
const { pms_plan_type } = proxy.useDict('pms_plan_type')

const props = defineProps({
  title: {
    type: String,
    default: '选择采购计划'
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
  planNo: undefined,
  title: undefined,
  planType: undefined,
  status: '2'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.planNo = undefined
  queryParams.title = undefined
  queryParams.planType = undefined
  queryParams.pageNum = 1
  selectedId.value = null
  selectedRow.value = null
  getList()
}

/** 加载计划列表 */
function getList() {
  loading.value = true
  listPlan(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.planId === selectedId.value))
        }
      })
    }
  }).catch(() => { loading.value = false })
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.planNo = undefined
  queryParams.title = undefined
  queryParams.planType = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.planId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.planId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  emit('confirm', { ...selectedRow.value })
  visible.value = false
}

/** 打开弹窗：currentPlanId 当前选中计划（可选，用于回显高亮） */
function open(currentPlanId) {
  visible.value = true
  if (currentPlanId) {
    selectedId.value = currentPlanId
  }
}

/** 计划类型标签 */
function planTypeLabel(planType) {
  const item = pms_plan_type.value.find(d => d.value == planType)
  return item ? item.label : '-'
}

function formatMoney(val) {
  if (val === null || val === undefined || val === '') return '-'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.plan-picker {
  display: flex;
  flex-direction: column;
}

.plan-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.plan-picker-table {
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

.plan-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
