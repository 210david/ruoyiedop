<template>
  <el-dialog v-model="visible" width="860px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
      <!-- 左侧部门树 -->
      <div class="picker-left">
        <div class="picker-left-header">
          <el-icon><OfficeBuilding /></el-icon>
          <span>组织机构</span>
        </div>
        <div class="picker-left-search">
          <el-input v-model="deptKeyword" placeholder="搜索部门" clearable size="small">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="picker-left-tree">
          <el-tree
            ref="deptTreeRef"
            :data="deptOptions"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            :expand-on-click-node="false"
            :default-expand-all="true"
            :filter-node-method="filterDeptNode"
            highlight-current
            @node-click="onDeptClick"
          >
            <template #default="{ data }">
              <span class="dept-node">
                <span class="node-icon-badge" :class="data.children && data.children.length ? 'node-icon-badge--dept' : 'node-icon-badge--leaf'">
                  <el-icon><Folder v-if="data.children && data.children.length" /><Document v-else /></el-icon>
                </span>
                <span class="dept-label" :title="data.label">{{ data.label }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>

      <!-- 右侧员工列表 -->
      <div class="picker-right">
        <!-- 搜索栏 -->
        <div class="employee-picker-search">
          <el-input v-model="queryParams.employeeNo" placeholder="工号" clearable size="small" style="width: 120px" @keyup.enter="handleQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input v-model="queryParams.employeeName" placeholder="姓名" clearable size="small" style="width: 130px; margin-left: 8px" @keyup.enter="handleQuery" />
          <el-input v-if="!multiple" v-model="queryParams.phone" placeholder="电话" clearable size="small" style="width: 110px; margin-left: 8px" @keyup.enter="handleQuery" />
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
          <div class="picker-right-info" v-if="multiple && selectedRows.length > 0">
            <el-tag type="success" effect="light" size="small" closable @close="clearAllSelection">已选 {{ selectedRows.length }} 人</el-tag>
          </div>
        </div>

        <!-- 多选模式：已选人员标签栏 -->
        <div class="employee-picker-selected" v-if="multiple && selectedRows.length > 0">
          <div class="selected-tags">
            <el-tag v-for="row in selectedRows" :key="row.employeeId" closable size="small" @close="toggleRowSelection(row, false)" style="margin: 2px 4px 2px 0">
              {{ row.employeeName }}（{{ row.employeeNo }}）
            </el-tag>
          </div>
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
            @selection-change="onSelectionChange"
            height="360"
            size="small"
          >
            <!-- 单选模式 -->
            <el-table-column v-if="!multiple" width="45" align="center">
              <template #default="{ row }">
                <el-radio :model-value="selectedId" :value="row.employeeId" @click.stop="onRowClick(row)"><span /></el-radio>
              </template>
            </el-table-column>
            <!-- 多选模式 -->
            <el-table-column v-if="multiple" type="selection" width="45" align="center" :selectable="canSelect" />
            <el-table-column label="工号" prop="employeeNo" width="110" show-overflow-tooltip />
            <el-table-column label="姓名" prop="employeeName" width="100" show-overflow-tooltip />
          <el-table-column label="部门" prop="orgName" min-width="120" show-overflow-tooltip />
          <el-table-column label="岗位" prop="positionName" width="110" show-overflow-tooltip />
          <el-table-column v-if="!multiple" label="电话" prop="phone" width="110" show-overflow-tooltip />
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
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="confirmDisabled">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listHrEmployee } from '@/api/hr/hrEmployee'
import { deptTreeSelect } from '@/api/system/user'
import { Search, OfficeBuilding, Folder, Document } from '@element-plus/icons-vue'

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
  },
  /** 是否多选模式（默认 false 单选） */
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
const tableRef = ref()

/** 多选模式状态 */
const selectedRows = ref([])
const pendingIds = ref([])

/** 部门树 */
const deptOptions = ref([])
const deptKeyword = ref('')
const selectedDeptId = ref(undefined)
const deptTreeRef = ref()

/** 确认按钮是否禁用 */
const confirmDisabled = computed(() => {
  if (props.multiple) {
    return selectedRows.value.length === 0
  }
  return !selectedId.value
})

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
  selectedDeptId.value = undefined
  deptKeyword.value = ''
  if (props.multiple) {
    selectedRows.value = []
    pendingIds.value = pendingIds.value || []
  } else {
    selectedId.value = pendingId.value || null
    selectedRow.value = null
  }
  loadDeptTree()
  getList()
}

