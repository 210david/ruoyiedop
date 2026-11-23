<template>
  <el-dialog v-model="visible" width="720px" append-to-body draggable class="rd-dialog" @open="onOpen">
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
    <div class="driver-picker">
      <!-- 搜索栏 -->
      <div class="driver-picker-search">
        <el-input v-model="queryParams.driverName" placeholder="司机姓名" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable size="small" style="width: 160px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 司机表格 -->
      <div class="driver-picker-table">
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
              <el-radio :model-value="selectedId" :value="row.driverId" @click.stop="onRowClick(row)"><span /></el-radio>
            </template>
          </el-table-column>
          <el-table-column label="司机姓名" prop="driverName" width="110" show-overflow-tooltip />
          <el-table-column label="手机号" prop="phone" width="130" show-overflow-tooltip />
          <el-table-column label="驾驶证类型" prop="licenseType" min-width="100" align="center" />
          <el-table-column label="状态" prop="status" width="80" align="center">
            <template #default="scope"><dict-tag :options="oa_driver_status" :value="scope.row.status" /></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="driver-picker-pager">
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
import { pickerDriverList, getOaDriver } from '@/api/oa/driver'
import { Search } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { oa_driver_status } = proxy.useDict('oa_driver_status')

const props = defineProps({
  title: {
    type: String,
    default: '选择司机'
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

/** 打开时由父页面带入的当前选中司机，用于回显 */
let pending = { id: null, name: '' }

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  driverName: undefined,
  phone: undefined
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.driverName = undefined
  queryParams.phone = undefined
  queryParams.pageNum = 1
  selectedId.value = pending.id || null
  selectedRow.value = null
  // 修改场景：先回填当前已选司机，避免其不在当前页时确定按钮不可用
  if (pending.id) {
    if (pending.name) {
      selectedRow.value = { driverId: pending.id, driverName: pending.name }
    } else {
      // 调用方只传了ID：补拉详情，防止直接点确定时把司机姓名写成空值
      getOaDriver(pending.id).then(res => {
        const d = res.data || {}
        if (selectedId.value === pending.id) {
          selectedRow.value = { driverId: d.driverId || pending.id, driverName: d.driverName }
        }
      }).catch(() => {})
    }
  }
  getList()
}

/** 加载司机列表 */
function getList() {
  loading.value = true
  pickerDriverList(queryParams).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 回显选中行：命中当前页时用行数据补全信息
    if (selectedId.value) {
      const hit = (list.value || []).find(r => r.driverId === selectedId.value)
      if (hit) {
        selectedRow.value = hit
      }
      nextTick(() => {
        if (tableRef.value && hit) {
          tableRef.value.setCurrentRow(hit)
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
  queryParams.driverName = undefined
  queryParams.phone = undefined
  handleQuery()
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedId.value = row.driverId
  selectedRow.value = row
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedId.value = row.driverId
  selectedRow.value = row
  handleConfirm()
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedId.value || !selectedRow.value) return
  emit('confirm', {
    driverId: selectedRow.value.driverId,
    driverName: selectedRow.value.driverName,
    phone: selectedRow.value.phone,
    ownerDept: selectedRow.value.ownerDept,
    licenseType: selectedRow.value.licenseType
  })
  visible.value = false
}

/**
 * 打开弹窗
 * @param {Number|String} currentDriverId 当前已选司机ID（回显用）
 * @param {String} currentDriverName 当前已选司机姓名（回显用，缺省时仅保留ID）
 */
function open(currentDriverId, currentDriverName) {
  pending = { id: currentDriverId || null, name: currentDriverName || '' }
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.driver-picker {
  display: flex;
  flex-direction: column;
}

.driver-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.driver-picker-table {
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

.driver-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
