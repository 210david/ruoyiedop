<template>
  <el-dialog v-model="visible" width="760px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="employee-picker">
      <!-- 搜索栏 -->
      <div class="employee-picker-search">
        <el-input v-model="queryParams.employeeNo" placeholder="工号" clearable size="small" style="width: 140px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.employeeName" placeholder="姓名" clearable size="small" style="width: 150px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-input v-model="queryParams.phone" placeholder="联系电话" clearable size="small" style="width: 150px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 员工表格 -->
      <div class="employee-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.employeeId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="工号" prop="employeeNo" width="110" show-overflow-tooltip />
          <el-table-column label="姓名" prop="employeeName" width="110" show-overflow-tooltip />
          <el-table-column label="部门" prop="orgName" min-width="140" show-overflow-tooltip />
          <el-table-column label="岗位" prop="positionName" width="130" show-overflow-tooltip />
          <el-table-column label="联系电话" prop="phone" width="130" show-overflow-tooltip />
          <el-table-column label="状态" prop="employeeStatus" width="80" align="center">
            <template #default="{ row }">
              <dict-tag :options="hr_employee_status" :value="row.employeeStatus" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="employee-picker-pager">
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
import { listHrEmployee } from '@/api/hr/hrEmployee'

const { proxy } = getCurrentInstance()
const { hr_employee_status } = proxy.useDict('hr_employee_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择人员'
  },
  /** 是否只显示在职/试用人员（默认 true，排除离职、退休） */
  onlyActive: {
    type: Boolean,
    default: true
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
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  employeeNo: undefined,
  employeeName: undefined,
  phone: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.employeeNo = undefined
  queryParams.employeeName = undefined
  queryParams.phone = undefined
  queryParams.pageNum = 1
  selectedId.value = pendingId.value || null
  selectedRow.value = null
  getList()
}

/** 加载员工档案列表（数据源与员工档案一致：biz_hr_employee） */
function getList() {
  loading.value = true
  const params = { ...queryParams }
  if (props.onlyActive) {
    // 仅在职(0)/试用(1)
    params.params = { activeOnly: true }
  }
  listHrEmployee(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行
    if (selectedId.value) {
      nextTick(() => {
        if (tableRef.value) {
          tableRef.value.setCurrentRow(list.value.find(r => r.employeeId === selectedId.value))
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
  queryParams.employeeNo = undefined
  queryParams.employeeName = undefined
  queryParams.phone = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.employeeId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.employeeId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value) return
  const row = selectedRow.value
  emit('confirm', {
    employeeId: row.employeeId,
    employeeNo: row.employeeNo,
    employeeName: row.employeeName,
    idCard: row.idCard,
    gender: row.gender,
    phone: row.phone,
    email: row.email,
    orgId: row.orgId,
    orgName: row.orgName,
    positionId: row.positionId,
    positionName: row.positionName,
    employeeStatus: row.employeeStatus
  })
  visible.value = false
}

/** 打开弹窗 */
function open(currentEmployeeId) {
  pendingId.value = currentEmployeeId || null
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.employee-picker {
  display: flex;
  flex-direction: column;
}

.employee-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.employee-picker-table {
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

.employee-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