/** 加载部门树 */
function loadDeptTree() {
  deptTreeSelect().then(res => {
    deptOptions.value = res.data
  })
}

/** 过滤部门节点 */
function filterDeptNode(value, data) {
  if (!value) return true
  return data.label && data.label.indexOf(value) !== -1
}

/** 监听部门搜索关键词 */
watch(deptKeyword, (val) => {
  if (deptTreeRef.value) {
    deptTreeRef.value.filter(val)
  }
})

/** 部门树点击 */
function onDeptClick(data) {
  selectedDeptId.value = data.id
  queryParams.pageNum = 1
  getList()
}

/** 加载员工档案列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams }
  if (props.onlyActive) {
    params.params = { activeOnly: true }
  }
  if (selectedDeptId.value) {
    params.orgId = selectedDeptId.value
  }
  listHrEmployee(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    if (props.multiple && pendingIds.value.length > 0) {
      nextTick(() => {
        if (tableRef.value) {
          list.value.forEach(row => {
            if (pendingIds.value.includes(row.employeeId)) {
              tableRef.value.toggleRowSelection(row, true)
            }
          })
        }
      })
    } else if (!props.multiple && selectedId.value) {
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
  selectedDeptId.value = undefined
  if (deptTreeRef.value) {
    deptTreeRef.value.setCurrentKey(null)
  }
  handleQuery()
}

/** 单选模式：行点击 - 选中；多选模式：行点击 - 切换选中 */
function onRowClick(row) {
  if (props.multiple) {
    if (tableRef.value) {
      tableRef.value.toggleRowSelection(row)
    }
    return
  }
  selectedId.value = row.employeeId
  selectedRow.value = row
}

/** 单选模式：行双击 - 确认 */
function onRowDblClick(row) {
  if (props.multiple) return
  selectedId.value = row.employeeId
  selectedRow.value = row
  handleConfirm()
}

/** 多选模式：选择行变化 */
function onSelectionChange(rows) {
  if (!props.multiple) return
  // 合并分页选择：保留不在当前页的已选 + 当前页的已选
  const currentPageIds = new Set(list.value.map(r => r.employeeId))
  const kept = selectedRows.value.filter(r => !currentPageIds.has(r.employeeId))
  selectedRows.value = [...kept, ...rows]
}

/** 多选模式：手动切换行选择状态 */
function toggleRowSelection(row, selected) {
  if (tableRef.value) {
    tableRef.value.toggleRowSelection(row, selected)
  }
  if (!selected) {
    selectedRows.value = selectedRows.value.filter(r => r.employeeId !== row.employeeId)
  }
}

/** 多选模式：清空所有选择 */
function clearAllSelection() {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  selectedRows.value = []
}

/** 多选模式：行是否可选 */
function canSelect() {
  return true
}

/** 确认选择 */
function handleConfirm() {
  if (props.multiple) {
    if (selectedRows.value.length === 0) return
    emit('confirm', selectedRows.value.map(row => ({
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
    })))
    visible.value = false
  } else {
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
}

/** 打开弹窗 */
function open(currentId) {
  if (props.multiple) {
    pendingIds.value = Array.isArray(currentId) ? [...currentId] : (currentId ? [currentId] : [])
    selectedRows.value = []
  } else {
    pendingId.value = currentId || null
  }
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.employee-picker {
  display: flex;
  flex-direction: row;
  gap: 12px;
  height: 480px;
}

/* ===== 左侧部门树 ===== */
.picker-left {
  width: 240px;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fafbfc;
}

.picker-left-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;

  .el-icon {
    color: #409eff;
    font-size: 15px;
  }
}

.picker-left-search {
  padding: 8px 10px;
  border-bottom: 1px solid #f0f0f0;
}

.picker-left-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 6px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 4px;
  }

  :deep(.el-tree-node__content) {
    height: 30px;
    border-radius: 4px;
    margin-bottom: 1px;

    &:hover {
      background: #f0f7ff;
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background: #e6f0fd;
    color: #409eff;
    font-weight: 600;
  }
}

.dept-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  overflow: hidden;

  .dept-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.node-icon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 5px;
  flex-shrink: 0;

  .el-icon {
    font-size: 12px;
    color: #fff;
  }

  &--dept {
    background: linear-gradient(135deg, #f5b454, #e6a23c);
  }

  &--leaf {
    background: linear-gradient(135deg, #67c23a, #529b2e);
  }
}

/* ===== 右侧员工列表 ===== */
.picker-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.employee-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.picker-right-info {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.employee-picker-selected {
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;

  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    max-height: 80px;
    overflow-y: auto;
  }
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
